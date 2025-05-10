package oliveiradev.encurtador_url.interfaces.dto;

import java.time.LocalDateTime;

public class DtoUrlEncurtada {
    private final String urlOriginal;
    private final String codigoCurto;
    private final String urlEncurtadaCompleta;
    private final long acessos;
    private final LocalDateTime dataCriacao;
    private final LocalDateTime dataExpiracao;

    public DtoUrlEncurtada(String urlOriginal, String codigoCurto, String urlEncurtadaCompleta,
                           long acessos, LocalDateTime dataCriacao, LocalDateTime dataExpiracao) {
        this.urlOriginal = urlOriginal;
        this.codigoCurto = codigoCurto;
        this.urlEncurtadaCompleta = urlEncurtadaCompleta;
        this.acessos = acessos;
        this.dataCriacao = dataCriacao;
        this.dataExpiracao = dataExpiracao;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public String getCodigoCurto() {
        return codigoCurto;
    }

    public String getUrlEncurtadaCompleta() {
        return urlEncurtadaCompleta;
    }

    public long getAcessos() {
        return acessos;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }
} 