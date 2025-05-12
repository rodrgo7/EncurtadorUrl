package oliveiradev.encurtador_url.interfaces.rest;

import jakarta.validation.Valid;
import oliveiradev.encurtador_url.application.service.AplicacaoEncurtadorService;
import oliveiradev.encurtador_url.dto.ComandoEncurtadorUrl;
import oliveiradev.encurtador_url.dto.DtoUrlEncurtada;
import oliveiradev.encurtador_url.interfaces.exception.UrlNaoEncontradaInterfaceException;
import oliveiradev.encurtador_url.interfaces.rest.dto.EncurtarUrlHttpRequest;
import oliveiradev.encurtador_url.interfaces.rest.dto.EncurtarUrlHttpResponse;
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

/**
 * REST Controller for URL shortening operations.
 * Handles HTTP requests for URL shortening, redirection, and information retrieval.
 */
@RestController
@RequestMapping("/")
public class UrlShortController {
    private static final Logger log = LoggerFactory.getLogger(UrlShortController.class);
    private final AplicacaoEncurtadorService aplicacaoEncurtadorService;

    @Autowired
    public UrlShortController(AplicacaoEncurtadorService aplicacaoEncurtadorService) {
        this.aplicacaoEncurtadorService = aplicacaoEncurtadorService;
    }

    /**
     * Shortens a URL with optional TTL (Time To Live).
     * @param request The HTTP request containing the URL to shorten and optional TTL
     * @return HTTP response with the shortened URL information
     */
    @PostMapping("/api/v1/encurtar")
    public ResponseEntity<EncurtarUrlHttpResponse> encurtarUrl(@Valid @RequestBody EncurtarUrlHttpRequest request) {
        log.info("Controller: Recebida requisição para encurtar URL: '{}', TTL (minutos): {}",
                request.getUrl(), request.getTtlEmMinutos());

        ComandoEncurtadorUrl comando = new ComandoEncurtadorUrl(request.getUrl(), request.getTtlEmMinutos());
        DtoUrlEncurtada dtoResultadoApp = aplicacaoEncurtadorService.encurtarUrl(comando);

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

    /**
     * Redirects to the original URL based on the short code.
     * @param codigoCurto The short code to redirect
     * @return HTTP response with redirect headers or error status
     */
    @GetMapping("/{codigoCurto}")
    public ResponseEntity<Void> redirecionar(@PathVariable String codigoCurto) {
        log.info("Controller: Recebida requisição de redirecionamento para o código curto: '{}'", codigoCurto);

        Optional<String> urlOriginalOpt = aplicacaoEncurtadorService.redirecionarEIncrementarAcesso(codigoCurto);

        if (urlOriginalOpt.isPresent()) {
            String urlOriginal = urlOriginalOpt.get();
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(new URI(urlOriginal));
                log.info("Controller: Redirecionando código '{}' para URL: '{}'", codigoCurto, urlOriginal);
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } catch (URISyntaxException e) {
                log.error("Controller: URL original ('{}') recuperada para o código '{}' é malformada.",
                        urlOriginal, codigoCurto, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            log.warn("Controller: Código curto '{}' não encontrado ou expirado para redirecionamento.", codigoCurto);
            throw new UrlNaoEncontradaInterfaceException("URL não encontrada ou expirada para o código: " + codigoCurto);
        }
    }

    /**
     * Retrieves information about a shortened URL.
     * @param codigoCurto The short code to get information for
     * @return HTTP response with the URL information or 404 if not found/expired
     */
    @GetMapping("/api/v1/info/{codigoCurto}")
    public ResponseEntity<EncurtarUrlHttpResponse> obterInfoUrl(@PathVariable String codigoCurto) {
        log.info("Controller: Recebida requisição de informações para o código curto: '{}'", codigoCurto);

        Optional<DtoUrlEncurtada> dtoResultadoOpt = aplicacaoEncurtadorService.obterInfoUrlPorCodigoCurto(codigoCurto);

        return dtoResultadoOpt
                .map(dtoApp -> {
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
                .orElseThrow(() -> {
                    log.warn("Controller: Código curto '{}' não encontrado ou expirado ao buscar informações.", codigoCurto);
                    return new UrlNaoEncontradaInterfaceException("Informações não encontradas ou URL expirada para o código: " + codigoCurto);
                });
    }
}