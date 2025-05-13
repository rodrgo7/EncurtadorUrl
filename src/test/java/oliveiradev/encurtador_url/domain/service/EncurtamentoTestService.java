package oliveiradev.encurtador_url.domain.service;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.domain.repository.MapeamentoUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncurtamentoServiceTest {

    @Mock
    private MapeamentoUrlRepository mockMapeamentoUrlRepository;

    @Mock
    private CodigoCurtoService mockCodigoCurtoService;

    @InjectMocks
    private EncurtamentoService encurtamentoService;

    private UrlOriginal urlOriginalPadrao;
    private CodigoCurto codigoCurtoGerado;
    private LocalDateTime dataExpiracaoPadrao;

    private static final int MAX_TENTATIVAS_ESPERADAS = 5;

    @BeforeEach
    void setUp() {
        urlOriginalPadrao = new UrlOriginal("[https://www.muito-longa-url.com/para/encurtar](https://www.muito-longa-url.com/para/encurtar)");
        codigoCurtoGerado = new CodigoCurto("GenCd1");
        dataExpiracaoPadrao = LocalDateTime.now().plusDays(7);
    }

    @Test
    void encurtar_ComUrlNovaEDataExpiracao_DeveGerarCodigoSalvarERetornarMapeamento() {
        when(mockCodigoCurtoService.gerar()).thenReturn(codigoCurtoGerado);
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoCurtoGerado)).thenReturn(false);
        when(mockMapeamentoUrlRepository.salvar(any(MapeamentoUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MapeamentoUrl resultado = encurtamentoService.encurtar(urlOriginalPadrao, dataExpiracaoPadrao);

        assertNotNull(resultado);
        assertEquals(codigoCurtoGerado, resultado.getCodigoCurto());
        assertEquals(urlOriginalPadrao, resultado.getUrlOriginal());
        assertEquals(dataExpiracaoPadrao, resultado.getDataExpiracao());

        verify(mockCodigoCurtoService, times(1)).gerar();
        verify(mockMapeamentoUrlRepository, times(1)).existeCodigoCurto(codigoCurtoGerado);
        verify(mockMapeamentoUrlRepository, times(1)).salvar(any(MapeamentoUrl.class));
    }

    @Test
    void encurtar_ComMaximoDeTentativasDeColisaoAtingido_DeveLancarIllegalStateException() {
        when(mockCodigoCurtoService.gerar()).thenReturn(codigoCurtoGerado);
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoCurtoGerado)).thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            encurtamentoService.encurtar(urlOriginalPadrao, dataExpiracaoPadrao);
        });

        String mensagemEsperada = String.format(
                "Não foi possível gerar um código curto único para a URL '%s' após %d tentativas.",
                urlOriginalPadrao.getValor(), MAX_TENTATIVAS_ESPERADAS
        );
        // A mensagem no seu EncurtamentoService é "Não foi possivel..."
        // Ajustando para corresponder:
        mensagemEsperada = String.format(
                "Não foi possivel gerar um código curto único para a URL '%s' após %d tentativas.",
                urlOriginalPadrao.getValor(), MAX_TENTATIVAS_ESPERADAS
        );
        assertEquals(mensagemEsperada, exception.getMessage());

        verify(mockCodigoCurtoService, times(MAX_TENTATIVAS_ESPERADAS)).gerar();
        verify(mockMapeamentoUrlRepository, times(MAX_TENTATIVAS_ESPERADAS)).existeCodigoCurto(codigoCurtoGerado);
        verify(mockMapeamentoUrlRepository, never()).salvar(any(MapeamentoUrl.class));
    }

    @Test
    void buscarPorCodigoCurto_QuandoCodigoExiste_DeveRetornarOptionalComMapeamento() {
        MapeamentoUrl mapeamentoEsperado = new MapeamentoUrl(codigoCurtoGerado, urlOriginalPadrao, dataExpiracaoPadrao);
        // Ajuste no nome do método mockado para corresponder à interface do repositório
        when(mockMapeamentoUrlRepository.buscarPorCodigoCurto(codigoCurtoGerado)).thenReturn(Optional.of(mapeamentoEsperado));

        // Ajuste no nome do método do serviço de domínio sendo testado
        Optional<MapeamentoUrl> resultado = encurtamentoService.buscarPorCodigoCurto(codigoCurtoGerado);

        assertTrue(resultado.isPresent());
        assertEquals(mapeamentoEsperado, resultado.get());
        verify(mockMapeamentoUrlRepository, times(1)).buscarPorCodigoCurto(codigoCurtoGerado);
    }

    @Test
    void salvar_DeveChamarRepositorioSalvarERetornarMapeamento() {
        MapeamentoUrl mapeamentoParaSalvar = new MapeamentoUrl(codigoCurtoGerado, urlOriginalPadrao, dataExpiracaoPadrao);
        when(mockMapeamentoUrlRepository.salvar(mapeamentoParaSalvar)).thenReturn(mapeamentoParaSalvar);

        MapeamentoUrl resultado = encurtamentoService.salvar(mapeamentoParaSalvar);

        assertEquals(mapeamentoParaSalvar, resultado);
        verify(mockMapeamentoUrlRepository, times(1)).salvar(mapeamentoParaSalvar);
    }
}