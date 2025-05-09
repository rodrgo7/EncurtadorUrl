package oliveiradev.encurtador_url.domain.model;

import org.springframework.util.Assert;
import java.util.Objects;

public final class UrlOriginal {
    private final String valor;

    public UrlOriginal(String valor) {
        Assert.hasText(valor, "o valor da URL original não pode ser nulo ou vazio");
        this.valor = valor;
    }

    public String getValor()  {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlOriginal that = (UrlOriginal) o;

        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public  String toString() {
        return valor;
    }
}
