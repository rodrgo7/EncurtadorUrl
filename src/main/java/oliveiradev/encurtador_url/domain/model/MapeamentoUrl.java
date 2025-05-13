package oliveiradev.encurtador_url.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "mapeamentos_url") // Nome da coleção no MongoDB
public class MapeamentoUrl { // Entity - Aggregate Root
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

    public MapeamentoUrl(CodigoCurto codigoCurto, UrlOriginal urlOriginal, LocalDateTime dataExpiracao) {
        Assert.notNull(codigoCurto, "O código curto não pode ser nulo.");
        Assert.notNull(urlOriginal, "A URL original não pode ser nula.");

        this.codigoCurto = codigoCurto;
        this.urlOriginal = urlOriginal;
        this.dataCriacao = LocalDateTime.now(); // Data de criação é sempre o momento atual
        this.dataExpiracao = dataExpiracao;
        this.contadorAcessos = 0L;
    }

    // Getters
    public String getId() { return id; }
    public CodigoCurto getCodigoCurto() { return codigoCurto; }
    public UrlOriginal getUrlOriginal() { return urlOriginal; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public long getContadorAcessos() { return contadorAcessos; }

    public boolean isExpirado(LocalDateTime dataHoraAtual) {
        Assert.notNull(dataHoraAtual, "A data/hora atual para verificação de expiração não pode ser nula.");
        if (this.dataExpiracao == null) {
            return false; // Nunca expira
        }
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
        if (this.id != null && that.id != null) { // Compara por ID se ambos existirem
            return Objects.equals(this.id, that.id);
        }
        // Se IDs não estão presentes (ex: antes de salvar), compara por código curto (que deve ser único)
        return Objects.equals(codigoCurto, that.codigoCurto);
    }

    @Override
    public int hashCode() {
        if (this.id != null) { // Usa ID para hashCode se existir
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