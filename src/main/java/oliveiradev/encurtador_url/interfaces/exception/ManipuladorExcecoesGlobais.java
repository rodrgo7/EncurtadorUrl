package oliveiradev.encurtador_url.interfaces.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class ManipuladorExcecoesGlobais {
    private static final Logger log = LoggerFactory.getLogger(ManipuladorExcecoesGlobais.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (mensagemExistente, novaMensagem) -> mensagemExistente + "; " + novaMensagem // Em caso de múltiplos erros no mesmo campo
                ));
        log.warn("Erro de validação nos argumentos do método: {}", erros, ex);
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(UrlNaoEncontradaInterfaceException.class)
    public ResponseEntity<Map<String, String>> handleUrlNaoEncontrada(UrlNaoEncontradaInterfaceException ex) {
        Map<String, String> corpoResposta = new HashMap<>();
        corpoResposta.put("erro", ex.getMessage());
        log.warn("Recurso não encontrado: {}", ex.getMessage());

        return new ResponseEntity<>(corpoResposta, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        Map<String, String> corpoResposta = new HashMap<>();
        // Evite expor detalhes internos da exceção diretamente ao cliente em produção.
        corpoResposta.put("erro", "Não foi possível processar a requisição devido a um estado inesperado ou conflito de recursos.");
        // Para depuração, podemos logar a mensagem completa.
        log.error("Exceção de estado ilegal processando requisição: {}", ex.getMessage(), ex);
        // HttpStatus.CONFLICT (409) pode ser apropriado se for uma falha de regra de negócio previsível.
        // HttpStatus.INTERNAL_SERVER_ERROR (500) para falhas mais genéricas de estado.
        return new ResponseEntity<>(corpoResposta, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> corpoResposta = new HashMap<>();
        corpoResposta.put("erro", "Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.");
        log.error("Erro genérico não tratado capturado: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpoResposta);
    }
}
