package oliveiradev.encurtador_url.infrastructure.services;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.service.CodigoCurtoService;

import java.security.SecureRandom;
import java.util.Base64;

public class GeradorCodigoCurtoAleatorio implements CodigoCurtoService {
    private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getMimeEncoder().withoutPadding();
    private static final int NUMERO_BYTES_CODIGO = 6;

    @Override
    public CodigoCurto gerar() {
        byte[] bytesAleatorios = new byte[NUMERO_BYTES_CODIGO];
        random.nextBytes(bytesAleatorios);
        String valorCodigo = encoder.encodeToString(bytesAleatorios);

        return new CodigoCurto(valorCodigo);
    }
} 