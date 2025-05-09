package oliveiradev.encurtador_url.domain.service;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.domain.repository.MapeamentoUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class EncurtamentoService {
    private static final Logger log = LoggerFactory.getLogger(EncurtamentoService.class);
    // Número máximo de tentativas para gerar um código curto
    private static final int MAX_TENTATIVAS_GERACAO_CODIGO = 5;
    private final MapeamentoUrlRepository mapeamentoUrlRepository;
    private final CodigoCurtoService codigoCurtoService;

    @Autowired
    public EncurtamentoService(MapeamentoUrlRepository mapeamentoUrlRepository, CodigoCurtoService codigoCurtoService) {
        this.mapeamentoUrlRepository = mapeamentoUrlRepository;
        this.codigoCurtoService = codigoCurtoService;
    }

    public MapeamentoUrl encurtar(UrlOriginal urlOriginal, LocalDateTime dataExpiracao) {
        log.debug("Iniciando processo de encurtamento para URL: {}, Expiração: {}", urlOriginal.getValor(), dataExpiracao);

        int tentativas = 0;
        while (tentativas < MAX_TENTATIVAS_GERACAO_CODIGO) {
            CodigoCurto novoCodCurto = codigoCurtoService.gerar();
            log.trace("Tentativa {}: Código curto gerado '{}'", tentativas + 1, novoCodCurto.getValor());

            if (!mapeamentoUrlRepository.existeCodigoCurto(novoCodCurto)) {
                MapeamentoUrl novoMapeamento = new MapeamentoUrl(novoCodCurto, urlOriginal, dataExpiracao);
                MapeamentoUrl mapeamentoSalvo = mapeamentoUrlRepository.salvar(novoMapeamento);
                log.info("URL '{}' encurtada com sucesso para '{}'. Expiração: {}",
                        urlOriginal.getValor(), novoCodCurto.getValor(), dataExpiracao);
                return mapeamentoSalvo;
            } else {
                log.warn("Colisão detectada para o código curto '{}'. Tentando novamente...", novoCodCurto.getValor());
            }
            tentativas++;
        }

        String mensagemErro = String.format(
                "Não foi possivel gerar um código curto único para a URL '%s' após %d tentativas.",
                urlOriginal.getValor(), MAX_TENTATIVAS_GERACAO_CODIGO
        );
        log.error(mensagemErro);

        throw new IllegalStateException(mensagemErro);
    }

    public Optional<MapeamentoUrl> buscaCodigoCurto(CodigoCurto codigoCurto) {
        log.debug("Bucando mapeamento para o código curto: {}", codigoCurto.getValor());

        return mapeamentoUrlRepository.buscaCodigoCurto(codigoCurto);
    }

    public MapeamentoUrl salvar(MapeamentoUrl mapeamentoUrl) {
        log.debug("Salvando mapeamento com ID: {} e Código Curto: {}", mapeamentoUrl.getId(), mapeamentoUrl.getCodigoCurto().getValor());

        return mapeamentoUrlRepository.salvar(mapeamentoUrl);
    }
}