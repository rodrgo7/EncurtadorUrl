package oliveiradev.encurtador_url.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "mapeamentos_url") // Nome definido para o MongoDB
public class MapeamentoUrl {
    @Id
    private String id;
    @Indexed(unique = true)
    private CodigoCurto codigoCurto;
    private UrlOriginal urlOriginal;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private long contadorAcessos;

    protected MapeamentoUrl() {
        this.contadorAcessos = 0L;
    }

    public MapeamentoUrl(CodigoCurto codigoCurto, UrlOriginal urlOriginal, LocalDateTime dataCriacao, LocalDateTime dataExpiracao) {
        Assert.notNull(codigoCurto, "O codigo curto não pode ser nulo");
        Assert.notNull(urlOriginal, "O URL original não pode ser nulo");

        this.codigoCurto = codigoCurto;
        this.urlOriginal = urlOriginal;
        this.dataCriacao = LocalDateTime.now();
        this.dataExpiracao = dataExpiracao;
        this.contadorAcessos = 0L;
    }

    public MapeamentoUrl(CodigoCurto novoCodCurto, UrlOriginal urlOriginal, LocalDateTime dataExpiracao) {
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public CodigoCurto getCodigoCurto() {
        return codigoCurto;
    }

    public UrlOriginal getUrlOriginal() {
        return urlOriginal;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public long getContadorAcessos() {
        return contadorAcessos;
    }

    public boolean isExpirado(LocalDateTime dataHoraAtual) {
        Assert.notNull(dataHoraAtual, "A data/hora atual para verificação de expiração não pode ser nula.");
        if (this.dataExpiracao == null) {
            return false; // Se não há data de expiração, nunca expira.
        }

        // Considera expirado se a data/hora atual for DEPOIS da data de expiração.
        return dataHoraAtual.isAfter(this.dataExpiracao);
    }

    public void registrarAcesso() {
        this.contadorAcessos++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapeamentoUrl that = (MapeamentoUrl) o;

        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        return Objects.equals(codigoCurto, that.codigoCurto);
    }

    @Override
    public int hashCode() {
        if (this.id != null) {
            return Objects.hash(this.id);
        }

        return Objects.hash(codigoCurto);
    }

    @Override
    public String toString() {
        return "MapeamentoUrl{" +
                "id='" + id + '\'' +
                ", codigoCurto=" + (codigoCurto != null ? codigoCurto.getValor() : "null") +
                ", urlOriginal=" + (urlOriginal != null ? urlOriginal.getValor() : "null") +
                ", dataCriacao=" + dataCriacao +
                ", dataExpiracao=" + dataExpiracao +
                ", contadorAcessos=" + contadorAcessos +
                '}';
    }
}
