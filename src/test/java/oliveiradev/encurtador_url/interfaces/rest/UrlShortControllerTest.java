package oliveiradev.encurtador_url.interfaces.rest;

import oliveiradev.encurtador_url.application.dto.DtoUrlEncurtada;
import oliveiradev.encurtador_url.application.service.AplicacaoEncurtadorService;
import oliveiradev.encurtador_url.interfaces.rest.dto.EncurtarUrlHttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.any;
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
    private AplicacaoEncurtadorService mockServicoAplicacao;

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
        String urlOriginal = "[https://www.valida.com/com-ttl](https://www.valida.com/com-ttl)";
        Long ttl = 60L;
        String codigoCurto = "TtlCd1";
        EncurtarUrlHttpRequest request = new EncurtarUrlHttpRequest(urlOriginal, ttl);

        DtoUrlEncurtada dtoApp = new DtoUrlEncurtada(
                urlOriginal, codigoCurto, BASE_URL_TESTE + "/" + codigoCurto, 0L, dataCriacaoTeste, dataExpiracaoTeste
        );

        when(mockServicoAplicacao.encurtarUrl(argThat(cmd ->
                cmd.getUrlOriginal().equals(urlOriginal) && cmd.getTtlEmMinutos() != null && cmd.getTtlEmMinutos().equals(ttl)
        ))).thenReturn(dtoApp);

        mockMvc.perform(post("/api/v1/encurtar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.urlEncurtada", is(BASE_URL_TESTE + "/" + codigoCurto)))
                .andExpect(jsonPath("$.dataExpiracao", is(dataExpiracaoTeste.format(isoFormatter))));
    }

    @Test
    void redirecionar_ComCodigoValidoNaoExpirado_DeveRetornarStatusFoundERedirecionar() throws Exception {
        String codigoCurto = "ValidCd";
        String urlDestino = "[https://destino.valido.com](https://destino.valido.com)";
        when(mockServicoAplicacao.redirecionarEIncrementarAcesso(codigoCurto)).thenReturn(Optional.of(urlDestino));

        mockMvc.perform(get("/{codigoCurto}", codigoCurto))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(urlDestino));
    }

    @Test
    void obterInfoUrl_ComCodigoValidoNaoExpirado_DeveRetornarStatusOKERespostaCompleta() throws Exception {
        String codigoCurto = "InfoOkCd";
        String urlOriginal = "[https://info.ok.com](https://info.ok.com)";
        DtoUrlEncurtada dtoApp = new DtoUrlEncurtada(
                urlOriginal, codigoCurto, BASE_URL_TESTE + "/" + codigoCurto, 15L, dataCriacaoTeste, dataExpiracaoTeste
        );
        when(mockServicoAplicacao.obterInfoUrlPorCodigoCurto(codigoCurto)).thenReturn(Optional.of(dtoApp));

        mockMvc.perform(get("/api/v1/info/{codigoCurto}", codigoCurto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlEncurtada", is(BASE_URL_TESTE + "/" + codigoCurto)))
                .andExpect(jsonPath("$.dataExpiracao", is(dataExpiracaoTeste.format(isoFormatter))));
    }
}
