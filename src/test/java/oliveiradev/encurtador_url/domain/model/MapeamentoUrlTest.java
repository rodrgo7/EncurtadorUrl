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
        urlOriginalPadrao = new UrlOriginal("https://exemplo.com/test");
    }

    @Test
    void construtor_ComDadosValidosSemExpiracao_DeveInicializarCorretamente() {
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);

        assertNotNull(mapeamento);
        assertEquals(codigoCurtoPadrao, mapeamento.getCodigoCurto());
        assertEquals(urlOriginalPadrao, mapeamento.getUrlOriginal());
        assertNotNull(mapeamento.getDataCriacao(), "Data de criação não deve ser nula.");
        assertNull(mapeamento.getDataExpiracao(), "Data de expiração deve ser nula se não fornecida.");
        assertEquals(0L, mapeamento.getContadorAcessos(), "Contador de acessos deve iniciar em zero.");
        assertNull(mapeamento.getId(), "ID deve ser nulo antes da persistência.");
    }

    @Test
    void construtor_ComDadosValidosComExpiracao_DeveInicializarCorretamente() {
        LocalDateTime dataExpiracao = LocalDateTime.now().plusDays(5);
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, dataExpiracao);

        assertNotNull(mapeamento);
        assertEquals(dataExpiracao, mapeamento.getDataExpiracao(), "Data de expiração deve ser a fornecida.");
    }

    @Test
    void construtor_ComCodigoCurtoNulo_DeveLancarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MapeamentoUrl(null, urlOriginalPadrao, null);
        }, "Código curto nulo deve lançar exceção.");
    }

    @Test
    void construtor_ComUrlOriginalNula_DeveLancarIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MapeamentoUrl(codigoCurtoPadrao, null, null);
        }, "URL original nula deve lançar exceção.");
    }

    @Test
    void registrarAcesso_DeveIncrementarContadorDeAcessos() {
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);
        assertEquals(0L, mapeamento.getContadorAcessos());

        mapeamento.registrarAcesso();
        assertEquals(1L, mapeamento.getContadorAcessos(), "Contador deve ser 1 após o primeiro acesso.");

        mapeamento.registrarAcesso();
        assertEquals(2L, mapeamento.getContadorAcessos(), "Contador deve ser 2 após o segundo acesso.");
    }

    @Test
    void isExpirado_ComDataExpiracaoNula_DeveRetornarFalse() {
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);
        assertFalse(mapeamento.isExpirado(LocalDateTime.now()), "Mapeamento sem data de expiração não deve expirar.");
    }

    @Test
    void isExpirado_ComDataExpiracaoNoFuturo_DeveRetornarFalse() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataExpiracaoFutura = agora.plusHours(1);
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, dataExpiracaoFutura);
        assertFalse(mapeamento.isExpirado(agora), "Mapeamento com data de expiração no futuro não deve estar expirado.");
    }

    @Test
    void isExpirado_ComDataExpiracaoNoPassado_DeveRetornarTrue() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataExpiracaoPassada = agora.minusHours(1);
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, dataExpiracaoPassada);
        assertTrue(mapeamento.isExpirado(agora), "Mapeamento com data de expiração no passado deve estar expirado.");
    }

    @Test
    void isExpirado_ComDataExpiracaoIgualAgora_DeveRetornarFalsePelaLogicaIsAfter() {
        // A lógica é `dataHoraAtual.isAfter(this.dataExpiracao)`.
        // Se dataHoraAtual == dataExpiracao, isAfter() retorna false.
        LocalDateTime agora = LocalDateTime.now();
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, agora);
        assertFalse(mapeamento.isExpirado(agora), "Mapeamento com data de expiração igual agora não deve estar expirado (lógica isAfter).");
    }

    @Test
    void isExpirado_ComDataHoraAtualNula_DeveLancarIllegalArgumentException() {
        MapeamentoUrl mapeamento = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> {
            mapeamento.isExpirado(null);
        }, "Verificar expiração com data/hora atual nula deve lançar exceção.");
    }

    @Test
    void equals_ComMesmoId_DeveRetornarTrue() {
        MapeamentoUrl map1 = new MapeamentoUrl(new CodigoCurto("C1"), new UrlOriginal("U1"), null);
        // Simular que o ID foi setado pela persistência (usando reflection para este teste, não ideal para produção)
        try {
            java.lang.reflect.Field idField = MapeamentoUrl.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(map1, "id-123");
        } catch (Exception e) { fail("Falha ao setar ID para teste de equals"); }

        MapeamentoUrl map2 = new MapeamentoUrl(new CodigoCurto("C2"), new UrlOriginal("U2"), null);
        try {
            java.lang.reflect.Field idField = MapeamentoUrl.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(map2, "id-123"); // Mesmo ID
        } catch (Exception e) { fail("Falha ao setar ID para teste de equals"); }

        assertEquals(map1, map2, "Mapeamentos com o mesmo ID devem ser iguais.");
    }

    @Test
    void equals_ComIdsDiferentes_DeveRetornarFalse() {
        MapeamentoUrl map1 = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);
        try {
            java.lang.reflect.Field idField = MapeamentoUrl.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(map1, "id-1");
        } catch (Exception e) { fail("Falha ao setar ID para teste de equals"); }


        MapeamentoUrl map2 = new MapeamentoUrl(codigoCurtoPadrao, urlOriginalPadrao, null);
        try {
            java.lang.reflect.Field idField = MapeamentoUrl.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(map2, "id-2"); // ID diferente
        } catch (Exception e) { fail("Falha ao setar ID para teste de equals"); }

        assertNotEquals(map1, map2, "Mapeamentos com IDs diferentes não devem ser iguais.");
    }

    @Test
    void equals_SemIdsMasComMesmoCodigoCurto_DeveRetornarTrue() {
        // Testando a lógica de fallback para codigoCurto quando IDs são nulos.
        CodigoCurto mesmoCodigo = new CodigoCurto("UniqueCD");
        MapeamentoUrl map1 = new MapeamentoUrl(mesmoCodigo, new UrlOriginal("U1"), null);
        MapeamentoUrl map2 = new MapeamentoUrl(mesmoCodigo, new UrlOriginal("U2"), null); // URL original diferente, mas mesmo código

        assertEquals(map1, map2, "Mapeamentos sem IDs mas com mesmo código curto devem ser iguais.");
    }

    @Test
    void equals_SemIdsComCodigosCurtosDiferentes_DeveRetornarFalse() {
        MapeamentoUrl map1 = new MapeamentoUrl(new CodigoCurto("CD_A"), urlOriginalPadrao, null);
        MapeamentoUrl map2 = new MapeamentoUrl(new CodigoCurto("CD_B"), urlOriginalPadrao, null);
        assertNotEquals(map1, map2);
    }

    @Test
    void hashCode_ConsistenciaComEquals() {
        // Se map1.equals(map2) é true, então map1.hashCode() == map2.hashCode() deve ser true.
        MapeamentoUrl map1ComId = new MapeamentoUrl(new CodigoCurto("C1"), new UrlOriginal("U1"), null);
        MapeamentoUrl map2ComId = new MapeamentoUrl(new CodigoCurto("C2"), new UrlOriginal("U2"), null);
        try {
            java.lang.reflect.Field idField = MapeamentoUrl.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(map1ComId, "id-hash");
            idField.set(map2ComId, "id-hash");
        } catch (Exception e) { fail("Falha ao setar ID para teste de hashCode"); }
        assertEquals(map1ComId.hashCode(), map2ComId.hashCode(), "HashCodes devem ser iguais para Mapeamentos com mesmo ID.");

        CodigoCurto mesmoCodigo = new CodigoCurto("HashCD");
        MapeamentoUrl map1SemId = new MapeamentoUrl(mesmoCodigo, new UrlOriginal("U1"), null);
        MapeamentoUrl map2SemId = new MapeamentoUrl(mesmoCodigo, new UrlOriginal("U2"), null);
        assertEquals(map1SemId.hashCode(), map2SemId.hashCode(), "HashCodes devem ser iguais para Mapeamentos sem ID mas com mesmo código curto.");
    }
}