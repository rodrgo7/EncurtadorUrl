package oliveiradev.encurtador_url.interfaces.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UrlNaoEncontradaInterfaceException extends RuntimeException {

    public UrlNaoEncontradaInterfaceException(String mensagem) {
        super(mensagem);
    }

    public UrlNaoEncontradaInterfaceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

