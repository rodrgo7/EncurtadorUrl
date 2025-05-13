package oliveiradev.encurtador_url.interfaces.rest;

import oliveiradev.encurtador_url.application.dto.ComandoEncurtadorUrl;
import oliveiradev.encurtador_url.application.dto.DtoUrlEncurtada;
import oliveiradev.encurtador_url.application.service.AplicacaoEncurtadorService;

import oliveiradev.encurtador_url.interfaces.exception.UrlNaoEncontradaInterfaceException;
import oliveiradev.encurtador_url.interfaces.rest.dto.EncurtarUrlHttpRequest;
import oliveiradev.encurtador_url.interfaces.rest.dto.EncurtarUrlHttpResponse;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;


@RestController
@RequestMapping("/") // Define o path base para os endpoints de redirecionamento.
public class UrlShortController {
    // Logger inicializado com o nome correto da classe
    private static final Logger log = LoggerFactory.getLogger(UrlShortController.class);

    private final AplicacaoEncurtadorService servicoAplicacao;

    @Autowired
    public UrlShortController(AplicacaoEncurtadorService servicoAplicacao) { // Nome do construtor corrigido
        this.servicoAplicacao = servicoAplicacao;
    }

    @PostMapping("/api/v1/encurtar")
    public ResponseEntity<EncurtarUrlHttpResponse> encurtarUrl(@Valid @RequestBody EncurtarUrlHttpRequest request) {
        log.info("Controller: Recebida requisição para encurtar URL: '{}', TTL (minutos): {}",
                request.getUrl(), request.getTtlEmMinutos());

        // Mapeia o DTO HTTP para o Comando da camada de Aplicação.
        ComandoEncurtadorUrl comando = new ComandoEncurtadorUrl(request.getUrl(), request.getTtlEmMinutos());

        // Delega para o serviço de aplicação.
        DtoUrlEncurtada dtoResultadoApp = servicoAplicacao.encurtarUrl(comando);

        // Mapeia o DTO da camada de Aplicação para o DTO de resposta HTTP.
        EncurtarUrlHttpResponse httpResponse = new EncurtarUrlHttpResponse(
                dtoResultadoApp.getUrlEncurtadaCompleta(),
                dtoResultadoApp.getUrlOriginal(),
                dtoResultadoApp.getAcessos(),
                dtoResultadoApp.getDataCriacao(),
                dtoResultadoApp.getDataExpiracao()
        );

        log.info("Controller: URL encurtada com sucesso. URL curta: '{}', Expira em: {}",
                httpResponse.getUrlEncurtada(), httpResponse.getDataExpiracao());
        return ResponseEntity.status(HttpStatus.CREATED).body(httpResponse);
    }

    @GetMapping("/{codigoCurto}")
    public ResponseEntity<Void> redirecionar(@PathVariable String codigoCurto) {
        log.info("Controller: Recebida requisição de redirecionamento para o código curto: '{}'", codigoCurto);

        // Delega para o serviço de aplicação, que já lida com a lógica de expiração e incremento de acesso.
        Optional<String> urlOriginalOpt = servicoAplicacao.redirecionarEIncrementarAcesso(codigoCurto);

        if (urlOriginalOpt.isPresent()) {
            String urlOriginal = urlOriginalOpt.get();
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(new URI(urlOriginal)); // Define o cabeçalho Location para o redirecionamento.
                log.info("Controller: Redirecionando código '{}' para URL: '{}'", codigoCurto, urlOriginal);
                return new ResponseEntity<>(headers, HttpStatus.FOUND); // HTTP 302
            } catch (URISyntaxException e) {
                // Isso indica um problema com os dados armazenados (URL original malformada).
                log.error("Controller: URL original ('{}') recuperada para o código '{}' é malformada.",
                        urlOriginal, codigoCurto, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            // Se o Optional estiver vazio, significa que o código não foi encontrado ou está expirado.
            log.warn("Controller: Código curto '{}' não encontrado ou expirado para redirecionamento.", codigoCurto);
            // Lança uma exceção que será tratada pelo ManipuladorExcecoesGlobais para retornar 404.
            throw new UrlNaoEncontradaInterfaceException("URL não encontrada ou expirada para o código: " + codigoCurto);
        }
    }

    @GetMapping("/api/v1/info/{codigoCurto}")
    public ResponseEntity<EncurtarUrlHttpResponse> obterInfoUrl(@PathVariable String codigoCurto) {
        log.info("Controller: Recebida requisição de informações para o código curto: '{}'", codigoCurto);

        // Delega para o serviço de aplicação, que já lida com a lógica de expiração.
        Optional<DtoUrlEncurtada> dtoResultadoOpt = servicoAplicacao.obterInfoUrlPorCodigoCurto(codigoCurto);

        return dtoResultadoOpt
                .map(dtoApp -> { // Se encontrado e não expirado, mapeia para o DTO de resposta HTTP.
                    EncurtarUrlHttpResponse httpResponse = new EncurtarUrlHttpResponse(
                            dtoApp.getUrlEncurtadaCompleta(),
                            dtoApp.getUrlOriginal(),
                            dtoApp.getAcessos(),
                            dtoApp.getDataCriacao(),
                            dtoApp.getDataExpiracao()
                    );
                    log.info("Controller: Informações encontradas para o código '{}'.", codigoCurto);
                    return ResponseEntity.ok(httpResponse);
                })
                .orElseThrow(() -> { // Se não encontrado ou expirado, lança exceção para resultar em 404.
                    log.warn("Controller: Código curto '{}' não encontrado ou expirado ao buscar informações.", codigoCurto);
                    return new UrlNaoEncontradaInterfaceException("Informações não encontradas ou URL expirada para o código: " + codigoCurto);
                });
    }
}
