package oliveiradev.encurtador_url.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL) // Opcional: não inclui campos com valor nulo no JSON de resposta (ex: dataExpiracao)
public class EncurtarUrlHttpResponse {

    private String urlEncurtada;
    private String urlOriginal;
    private long acessos;

    // Formata a data e hora no padrão ISO 8601 para a resposta JSON.
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataExpiracao; // Será nulo se a URL não expirar.

    public EncurtarUrlHttpResponse() {
    }

    public EncurtarUrlHttpResponse(String urlEncurtada, String urlOriginal, long acessos,
                                   LocalDateTime dataCriacao, LocalDateTime dataExpiracao) {
        this.urlEncurtada = urlEncurtada;
        this.urlOriginal = urlOriginal;
        this.acessos = acessos;
        this.dataCriacao = dataCriacao;
        this.dataExpiracao = dataExpiracao;
    }

    // Getters e Setters
    public String getUrlEncurtada() {
        return urlEncurtada;
    }

    public void setUrlEncurtada(String urlEncurtada) {
        this.urlEncurtada = urlEncurtada;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public void setUrlOriginal(String urlOriginal) {
        this.urlOriginal = urlOriginal;
    }

    public long getAcessos() {
        return acessos;
    }

    public void setAcessos(long acessos) {
        this.acessos = acessos;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }
}