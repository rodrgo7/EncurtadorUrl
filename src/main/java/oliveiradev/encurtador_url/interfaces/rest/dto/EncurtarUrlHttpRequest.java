package oliveiradev.encurtador_url.interfaces.rest.dto;

import jakarta.validation.constraints.Min;    // Para validar o TTL
import jakarta.validation.constraints.NotBlank; // Para validar a URL
import org.hibernate.validator.constraints.URL; // Validador de URL mais robusto

/**
 * Data Transfer Object (DTO) para a requisição HTTP de encurtamento de URL.
 * Contém a URL original a ser encurtada e um campo opcional para o
 * tempo de vida (TTL - Time To Live) em minutos.
 * As anotações de validação do Bean Validation são usadas para garantir
 * que os dados de entrada sejam válidos antes de serem processados.
 */
public class EncurtarUrlHttpRequest {

    @NotBlank(message = "A URL não pode ser vazia ou nula.")
    @URL(message = "O formato da URL fornecida é inválido.") // Validação de formato de URL
    private String url;

    // Opcional: Tempo de vida da URL encurtada em minutos.
    // Se fornecido, deve ser um valor positivo.
    @Min(value = 1, message = "O TTL (ttlEmMinutos), se especificado, deve ser de no mínimo 1 minuto.")
    private Long ttlEmMinutos;

    /**
     * Construtor padrão necessário para a desserialização JSON (ex: pelo Jackson).
     */
    public EncurtarUrlHttpRequest() {
    }

    /**
     * Construtor para criar uma instância com dados.
     * @param url A URL original.
     * @param ttlEmMinutos O tempo de vida em minutos (opcional).
     */
    public EncurtarUrlHttpRequest(String url, Long ttlEmMinutos) {
        this.url = url;
        this.ttlEmMinutos = ttlEmMinutos;
    }

    // Getters e Setters para que o framework (ex: Jackson) possa (de)serializar o objeto.
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getTtlEmMinutos() {
        return ttlEmMinutos;
    }

    public void setTtlEmMinutos(Long ttlEmMinutos) {
        this.ttlEmMinutos = ttlEmMinutos;
    }
}

