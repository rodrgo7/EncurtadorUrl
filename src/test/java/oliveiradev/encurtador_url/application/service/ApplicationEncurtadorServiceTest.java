package oliveiradev.encurtador_url.application.service;

import oliveiradev.encurtador_url.application.dto.ComandoEncurtadorUrl;
import oliveiradev.encurtador_url.application.dto.DtoUrlEncurtada;
import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.domain.service.EncurtamentoService; // Serviço de Domínio


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AplicacaoEncurtadorServiceTest { // Nome da classe de teste correto

    @Mock
    private EncurtamentoService mockEncurtamentoService; // Mock do serviço de domínio

    private final String BASE_URL_TESTE = "[http://test.short](http://test.short)";

    @InjectMocks
    private AplicacaoEncurtadorService aplicacaoEncurtadorService; // Serviço de aplicação sob teste

    @Captor
    private ArgumentCaptor<UrlOriginal> urlOriginalCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> dataExpiracaoCaptor;

    @BeforeEach
    void setUp() {
        // Recria a instância para garantir que a baseUrl de teste seja usada
        aplicacaoEncurtadorService = new AplicacaoEncurtadorService(mockEncurtamentoService, BASE_URL_TESTE);
    }

    @Test
    void encurtarUrl_ComComandoValidoETTL_DeveChamarDominioComDataCalculadaERetornarDto() {
        String urlOriginalStr = "[https://valida.com/com/ttl](https://valida.com/com/ttl)";
        Long ttlMinutos = 60L;
        ComandoEncurtadorUrl comando = new ComandoEncurtadorUrl(urlOriginalStr, ttlMinutos);

        CodigoCurto codigoGerado = new CodigoCurto("TTLcd1");
        LocalDateTime dataCriacao = LocalDateTime.now();
        MapeamentoUrl mapeamentoRetornadoPeloDominio = new MapeamentoUrl(
                codigoGerado, new UrlOriginal(urlOriginalStr), dataCriacao.plusMinutes(ttlMinutos) // Usa o construtor correto
        );
        when(mockEncurtamentoService.encurtar(urlOriginalCaptor.capture(), dataExpiracaoCaptor.capture()))
                .thenReturn(mapeamentoRetornadoPeloDominio);

        DtoUrlEncurtada resultadoDto = aplicacaoEncurtadorService.encurtarUrl(comando);

        assertNotNull(resultadoDto);
        assertEquals(urlOriginalStr, resultadoDto.getUrlOriginal());
        LocalDateTime dataExpiracaoEsperadaAprox = LocalDateTime.now().plusMinutes(ttlMinutos);
        LocalDateTime dataExpiracaoPassada = dataExpiracaoCaptor.getValue();
        assertNotNull(dataExpiracaoPassada);
        assertTrue(ChronoUnit.SECONDS.between(dataExpiracaoPassada, dataExpiracaoEsperadaAprox) < 5);
    }

    @Test
    void obterInfoUrlPorCodigoCurto_QuandoEncontradoENaoExpirado_DeveRetornarDto() {
        String codigoStr = "InfoOk";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        UrlOriginal urlObj = new UrlOriginal("[http://info.test](http://info.test)");
        LocalDateTime dataCriacao = LocalDateTime.now().minusDays(1);
        LocalDateTime dataExpiracao = LocalDateTime.now().plusDays(1);
        MapeamentoUrl mapeamentoMock = spy(new MapeamentoUrl(codigoObj, urlObj, dataExpiracao)); // Usa spy para chamar isExpirado real

        when(mapeamentoMock.isExpirado(any(LocalDateTime.class))).thenReturn(false);
        // Ajuste no nome do método mockado para corresponder ao serviço de domínio
        when(mockEncurtamentoService.buscarPorCodigoCurto(codigoObj)).thenReturn(Optional.of(mapeamentoMock));

        Optional<DtoUrlEncurtada> resultadoOpt = aplicacaoEncurtadorService.obterInfoUrlPorCodigoCurto(codigoStr);

        assertTrue(resultadoOpt.isPresent());
        assertEquals(urlObj.getValor(), resultadoOpt.get().getUrlOriginal());
        verify(mapeamentoMock).isExpirado(any(LocalDateTime.class));
    }

    @Test
    void redirecionarEIncrementarAcesso_QuandoEncontradoENaoExpirado_DeveIncrementarSalvarERetornarUrl() {
        String codigoStr = "RedirOk";
        CodigoCurto codigoObj = new CodigoCurto(codigoStr);
        UrlOriginal urlObj = new UrlOriginal("[http://destination.ok](http://destination.ok)");
        LocalDateTime dataExpiracaoFutura = LocalDateTime.now().plusDays(1);
        MapeamentoUrl mapeamentoReal = spy(new MapeamentoUrl(codigoObj, urlObj, dataExpiracaoFutura));

        when(mockEncurtamentoService.buscarPorCodigoCurto(codigoObj)).thenReturn(Optional.of(mapeamentoReal));
        when(mockEncurtamentoService.salvar(mapeamentoReal)).thenReturn(mapeamentoReal);

        Optional<String> resultadoOpt = aplicacaoEncurtadorService.redirecionarEIncrementarAcesso(codigoStr);

        assertTrue(resultadoOpt.isPresent());
        assertEquals(urlObj.getValor(), resultadoOpt.get());
        verify(mapeamentoReal).isExpirado(any(LocalDateTime.class));
        verify(mapeamentoReal).registrarAcesso();
        verify(mockEncurtamentoService).salvar(mapeamentoReal);
        assertEquals(1L, mapeamentoReal.getContadorAcessos());
    }
}
