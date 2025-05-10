package oliveiradev.encurtador_url.domain.service;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.model.MapeamentoUrl;
import oliveiradev.encurtador_url.domain.model.UrlOriginal;
import oliveiradev.encurtador_url.dto.ComandoEncurtadorUrl;
import oliveiradev.encurtador_url.dto.DtoUrlEncurtada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Para injetar valores do application.properties
import org.springframework.stereotype.Service; // Marca como um serviço Spring
import org.springframework.transaction.annotation.Transactional; // Para gerenciamento de transações
import jakarta.validation.Valid; // Para habilitar a validação de DTOs de comando
import org.springframework.validation.annotation.Validated; // Para habilitar a validação em nível de classe/método

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Validated // Habilita a validação para métodos anotados com @Valid neste serviço.
public class AplicacaoEncurtadorService {
    private static final Logger log = LoggerFactory.getLogger(AplicacaoEncurtadorService.class);

    private final EncurtamentoService encurtamentoService;
    private final String baseUrlAplicacao; // URL base para construir a URL encurtada completa.

    @Autowired
    public AplicacaoEncurtadorService(EncurtamentoService encurtamentoService,
                                      @Value("${app.baseUrl}") String baseUrlAplicacao) {
        this.encurtamentoService = encurtamentoService;
        // Garante que a baseUrl não termine com '/' para evitar barras duplas na URL final.
        this.baseUrlAplicacao = baseUrlAplicacao.endsWith("/") ?
                baseUrlAplicacao.substring(0, baseUrlAplicacao.length() - 1) :
                baseUrlAplicacao;
    }

    @Transactional // Garante que as operações de domínio (geração de código, salvamento) ocorram em uma transação.
    public DtoUrlEncurtada encurtarUrl(@Valid ComandoEncurtadorUrl comando) {
        log.info("Serviço de Aplicação: Processando comando para encurtar URL: '{}', TTL (minutos): {}",
                comando.getUrlOriginal(), comando.getTtlEmMinutos());

        // Converte a string da URL do comando para o Objeto de Valor do domínio.
        UrlOriginal urlOriginalObj = new UrlOriginal(comando.getUrlOriginal());

        // Calcula a data de expiração com base no TTL fornecido no comando.
        LocalDateTime dataExpiracao = null;
        if (comando.getTtlEmMinutos() != null && comando.getTtlEmMinutos() > 0) {
            dataExpiracao = LocalDateTime.now().plusMinutes(comando.getTtlEmMinutos());
            log.debug("Data de expiração calculada: {}", dataExpiracao);
        } else {
            log.debug("Nenhum TTL fornecido ou TTL inválido, URL não expirará (ou usará padrão do domínio, se houver).");
        }

        // Chama o serviço de domínio para executar a lógica de negócio de encurtamento.
        MapeamentoUrl mapeamentoPersistido = encurtamentoService.encurtar(urlOriginalObj, dataExpiracao);

        // Constrói a URL encurtada completa.
        String urlEncurtadaCompleta = baseUrlAplicacao + "/" + mapeamentoPersistido.getCodigoCurto().getValor();
        log.info("Serviço de Aplicação: URL encurtada com sucesso para: '{}', Expira em: {}",
                urlEncurtadaCompleta, mapeamentoPersistido.getDataExpiracao());

        // Converte a entidade de domínio MapeamentoUrl para o DTO de resposta.
        return new DtoUrlEncurtada(
                mapeamentoPersistido.getUrlOriginal().getValor(),
                mapeamentoPersistido.getCodigoCurto().getValor(),
                urlEncurtadaCompleta,
                mapeamentoPersistido.getContadorAcessos(),
                mapeamentoPersistido.getDataCriacao(),
                mapeamentoPersistido.getDataExpiracao()
        );
    }

    @Transactional(readOnly = true) // Operação de leitura
    public Optional<DtoUrlEncurtada> obterInfoUrlPorCodigoCurto(String valorCodigoCurto) {
        log.info("Serviço de Aplicação: Buscando informações para o código curto: {}", valorCodigoCurto);
        CodigoCurto codigoCurtoObj = new CodigoCurto(valorCodigoCurto);
        LocalDateTime agora = LocalDateTime.now();

        return encurtamentoService.buscaCodigoCurto(codigoCurtoObj)
                .filter(mapeamento -> { // Filtra para remover mapeamentos expirados.
                    if (mapeamento.isExpirado(agora)) {
                        log.warn("Mapeamento para código '{}' encontrado, mas está expirado (expirou em {}). Não será retornado.",
                                valorCodigoCurto, mapeamento.getDataExpiracao());
                        return false; // Exclui o mapeamento se estiver expirado.
                    }
                    return true; // Inclui o mapeamento se não estiver expirado.
                })
                .map(mapeamento -> { // Converte o MapeamentoUrl (domínio) para DtoUrlEncurtada (aplicação).
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

    @Transactional // Operação de escrita (incrementa contador e salva).
    public Optional<String> redirecionarEIncrementarAcesso(String valorCodigoCurto) {
        log.info("Serviço de Aplicação: Processando redirecionamento para o código curto: {}", valorCodigoCurto);
        CodigoCurto codigoCurtoObj = new CodigoCurto(valorCodigoCurto);
        LocalDateTime agora = LocalDateTime.now();

        Optional<MapeamentoUrl> mapeamentoOpt = encurtamentoService.buscaCodigoCurto(codigoCurtoObj);

        if (mapeamentoOpt.isEmpty()) {
            log.warn("Código curto '{}' não encontrado para redirecionamento.", valorCodigoCurto);
            return Optional.empty();
        }

        MapeamentoUrl mapeamento = mapeamentoOpt.get();

        // Verifica crucial: a URL está expirada?
        if (mapeamento.isExpirado(agora)) {
            log.warn("Tentativa de redirecionar código curto expirado: '{}'. Expirou em: {}",
                    valorCodigoCurto, mapeamento.getDataExpiracao());

            return Optional.empty(); // Não redireciona se estiver expirado.
        }

        // Se não estiver expirado, registra o acesso e salva o estado atualizado.
        mapeamento.registrarAcesso(); // Método de domínio para incrementar o contador.
        encurtamentoService.salvar(mapeamento); // Persiste a atualização do contador.

        log.info("Acesso registrado para código '{}'. Redirecionando para URL: '{}'",
                valorCodigoCurto, mapeamento.getUrlOriginal().getValor());

        return Optional.of(mapeamento.getUrlOriginal().getValor());
    }
}