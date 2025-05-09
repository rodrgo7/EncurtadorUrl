package oliveiradev.encurtador_url.domain.repository;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;

import java.util.Optional;

public interface MapeamentoUrlRepository {
    MapeamentoUrl salvar(MapeamentoUrl mapeamentoUrl);
    Optional<MapeamentoUrl> buscaCodigoCurto(CodigoCurto codigoCurto);

    boolean existeCodigoCurto(CodigoCurto codigoCurto);
    Optional<MapeamentoUrl> buscaurlOriginal(UrlOriginal urlOriginal);
}
