package oliveiradev.encurtador_url.infra.persistence;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.domain.repository.MapeamentoUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
public class SpringDataMongoMapeamentoUrlRepositoryImpl implements MapeamentoUrlRepository {
    private static final Logger log = LoggerFactory.getLogger(SpringDataMongoMapeamentoUrlRepositoryImpl.class);
    private final InternalSpringDataMongoRepository internalMongoRepository;

    @Autowired
    public SpringDataMongoMapeamentoUrlRepositoryImpl(InternalSpringDataMongoRepository internalMongoRepository) {
        this.internalMongoRepository = internalMongoRepository;
    }

    @Override
    public MapeamentoUrl salvar(MapeamentoUrl mapeamentoUrl) {
        log.debug("Persistindo MapeamentoUrl com código curto: {}", mapeamentoUrl.getCodigoCurto().getValor());
        return internalMongoRepository.save(mapeamentoUrl);
    }

    @Override
    public Optional<MapeamentoUrl> buscarPorCodigoCurto(CodigoCurto codigoCurto) { // Nome corrigido
        log.debug("Buscando MapeamentoUrl por código curto: {}", codigoCurto.getValor());
        return internalMongoRepository.findByCodigoCurto_Valor(codigoCurto.getValor());
    }

    @Override
    public boolean existeCodigoCurto(CodigoCurto codigoCurto) {
        log.trace("Verificando existência do código curto: {}", codigoCurto.getValor());
        return internalMongoRepository.existsByCodigoCurto_Valor(codigoCurto.getValor());
    }

    @Override
    public Optional<MapeamentoUrl> buscarPorUrlOriginal(UrlOriginal urlOriginal) { // Nome corrigido
        log.debug("Buscando MapeamentoUrl por URL original: {}", urlOriginal.getValor());
        return internalMongoRepository.findByUrlOriginal_Valor(urlOriginal.getValor());
    }
}