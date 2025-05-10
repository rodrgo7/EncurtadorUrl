package oliveiradev.encurtador_url.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class ComandoEncurtadorUrl {
    @NotBlank(message = "A URL original não pode ser nula ou vazia.")
    @URL(message = "O formato da URL original é inválido.")
    private final String urlOriginal;

    // TTL (Time-To-Live) em minutos. Se nulo, a URL não expira ou usa um padrão do sistema.
    // A anotação @Min garante que, se um TTL for fornecido, ele seja positivo.
    @Min(value = 1, message = "O TTL (ttlEmMinutos), se fornecido, deve ser de no mínimo 1 minuto.")
    private final Long ttlEmMinutos; // Opcional: tempo de vida em minutos.}

    public ComandoEncurtadorUrl(String urlOriginal, Long ttlEmMinutos) {
        this.urlOriginal = urlOriginal;
        this.ttlEmMinutos = ttlEmMinutos;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public Long getTtlEmMinutos() {
        return ttlEmMinutos;
    }
}