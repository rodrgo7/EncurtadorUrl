package oliveiradev.encurtador_url.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class UrlOriginalTest {

    @Test
    void construtor_ComValorValido_DeveCriarInstancia() {
        String urlValida = "https://www.example.com/path?query=value#fragment";
        UrlOriginal urlOriginal = new UrlOriginal(urlValida);
        assertNotNull(urlOriginal);
        assertEquals(urlValida, urlOriginal.getValor());
    }

    @Test
    void construtor_ComValorNulo_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UrlOriginal(null);
        });
        // A mensagem exata pode variar um pouco dependendo da sua implementação de Assert.hasText
        // Ajuste se a mensagem no seu UrlOriginal.java for diferente
        assertTrue(exception.getMessage().contains("não pode ser nulo ou vazio"));
    }

    @Test
    void construtor_ComValorVazio_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UrlOriginal("");
        });
        assertTrue(exception.getMessage().contains("não pode ser nulo ou vazio"));
    }

    @Test
    void construtor_ComValorApenasComEspacos_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new UrlOriginal("   ");
        });
        assertTrue(exception.getMessage().contains("não pode ser nulo ou vazio"));
    }

    @Test
    void equals_ComMesmoValor_DeveRetornarTrue() {
        String valor = "http://test.com";
        UrlOriginal url1 = new UrlOriginal(valor);
        UrlOriginal url2 = new UrlOriginal(valor);
        assertEquals(url1, url2, "UrlOriginal com o mesmo valor devem ser iguais.");
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