package oliveiradev.encurtador_url.domain.model;

import org.springframework.util.Assert;

import java.util.Objects;

public class CodigoCurto {
    private final String valor;

    public CodigoCurto(String valor) {
        Assert.hasText(valor, "o valor do código curto não pode ser nulo ou vazio.");

        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodigoCurto that = (CodigoCurto) o;

        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return  Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
