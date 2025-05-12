package oliveiradev.encurtador_url.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlOriginalTest {
    @Test
    void construtor_ComValorValido_DeveCriarInstancia() {
        String urlValida = "https://www.exemplo.com/path?query=value#fragment";
        UrlOriginal urlOriginal = new UrlOriginal(urlValida);
        assertNotNull(urlOriginal);
        assertEquals(urlValida, urlOriginal.getValor());
    }

    @Test
    void construtor_ComValorNulo_DeveLancarIllegalArgumentException() {
        // Verifica se uma IllegalArgumentException é lançada quando o construtor é chamado com null.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UrlOriginal(null);
        });
        assertTrue(exception.getMessage().contains("O valor da URL original não pode ser nulo ou vazio."));
    }

    @Test
    void construtor_ComValorVazio_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UrlOriginal("");
        });
        assertTrue(exception.getMessage().contains("O valor da URL original não pode ser nulo ou vazio."));
    }

    @Test
    void construtor_ComValorApenasComEspacos_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UrlOriginal("   ");
        });
        assertTrue(exception.getMessage().contains("O valor da URL original não pode ser nulo ou vazio."));
    }

    @Test
    void equals_ComMesmoValor_DeveRetornarTrue() {
        String valor = "http://test.com";
        UrlOriginal url1 = new UrlOriginal(valor);
        UrlOriginal url2 = new UrlOriginal(valor);
        assertEquals(url1, url2, "UrlOriginal com o mesmo valor devem ser iguais.");
    }

    @Test
    void equals_ComValoresDiferentes_DeveRetornarFalse() {
        UrlOriginal url1 = new UrlOriginal("http://test1.com");
        UrlOriginal url2 = new UrlOriginal("http://test2.com");
        assertNotEquals(url1, url2, "UrlOriginal com valores diferentes não devem ser iguais.");
    }

    @Test
    void equals_ComObjetoNulo_DeveRetornarFalse() {
        UrlOriginal url1 = new UrlOriginal("http://test.com");
        assertNotEquals(null, url1, "UrlOriginal não deve ser igual a nulo.");
    }

    @Test
    void equals_ComTipoDiferente_DeveRetornarFalse() {
        UrlOriginal url1 = new UrlOriginal("http://test.com");
        Object outroObjeto = new Object();
        assertNotEquals(url1, outroObjeto, "UrlOriginal não deve ser igual a um objeto de tipo diferente.");
    }

    @Test
    void hashCode_ComMesmoValor_DeveSerIgual() {
        String valor = "http://test.com/hashCode";
        UrlOriginal url1 = new UrlOriginal(valor);
        UrlOriginal url2 = new UrlOriginal(valor);
        assertEquals(url1.hashCode(), url2.hashCode(), "HashCodes de UrlOriginal com o mesmo valor devem ser iguais.");
    }

    @Test
    void toString_DeveRetornarOValorDaUrl() {
        String valor = "http://minha.url";
        UrlOriginal url = new UrlOriginal(valor);
        assertEquals(valor, url.toString(), "toString deve retornar o valor da URL.");
    }
}