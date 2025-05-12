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
public class EncurtamentoTestService {
    @Mock // Cria um mock para MapeamentoUrlRepository.
    private MapeamentoUrlRepository mockMapeamentoUrlRepository;

    @Mock // Cria um mock para CodigoCurtoService.
    private CodigoCurtoService mockEstrategiaGeracao;

    @InjectMocks // Cria uma instância de ServicoDominioEncurtamento e injeta os mocks acima nela.
    private EncurtamentoService servicoDominio;

    private UrlOriginal urlOriginalPadrao;
    private CodigoCurto codigoCurtoGerado;
    private LocalDateTime dataExpiracaoPadrao;

    // Constante para espelhar a do serviço, facilitando a manutenção do teste.
    private static final int MAX_TENTATIVAS_ESPERADAS = 5;

    @BeforeEach
    void setUp() {
        urlOriginalPadrao = new UrlOriginal("https://www.muito-longa-url.com/para/encurtar");
        codigoCurtoGerado = new CodigoCurto("GenCd1");
        dataExpiracaoPadrao = LocalDateTime.now().plusDays(7);
    }

    @Test
    void encurtar_ComUrlNovaEDataExpiracao_DeveGerarCodigoSalvarERetornarMapeamento() {
        // Configuração do Mock (Arrange)
        // Quando a estratégia gerar() for chamada, retorne codigoCurtoGerado.
        when(mockEstrategiaGeracao.gerar()).thenReturn(codigoCurtoGerado);
        // Quando existeCodigoCurto() for chamado com codigoCurtoGerado, retorne false (código é único).
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoCurtoGerado)).thenReturn(false);

        // Configura o mock para retornar o objeto que foi passado para o método salvar.
        // Isso simula o comportamento de um save bem-sucedido que retorna a entidade persistida.
        when(mockMapeamentoUrlRepository.salvar(any(MapeamentoUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ação (Act)
        MapeamentoUrl resultado = servicoDominio.encurtar(urlOriginalPadrao, dataExpiracaoPadrao);

        // Verificação (Assert)
        assertNotNull(resultado, "O resultado do encurtamento não deve ser nulo.");
        assertEquals(codigoCurtoGerado, resultado.getCodigoCurto(), "O código curto no resultado deve ser o gerado.");
        assertEquals(urlOriginalPadrao, resultado.getUrlOriginal(), "A URL original no resultado deve ser a fornecida.");
        assertEquals(dataExpiracaoPadrao, resultado.getDataExpiracao(), "A data de expiração no resultado deve ser a fornecida.");

        // Verifica se os métodos dos mocks foram chamados como esperado.
        verify(mockEstrategiaGeracao, times(1)).gerar(); // gerar() deve ser chamado uma vez.
        verify(mockMapeamentoUrlRepository, times(1)).existeCodigoCurto(codigoCurtoGerado); // existeCodigoCurto() chamado uma vez.
        verify(mockMapeamentoUrlRepository, times(1)).salvar(any(MapeamentoUrl.class)); // salvar() chamado uma vez.
    }

    @Test
    void encurtar_ComUrlNovaSemDataExpiracao_DeveSalvarComExpiracaoNula() {
        when(mockEstrategiaGeracao.gerar()).thenReturn(codigoCurtoGerado);
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoCurtoGerado)).thenReturn(false);

        // Captura o argumento passado para o método salvar para inspeção.
        ArgumentCaptor<MapeamentoUrl> mapeamentoCaptor = ArgumentCaptor.forClass(MapeamentoUrl.class);
        when(mockMapeamentoUrlRepository.salvar(mapeamentoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        servicoDominio.encurtar(urlOriginalPadrao, null); // dataExpiracao é nula

        MapeamentoUrl mapeamentoSalvo = mapeamentoCaptor.getValue();
        assertNull(mapeamentoSalvo.getDataExpiracao(), "Data de expiração deve ser nula no objeto salvo.");
    }

    @Test
    void encurtar_ComColisaoDeCodigoNaPrimeiraTentativa_DeveTentarNovamenteComSucesso() {
        CodigoCurto codigoColisao = new CodigoCurto("COLID3");
        CodigoCurto codigoSucesso = new CodigoCurto("SUCES0");

        // Configura a estratégia para retornar o código de colisão na primeira chamada, e o de sucesso na segunda.
        when(mockEstrategiaGeracao.gerar()).thenReturn(codigoColisao).thenReturn(codigoSucesso);
        // Configura o repositório para indicar que o primeiro código existe, e o segundo não.
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoColisao)).thenReturn(true);
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoSucesso)).thenReturn(false);
        when(mockMapeamentoUrlRepository.salvar(any(MapeamentoUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MapeamentoUrl resultado = servicoDominio.encurtar(urlOriginalPadrao, dataExpiracaoPadrao);

        assertNotNull(resultado);
        assertEquals(codigoSucesso, resultado.getCodigoCurto(), "Deve usar o código da segunda tentativa (sucesso).");

        verify(mockEstrategiaGeracao, times(2)).gerar(); // gerar() chamado duas vezes.
        verify(mockMapeamentoUrlRepository, times(1)).existeCodigoCurto(codigoColisao);
        verify(mockMapeamentoUrlRepository, times(1)).existeCodigoCurto(codigoSucesso);
        verify(mockMapeamentoUrlRepository, times(1)).salvar(any(MapeamentoUrl.class));
    }

    @Test
    void encurtar_ComMaximoDeTentativasDeColisaoAtingido_DeveLancarIllegalStateException() {
        // Configura a estratégia para sempre gerar o mesmo código.
        when(mockEstrategiaGeracao.gerar()).thenReturn(codigoCurtoGerado);
        // Configura o repositório para sempre indicar que o código gerado já existe.
        when(mockMapeamentoUrlRepository.existeCodigoCurto(codigoCurtoGerado)).thenReturn(true);

        // Ação e Verificação da Exceção
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            servicoDominio.encurtar(urlOriginalPadrao, dataExpiracaoPadrao);
        });

        String mensagemEsperada = String.format(
                "Não foi possível gerar um código curto único para a URL '%s' após %d tentativas.",
                urlOriginalPadrao.getValor(), MAX_TENTATIVAS_ESPERADAS
        );
        assertEquals(mensagemEsperada, exception.getMessage(), "Mensagem de exceção incorreta.");

        // Verifica se houve o número máximo de tentativas.
        verify(mockEstrategiaGeracao, times(MAX_TENTATIVAS_ESPERADAS)).gerar();
        verify(mockMapeamentoUrlRepository, times(MAX_TENTATIVAS_ESPERADAS)).existeCodigoCurto(codigoCurtoGerado);
        verify(mockMapeamentoUrlRepository, never()).salvar(any(MapeamentoUrl.class)); // Salvar nunca deve ser chamado.
    }

    @Test
    void buscarPorCodigoCurto_QuandoCodigoExiste_DeveRetornarOptionalComMapeamento() {
        MapeamentoUrl mapeamentoEsperado = new MapeamentoUrl(codigoCurtoGerado, urlOriginalPadrao, dataExpiracaoPadrao);
        when(mockMapeamentoUrlRepository.buscaCodigoCurto(codigoCurtoGerado)).thenReturn(Optional.of(mapeamentoEsperado));

        Optional<MapeamentoUrl> resultado = servicoDominio.buscaCodigoCurto(codigoCurtoGerado);

        assertTrue(resultado.isPresent(), "Deveria encontrar o mapeamento.");
        assertEquals(mapeamentoEsperado, resultado.get(), "O mapeamento retornado não é o esperado.");
        verify(mockMapeamentoUrlRepository, times(1)).buscaCodigoCurto(codigoCurtoGerado);
    }

    @Test
    void buscarPorCodigoCurto_QuandoCodigoNaoExiste_DeveRetornarOptionalVazio() {
        when(mockMapeamentoUrlRepository.buscaCodigoCurto(codigoCurtoGerado)).thenReturn(Optional.empty());

        Optional<MapeamentoUrl> resultado = servicoDominio.buscaCodigoCurto(codigoCurtoGerado);

        assertFalse(resultado.isPresent(), "Não deveria encontrar o mapeamento.");
        verify(mockMapeamentoUrlRepository, times(1)).buscaCodigoCurto(codigoCurtoGerado);
    }

    @Test
    void salvar_DeveChamarRepositorioSalvarERetornarMapeamento() {
        MapeamentoUrl mapeamentoParaSalvar = new MapeamentoUrl(codigoCurtoGerado, urlOriginalPadrao, dataExpiracaoPadrao);
        when(mockMapeamentoUrlRepository.salvar(mapeamentoParaSalvar)).thenReturn(mapeamentoParaSalvar);

        MapeamentoUrl resultado = servicoDominio.salvar(mapeamentoParaSalvar);

        assertEquals(mapeamentoParaSalvar, resultado, "O mapeamento retornado pelo salvar não é o esperado.");
        verify(mockMapeamentoUrlRepository, times(1)).salvar(mapeamentoParaSalvar);
    }
}

