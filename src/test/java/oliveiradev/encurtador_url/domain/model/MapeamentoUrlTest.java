package oliveiradev.encurtador_url.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MapeamentoUrlTest {
    private CodigoCurto codigoCurtoPadrao;
    private UrlOriginal urlOriginalPadrao;

    @BeforeEach
    void setUp() {
        codigoCurtoPadrao = new CodigoCurto("TestCD");
        urlOriginalPadrao = new UrlOriginal("https://example.com/test");
    }

    @Test
    void construtor_ComDadosValidosSemExpiracao_DeveInicializarCorretamente() {
        // Usa o construtor que não recebe dataCriacao como parâmetro
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);

        assertNotNull(mapeamento);
        assertEquals(codigoCurtoPadrao, mapeamento.getCodigoCurto());
        assertEquals(urlOriginalPadrao, mapeamento.getUrlOriginal());
        assertNotNull(mapeamento.getDataCriacao(), "Data de criação não deve ser nula.");
        assertNull(mapeamento.getDataExpiracao(), "Data de expiração deve ser nula se não fornecida.");
        assertEquals(0L, mapeamento.getContadorAcessos(), "Contador de acessos deve iniciar em zero.");
    }

    @Test
    void construtor_ComDadosValidosComExpiracao_DeveInicializarCorretamente() {
        LocalDateTime dataExpiracao = LocalDateTime.now().plusDays(5);
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, dataExpiracao);

        assertNotNull(mapeamento);
        assertEquals(dataExpiracao, mapeamento.getDataExpiracao(), "Data de expiração deve ser a fornecida.");
    }

    @Test
    void registrarAcesso_DeveIncrementarContadorDeAcessos() {
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);
        mapeamento.registrarAcesso();
        assertEquals(1L, mapeamento.getContadorAcessos());
    }

    @Test
    void isExpirado_ComDataExpiracaoNula_DeveRetornarFalse() {
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);
        assertFalse(mapeamento.isExpirado(LocalDateTime.now()));
    }

    @Test
    void isExpirado_ComDataExpiracaoNoFuturo_DeveRetornarFalse() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataExpiracaoFutura = agora.plusHours(1);
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, dataExpiracaoFutura);
        assertFalse(mapeamento.isExpirado(agora));
    }

    @Test
    void isExpirado_ComDataExpiracaoNoPassado_DeveRetornarTrue() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataExpiracaoPassada = agora.minusHours(1);
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, dataExpiracaoPassada);
        assertTrue(mapeamento.isExpirado(agora));
    }
}