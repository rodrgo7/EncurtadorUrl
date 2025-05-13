package oliveiradev.encurtador_url.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class EncurtarUrlHttpRequest {
    @NotBlank(message = "A URL não pode ser vazia ou nula.")
    @URL(message = "O formato da URL fornecida é inválido.")
    private String url;

    @Min(value = 1, message = "O TTL (ttlEmMinutos), se especificado, deve ser de no mínimo 1 minuto.")
    private Long ttlEmMinutos;

    public EncurtarUrlHttpRequest() {}

    public EncurtarUrlHttpRequest(String url, Long ttlEmMinutos) {
        this.url = url;
        this.ttlEmMinutos = ttlEmMinutos;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Long getTtlEmMinutos() { return ttlEmMinutos; }
    public void setTtlEmMinutos(Long ttlEmMinutos) { this.ttlEmMinutos = ttlEmMinutos; }
}
