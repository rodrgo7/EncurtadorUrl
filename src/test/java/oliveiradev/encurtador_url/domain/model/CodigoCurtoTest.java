package oliveiradev.encurtador_url.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodigoCurtoTest {
    @Test
    void construtor_ComValorValido_DeveCriarInstancia() {
        String codigoValido = "aBcDeFg";
        CodigoCurto codigoCurto = new CodigoCurto(codigoValido);
        assertNotNull(codigoCurto);
        assertEquals(codigoValido, codigoCurto.getValor());
    }

    @Test
    void construtor_ComValorNulo_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CodigoCurto(null);
        });
        assertTrue(exception.getMessage().contains("não pode ser nulo ou vazio"));
    }

    @Test
    void construtor_ComValorVazio_DeveLancarIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CodigoCurto("");
        });
        assertTrue(exception.getMessage().contains("não pode ser nulo ou vazio"));
    }

    @Test
    void equals_ComMesmoValor_DeveRetornarTrue() {
        String valor = "xYz123";
        CodigoCurto codigo1 = new CodigoCurto(valor);
        CodigoCurto codigo2 = new CodigoCurto(valor);
        assertEquals(codigo1, codigo2);
    }

    @Test
    void hashCode_ComMesmoValor_DeveSerIgual() {
        String valor = "TestCode";
        CodigoCurto codigo1 = new CodigoCurto(valor);
        CodigoCurto codigo2 = new CodigoCurto(valor);
        assertEquals(codigo1.hashCode(), codigo2.hashCode());
    }

    @Test
    void toString_DeveRetornarOValorDoCodigo() {
        String valor = "MyCode";
        CodigoCurto codigo = new CodigoCurto(valor);
        assertEquals(valor, codigo.toString());
    }
}