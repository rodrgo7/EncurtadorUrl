package oliveiradev.encurtador_url.application.service;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.application.dto.ComandoEncurtadorUrl; // DTO da raiz
import oliveiradev.encurtador_url.application.dto.DtoUrlEncurtada;   // DTO da raiz
import oliveiradev.encurtador_url.domain.service.EncurtamentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Validated
public class AplicacaoEncurtadorService {
    private static final Logger log = LoggerFactory.getLogger(AplicacaoEncurtadorService.class);

    private final EncurtamentoService encurtamentoService; // Serviço de Domínio
    private final String baseUrlAplicacao;

    @Autowired
    public AplicacaoEncurtadorService(EncurtamentoService encurtamentoService,
                                      @Value("${app.baseUrl}") String baseUrlAplicacao) {
        this.encurtamentoService = encurtamentoService;
        this.baseUrlAplicacao = baseUrlAplicacao.endsWith("/") ?
                baseUrlAplicacao.substring(0, baseUrlAplicacao.length() - 1) :
                baseUrlAplicacao;
    }

    @Transactional
    public DtoUrlEncurtada encurtarUrl(@Valid ComandoEncurtadorUrl comando) {
        log.info("Serviço de Aplicação: Processando comando para encurtar URL: '{}', TTL (minutos): {}",
                comando.getUrlOriginal(), comando.getTtlEmMinutos());

        UrlOriginal urlOriginalObj = new UrlOriginal(comando.getUrlOriginal());

        LocalDateTime dataExpiracao = null;
        if (comando.getTtlEmMinutos() != null && comando.getTtlEmMinutos() > 0) {
            dataExpiracao = LocalDateTime.now().plusMinutes(comando.getTtlEmMinutos());
            log.debug("Data de expiração calculada: {}", dataExpiracao);
        } else {
            log.debug("Nenhum TTL fornecido ou TTL inválido, URL não expirará.");
        }

        MapeamentoUrl mapeamentoPersistido = encurtamentoService.encurtar(urlOriginalObj, dataExpiracao);

        String urlEncurtadaCompleta = baseUrlAplicacao + "/" + mapeamentoPersistido.getCodigoCurto().getValor();
        log.info("Serviço de Aplicação: URL encurtada com sucesso para: '{}', Expira em: {}",
                urlEncurtadaCompleta, mapeamentoPersistido.getDataExpiracao());

        return new DtoUrlEncurtada(
                mapeamentoPersistido.getUrlOriginal().getValor(),
                mapeamentoPersistido.getCodigoCurto().getValor(),
                urlEncurtadaCompleta,
                mapeamentoPersistido.getContadorAcessos(),
                mapeamentoPersistido.getDataCriacao(),
                mapeamentoPersistido.getDataExpiracao()
        );
    }

    @Transactional(readOnly = true)
    public Optional<DtoUrlEncurtada> obterInfoUrlPorCodigoCurto(String valorCodigoCurto) {
        log.info("Serviço de Aplicação: Buscando informações para o código curto: {}", valorCodigoCurto);
        CodigoCurto codigoCurtoObj = new CodigoCurto(valorCodigoCurto);
        LocalDateTime agora = LocalDateTime.now();

        return encurtamentoService.buscarPorCodigoCurto(codigoCurtoObj) // Usa método com nome corrigido
                .filter(mapeamento -> {
                    if (mapeamento.isExpirado(agora)) {
                        log.warn("Mapeamento para código '{}' encontrado, mas está expirado (expirou em {}).",
                                valorCodigoCurto, mapeamento.getDataExpiracao());
                        return false;
                    }
                    return true;
                })
                .map(mapeamento -> {
                    String urlEncurtadaCompleta = baseUrlAplicacao + "/" + mapeamento.getCodigoCurto().getValor();
                    return new DtoUrlEncurtada(
                            mapeamento.getUrlOriginal().getValor(),
                            mapeamento.getCodigoCurto().getValor(),
                            urlEncurtadaCompleta,
                            mapeamento.getContadorAcessos(),
                            mapeamento.getDataCriacao(),
                            mapeamento.getDataExpiracao()
                    );
                });
    }

    @Transactional
    public Optional<String> redirecionarEIncrementarAcesso(String valorCodigoCurto) {
        log.info("Serviço de Aplicação: Processando redirecionamento para o código curto: {}", valorCodigoCurto);
        CodigoCurto codigoCurtoObj = new CodigoCurto(valorCodigoCurto);
        LocalDateTime agora = LocalDateTime.now();

        Optional<MapeamentoUrl> mapeamentoOpt = encurtamentoService.buscarPorCodigoCurto(codigoCurtoObj); // Usa método com nome corrigido

        if (mapeamentoOpt.isEmpty()) {
            log.warn("Código curto '{}' não encontrado para redirecionamento.", valorCodigoCurto);
            return Optional.empty();
        }

        MapeamentoUrl mapeamento = mapeamentoOpt.get();

        if (mapeamento.isExpirado(agora)) {
            log.warn("Tentativa de redirecionar código curto expirado: '{}'. Expirou em: {}",
                    valorCodigoCurto, mapeamento.getDataExpiracao());
            return Optional.empty();
        }

        mapeamento.registrarAcesso();
        encurtamentoService.salvar(mapeamento);

        log.info("Acesso registrado para código '{}'. Redirecionando para URL: '{}'",
                valorCodigoCurto, mapeamento.getUrlOriginal().getValor());
        return Optional.of(mapeamento.getUrlOriginal().getValor());
    }
}