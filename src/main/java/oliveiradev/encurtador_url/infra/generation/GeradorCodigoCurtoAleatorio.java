package oliveiradev.encurtador_url.infra.generation;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.service.CodigoCurtoService; // Usa a interface definida no domínio
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class GeradorCodigoCurtoAleatorio implements CodigoCurtoService { // Implementa a interface do domínio
    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private static final int NUMERO_BYTES_CODIGO = 6; // Gera 8 caracteres Base64

    @Override
    public CodigoCurto gerar() {
        byte[] bytesAleatorios = new byte[NUMERO_BYTES_CODIGO];
        random.nextBytes(bytesAleatorios);
        String valorCodigo = encoder.encodeToString(bytesAleatorios);
        return new CodigoCurto(valorCodigo);
    }
}