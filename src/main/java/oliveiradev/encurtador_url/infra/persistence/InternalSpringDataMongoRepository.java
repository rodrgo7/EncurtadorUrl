package oliveiradev.encurtador_url.infra.persistence;

import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface InternalSpringDataMongoRepository extends MongoRepository<MapeamentoUrl, String> {
    Optional<MapeamentoUrl> findByCodigoCurto_Valor(String valorCodigoCurto);
    boolean existsByCodigoCurto_Valor(String valorCodigoCurto);
    Optional<MapeamentoUrl> findByUrlOriginal_Valor(String valorUrlOriginal);
}
