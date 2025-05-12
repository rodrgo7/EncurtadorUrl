package oliveiradev.encurtador_url.application.service;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.domain.service.EncurtamentoService;
import oliveiradev.encurtador_url.interfaces.dto.DtoUrlEncurtada;
import oliveiradev.encurtador_url.interfaces.dto.ComandoEncurtadorUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationEncurtadorServiceTest {
    @Mock // Cria um mock do serviço de domínio.
    private EncurtamentoService mockServicoDominio;

    // A URL base é necessária pelo serviço de aplicação. Como @Value não funciona
    // em testes unitários puros, definimos um valor fixo para os testes.
    private final String BASE_URL_TESTE = "http://test.short";

    @InjectMocks // Cria a instância do serviço de aplicação e injeta o mockServicoDominio.
    private AplicacaoEncurtadorService aplicacaoService;

    // Captors para verificar os argumentos passados para os métodos mockados.
    @Captor
    private ArgumentCaptor<UrlOriginal> urlOriginalCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> dataExpiracaoCaptor;
    @Captor
    private ArgumentCaptor<CodigoCurto> codigoCurtoCaptor;
    @Captor
    private ArgumentCaptor<MapeamentoUrl> mapeamentoCaptor;

    @BeforeEach
    void setUp() {
        // Recria a instância do serviço de aplicação antes de cada teste,
        // garantindo que a baseUrl de teste seja usada corretamente.
        // Isso é necessário porque @InjectMocks não lida bem com a injeção de valores primitivos
        // como a String da baseUrl que vem de @Value no construtor original.
        aplicacaoService = new AplicacaoEncurtadorService(mockServicoDominio, BASE_URL_TESTE);
    }

    // --- Testes para o método encurtarUrl ---

    @Test
    void encurtarUrl_ComComandoValidoETTL_DeveChamarDominioComDataCalculadaERetornarDto() {
        String urlOriginalStr = "https://valida.com/com/ttl";
        Long ttlMinutos = 60L;
        ComandoEncurtadorUrl comando = new ComandoEncurtadorUrl(urlOriginalStr, ttlMinutos);

        CodigoCurto codigoGerado = new CodigoCurto("TTLcd1");
        LocalDateTime dataCriacao = LocalDateTime.now();
        // A data de expiração exata dependerá do momento da execução, capturaremos para verificar.
        MapeamentoUrl mapeamentoRetornadoPeloDominio = new MapeamentoUrl(
                codigoGerado, new UrlOriginal(urlOriginalStr), dataCriacao.plusMinutes(ttlMinutos)
        );
        // Configura o mock do serviço de domínio
        when(mockServicoDominio.encurtar(urlOriginalCaptor.capture(), dataExpiracaoCaptor.capture()))
                .thenReturn(mapeamentoRetornadoPeloDominio);

        DtoUrlEncurtada resultadoDto = aplicacaoService.encurtarUrl(comando);

        assertNotNull(resultadoDto);
        assertEquals(urlOriginalStr, resultadoDto.getUrlOriginal());
        assertEquals(codigoGerado.getValor(), resultadoDto.getCodigoCurto());
        assertEquals(BASE_URL_TESTE + "/" + codigoGerado.getValor(), resultadoDto.getUrlEncurtadaCompleta());
        assertNotNull(resultadoDto.getDataExpiracao());
        assertEquals(0L, resultadoDto.getAcessos()); // Acessos iniciais

        // Verifica os argumentos passados para o serviço de domínio
        assertEquals(urlOriginalStr, urlOriginalCaptor.getValue().getValor());
        LocalDateTime dataExpiracaoEsperadaAprox = LocalDateTime.now().plusMinutes(ttlMinutos);
        LocalDateTime dataExpiracaoPassada = dataExpiracaoCaptor.getValue();
        assertNotNull(dataExpiracaoPassada);
        // Verifica se a data passada está próxima da esperada (tolerância de segundos)
        assertTrue(ChronoUnit.SECONDS.between(dataExpiracaoPassada, dataExpiracaoEsperadaAprox) < 5,
                "A data de expiração passada para o domínio não está correta.");

        // Verifica se o método do domínio foi chamado uma vez.
        verify(mockServicoDominio, times(1)).encurtar(any(UrlOriginal.class), any(LocalDateTime.class));
    }

    @Test
    void encurtarUrl_ComComandoValidoSemTTL_DeveChamarDominioComDataNulaERetornarDtoSemExpiracao() {
        // Arrange
        String urlOriginalStr = "https://valida.com/sem/ttl";
        ComandoEncurtadorUrl comando = new ComandoEncurtadorUrl(urlOriginalStr, null); // Sem TTL

        CodigoCurto codigoGerado = new CodigoCurto("NoTTLcd");
        MapeamentoUrl mapeamentoRetornadoPeloDominio = new MapeamentoUrl(
                codigoGerado, new UrlOriginal(urlOriginalStr), null // Sem data de expiração
        );
        when(mockServicoDominio.encurtar(urlOriginalCaptor.capture(), dataExpiracaoCaptor.capture()))
                .thenReturn(mapeamentoRetornadoPeloDominio);

        // Act
        DtoUrlEncurtada resultadoDto = aplicacaoService.encurtarUrl(comando);

        // Assert
        assertNotNull(resultadoDto);
        assertEquals(urlOriginalStr, resultadoDto.getUrlOriginal());
        assertEquals(codigoGerado.getValor(), resultadoDto.getCodigoCurto());
        assertNull(resultadoDto.getDataExpiracao(), "O DTO não deve ter data de expiração.");

        // Verifica se o domínio foi chamado com data de expiração nula.
        assertNull(dataExpiracaoCaptor.getValue(), "A data de expiração passada para o domínio deve ser nula.");
        verify(mockServicoDominio, times(1)).encurtar(any(UrlOriginal.class), eq(null));
    }

    // --- Testes para o método obterInfoUrlPorCodigoCurto ---

    @Test
    void obterInfoUrlPorCodigoCurto_QuandoEncontradoENaoExpirado_DeveRetornarDto() {
        // Arrange
        String codigoStr = "InfoOk";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        UrlOriginal urlObj = new UrlOriginal("http://info.test");
        LocalDateTime dataCriacao = LocalDateTime.now().minusDays(1);
        LocalDateTime dataExpiracao = LocalDateTime.now().plusDays(1); // Não expirado
        MapeamentoUrl mapeamentoMock = mock(MapeamentoUrl.class); // Usar mock para controlar isExpirado

        when(mapeamentoMock.getCodigoCurto()).thenReturn(codigoObj);
        when(mapeamentoMock.getUrlOriginal()).thenReturn(urlObj);
        when(mapeamentoMock.getContadorAcessos()).thenReturn(10L);
        when(mapeamentoMock.getDataCriacao()).thenReturn(dataCriacao);
        when(mapeamentoMock.getDataExpiracao()).thenReturn(dataExpiracao);
        when(mapeamentoMock.isExpirado(any(LocalDateTime.class))).thenReturn(false); // Mock para não expirado

        when(mockServicoDominio.buscaCodigoCurto(codigoObj)).thenReturn(Optional.of(mapeamentoMock));

        // Act
        Optional<DtoUrlEncurtada> resultadoOpt = aplicacaoService.obterInfoUrlPorCodigoCurto(codigoStr);

        // Assert
        assertTrue(resultadoOpt.isPresent(), "Deveria retornar um DTO.");
        DtoUrlEncurtada dto = resultadoOpt.get();
        assertEquals(urlObj.getValor(), dto.getUrlOriginal());
        assertEquals(codigoStr, dto.getCodigoCurto());
        assertEquals(BASE_URL_TESTE + "/" + codigoStr, dto.getUrlEncurtadaCompleta());
        assertEquals(10L, dto.getAcessos());
        assertEquals(dataCriacao, dto.getDataCriacao());
        assertEquals(dataExpiracao, dto.getDataExpiracao());

        verify(mockServicoDominio, times(1)).buscaCodigoCurto(codigoObj);
        verify(mapeamentoMock, times(1)).isExpirado(any(LocalDateTime.class)); // Verifica se a expiração foi checada
    }

    @Test
    void obterInfoUrlPorCodigoCurto_QuandoEncontradoMasExpirado_DeveRetornarOptionalVazio() {
        // Arrange
        String codigoStr = "InfoExp";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        LocalDateTime dataExpiracaoPassada = LocalDateTime.now().minusDays(1);
        MapeamentoUrl mapeamentoExpirado = mock(MapeamentoUrl.class);

        when(mapeamentoExpirado.isExpirado(any(LocalDateTime.class))).thenReturn(true); // Mock para expirado
        when(mockServicoDominio.buscaCodigoCurto(codigoObj)).thenReturn(Optional.of(mapeamentoExpirado));

        // Act
        Optional<DtoUrlEncurtada> resultadoOpt = aplicacaoService.obterInfoUrlPorCodigoCurto(codigoStr);

        // Assert
        assertFalse(resultadoOpt.isPresent(), "Não deveria retornar DTO para URL expirada.");
        verify(mockServicoDominio, times(1)).buscaCodigoCurto(codigoObj);
        verify(mapeamentoExpirado, times(1)).isExpirado(any(LocalDateTime.class));
    }

    @Test
    void obterInfoUrlPorCodigoCurto_QuandoNaoEncontrado_DeveRetornarOptionalVazio() {
        // Arrange
        String codigoStr = "InfoNotFound";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        when(mockServicoDominio.buscaCodigoCurto(codigoObj)).thenReturn(Optional.empty());

        // Act
        Optional<DtoUrlEncurtada> resultadoOpt = aplicacaoService.obterInfoUrlPorCodigoCurto(codigoStr);

        // Assert
        assertFalse(resultadoOpt.isPresent());
        verify(mockServicoDominio, times(1)).buscaCodigoCurto(codigoObj);
    }

    // --- Testes para o método redirecionarEIncrementarAcesso ---

    @Test
    void redirecionarEIncrementarAcesso_QuandoEncontradoENaoExpirado_DeveIncrementarSalvarERetornarUrl() {
        // Arrange
        String codigoStr = "RedirOk";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        UrlOriginal urlObj = new UrlOriginal("http://destination.ok");
        LocalDateTime dataExpiracaoFutura = LocalDateTime.now().plusDays(1);
        // Usar um objeto real aqui para poder chamar registrarAcesso nele.
        MapeamentoUrl mapeamentoReal = spy(new MapeamentoUrl(codigoObj, urlObj, dataExpiracaoFutura));

        when(mockServicoDominio.buscaCodigoCurto(codigoObj)).thenReturn(Optional.of(mapeamentoReal));
        // Mocka o salvar que é chamado pelo serviço de aplicação através do serviço de domínio
        when(mockServicoDominio.salvar(mapeamentoReal)).thenReturn(mapeamentoReal);

        // Act
        Optional<String> resultadoOpt = aplicacaoService.redirecionarEIncrementarAcesso(codigoStr);

        // Assert
        assertTrue(resultadoOpt.isPresent(), "Deveria retornar a URL original.");
        assertEquals(urlObj.getValor(), resultadoOpt.get());

        // Verifica se isExpirado foi chamado (implicitamente pelo spy ou explicitamente se mockado)
        verify(mapeamentoReal).isExpirado(any(LocalDateTime.class));
        // Verifica se registrarAcesso foi chamado no objeto real
        verify(mapeamentoReal).registrarAcesso();
        // Verifica se o serviço de domínio foi chamado para salvar o mapeamento atualizado
        verify(mockServicoDominio).salvar(mapeamentoReal);
        // Verifica se o contador foi realmente incrementado (já que usamos um objeto real/spy)
        assertEquals(1L, mapeamentoReal.getContadorAcessos());
    }

    @Test
    void redirecionarEIncrementarAcesso_QuandoEncontradoMasExpirado_DeveRetornarOptionalVazioENaoSalvar() {
        // Arrange
        String codigoStr = "RedirExp";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        UrlOriginal urlObj = new UrlOriginal("http://destination.exp");
        MapeamentoUrl mapeamentoExpirado = spy(new MapeamentoUrl(codigoObj, urlObj, LocalDateTime.now().minusDays(1)));

        when(mockServicoDominio.buscaCodigoCurto(codigoObj)).thenReturn(Optional.of(mapeamentoExpirado));

        Optional<String> resultadoOpt = aplicacaoService.redirecionarEIncrementarAcesso(codigoStr);

        // Assert
        assertFalse(resultadoOpt.isPresent(), "Não deveria retornar URL para mapeamento expirado.");

        // Verifica se isExpirado foi chamado
        verify(mapeamentoExpirado).isExpirado(any(LocalDateTime.class));
        // Verifica que registrarAcesso NÃO foi chamado
        verify(mapeamentoExpirado, never()).registrarAcesso();
        // Verifica que salvar NÃO foi chamado
        verify(mockServicoDominio, never()).salvar(any(MapeamentoUrl.class));
        // Verifica que o contador permaneceu zero
        assertEquals(0L, mapeamentoExpirado.getContadorAcessos());
    }

    @Test
    void redirecionarEIncrementarAcesso_QuandoNaoEncontrado_DeveRetornarOptionalVazio() {
        // Arrange
        String codigoStr = "RedirNotFound";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        when(mockServicoDominio.buscaCodigoCurto(codigoObj)).thenReturn(Optional.empty());

        // Act
        Optional<String> resultadoOpt = aplicacaoService.redirecionarEIncrementarAcesso(codigoStr);

        // Assert
        assertFalse(resultadoOpt.isPresent());
        verify(mockServicoDominio, never()).salvar(any(MapeamentoUrl.class)); // Salvar não deve ser chamado.
    }
}