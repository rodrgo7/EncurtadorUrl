package oliveiradev.encurtador_url.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import oliveiradev.encurtador_url.domain.service.AplicacaoEncurtadorService;
import oliveiradev.encurtador_url.dto.DtoUrlEncurtada;
import oliveiradev.encurtador_url.interfaces.rest.dto.EncurtarUrlHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortController.class)
class UrlShortControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AplicacaoEncurtadorService mockAplicacaoService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL_TESTE = "http://localhost:8080";
    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private LocalDateTime dataCriacaoTeste;
    private LocalDateTime dataExpiracaoTeste;

    @BeforeEach
    void setUp() {
        dataCriacaoTeste = LocalDateTime.now().minusHours(1);
        dataExpiracaoTeste = LocalDateTime.now().plusDays(1);
    }

    @Test
    void encurtarUrl_ComRequestValidoETTL_DeveRetornarStatusCreatedERespostaCompleta() throws Exception {
        String urlOriginal = "https://www.valida.com/com-ttl";
        Long ttl = 60L;
        String codigoCurto = "TtlCd1";
        EncurtarUrlHttpRequest request = new EncurtarUrlHttpRequest(urlOriginal, ttl);

        DtoUrlEncurtada dtoApp = new DtoUrlEncurtada(
                urlOriginal, codigoCurto, BASE_URL_TESTE + "/" + codigoCurto, 0L, dataCriacaoTeste, dataExpiracaoTeste
        );

        when(mockAplicacaoService.encurtarUrl(argThat(cmd ->
                cmd.getUrlOriginal().equals(urlOriginal) && cmd.getTtlEmMinutos().equals(ttl)
        ))).thenReturn(dtoApp);

        mockMvc.perform(post("/api/v1/encurtar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.urlEncurtada", is(BASE_URL_TESTE + "/" + codigoCurto)))
                .andExpect(jsonPath("$.urlOriginal", is(urlOriginal)))
                .andExpect(jsonPath("$.acessos", is(0)))
                .andExpect(jsonPath("$.dataCriacao", is(dataCriacaoTeste.format(isoFormatter))))
                .andExpect(jsonPath("$.dataExpiracao", is(dataExpiracaoTeste.format(isoFormatter))));
    }

    @Test
    void encurtarUrl_ComRequestValidoSemTTL_DeveRetornarStatusCreatedComExpiracaoNula() throws Exception {
        String urlOriginal = "https://www.valida.com/sem-ttl";
        EncurtarUrlHttpRequest request = new EncurtarUrlHttpRequest(urlOriginal, null); // Sem TTL
        String codigoCurto = "NoTtlCd";

        DtoUrlEncurtada dtoApp = new DtoUrlEncurtada(
                urlOriginal, codigoCurto, BASE_URL_TESTE + "/" + codigoCurto, 0L, dataCriacaoTeste, null // Expiracao nula
        );

        when(mockAplicacaoService.encurtarUrl(argThat(cmd ->
                cmd.getUrlOriginal().equals(urlOriginal) && cmd.getTtlEmMinutos() == null
        ))).thenReturn(dtoApp);

        mockMvc.perform(post("/api/v1/encurtar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.urlEncurtada", is(BASE_URL_TESTE + "/" + codigoCurto)))
                .andExpect(jsonPath("$.dataExpiracao", is(nullValue()))); // Verifica se a expiração é nula
    }

    @Test
    void encurtarUrl_ComUrlInvalida_DeveRetornarStatusBadRequest() throws Exception {
        EncurtarUrlHttpRequest request = new EncurtarUrlHttpRequest("url-invalida", null);

        // A validação @URL no DTO EncurtarUrlHttpRequest deve falhar
        mockMvc.perform(post("/api/v1/encurtar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Espera status 400
    }

    @Test
    void encurtarUrl_ComTTLInvalido_DeveRetornarStatusBadRequest() throws Exception {
        EncurtarUrlHttpRequest request = new EncurtarUrlHttpRequest("https://valida.com", 0L); // TTL 0 é inválido (@Min(1))

        // A validação @Min no DTO EncurtarUrlHttpRequest deve falhar
        mockMvc.perform(post("/api/v1/encurtar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Espera status 400
    }

    // --- Testes para GET /{codigoCurto} (Redirecionamento) ---
    @Test
    void redirecionar_ComCodigoValidoNaoExpirado_DeveRetornarStatusFoundERedirecionar() throws Exception {
        String codigoCurto = "ValidCd";
        String urlDestino = "https://destino.valido.com";
        when(mockAplicacaoService.redirecionarEIncrementarAcesso(codigoCurto)).thenReturn(Optional.of(urlDestino));

        mockMvc.perform(get("/{codigoCurto}", codigoCurto)) // Simula GET para /ValidCd
                .andExpect(status().isFound()) // Verifica se o status é 302 Found
                .andExpect(redirectedUrl(urlDestino)); // Verifica o cabeçalho Location
    }

    @Test
    void redirecionar_ComCodigoNaoEncontradoOuExpirado_DeveRetornarStatusNotFound() throws Exception {
        String codigoInexistente = "NotFoundCd";
        // O serviço de aplicação retorna Optional.empty para códigos não encontrados ou expirados
        when(mockAplicacaoService.redirecionarEIncrementarAcesso(codigoInexistente)).thenReturn(Optional.empty());

        mockMvc.perform(get("/{codigoCurto}", codigoInexistente))
                .andExpect(status().isNotFound()); // Espera 404 Not Found (tratado pelo ManipuladorExcecoesGlobais)
    }

    @Test
    void redirecionar_ComUrlOriginalMalformada_DeveRetornarStatusInternalServerError() throws Exception {
        String codigoCurto = "BadUrlCd";
        String urlMalformada = "htp:/invalido"; // URI inválida
        when(mockAplicacaoService.redirecionarEIncrementarAcesso(codigoCurto)).thenReturn(Optional.of(urlMalformada));
        // A URISyntaxException será lançada dentro do controller ao tentar criar a URI

        mockMvc.perform(get("/{codigoCurto}", codigoCurto))
                .andExpect(status().isInternalServerError()); // Espera 500 Internal Server Error
    }

    // --- Testes para GET /api/v1/info/{codigoCurto} ---
    @Test
    void obterInfoUrl_ComCodigoValidoNaoExpirado_DeveRetornarStatusOKERespostaCompleta() throws Exception {
        String codigoCurto = "InfoOkCd";
        String urlOriginal = "https://info.ok.com";
        DtoUrlEncurtada dtoApp = new DtoUrlEncurtada(
                urlOriginal, codigoCurto, BASE_URL_TESTE + "/" + codigoCurto, 15L, dataCriacaoTeste, dataExpiracaoTeste
        );
        when(mockAplicacaoService.obterInfoUrlPorCodigoCurto(codigoCurto)).thenReturn(Optional.of(dtoApp));

        mockMvc.perform(get("/api/v1/info/{codigoCurto}", codigoCurto))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.urlEncurtada", is(BASE_URL_TESTE + "/" + codigoCurto)))
                .andExpect(jsonPath("$.urlOriginal", is(urlOriginal)))
                .andExpect(jsonPath("$.acessos", is(15)))
                .andExpect(jsonPath("$.dataCriacao", is(dataCriacaoTeste.format(isoFormatter))))
                .andExpect(jsonPath("$.dataExpiracao", is(dataExpiracaoTeste.format(isoFormatter))));
    }

    @Test
    void obterInfoUrl_ComCodigoNaoEncontradoOuExpirado_DeveRetornarStatusNotFound() throws Exception {
        String codigoInexistente = "InfoNotFoundCd";
        // O serviço de aplicação retorna Optional.empty para códigos não encontrados ou expirados
        when(mockAplicacaoService.obterInfoUrlPorCodigoCurto(codigoInexistente)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/info/{codigoCurto}", codigoInexistente))
                .andExpect(status().isNotFound()); // Espera 404 Not Found (tratado pelo ManipuladorExcecoesGlobais)
    }
}