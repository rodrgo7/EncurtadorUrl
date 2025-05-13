package oliveiradev.encurtador_url.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class ComandoEncurtadorUrl {
    @NotBlank(message = "A URL original não pode ser nula ou vazia.")
    @URL(message = "O formato da URL original é inválido.")
    private final String urlOriginal;

    @Min(value = 1, message = "O TTL (ttlEmMinutos), se fornecido, deve ser de no mínimo 1 minuto.")
    private final Long ttlEmMinutos;

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