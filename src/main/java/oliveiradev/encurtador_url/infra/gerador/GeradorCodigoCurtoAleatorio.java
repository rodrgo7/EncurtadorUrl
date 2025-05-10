package oliveiradev.encurtador_url.infra.gerador;

import oliveiradev.encurtador_url.domain.model.CodigoCurto;
import oliveiradev.encurtador_url.domain.service.CodigoCurtoService;

import java.security.SecureRandom;
import java.util.Base64;

public class GeradorCodigoCurtoAleatorio implements CodigoCurtoService {
    // SecureRandom para fins de segurança e aleatoriedade.
    private static final SecureRandom random = new SecureRandom();

    private static final Base64.Encoder encoder = Base64.getMimeEncoder().withoutPadding();

    // Define o número de bytes aleatórios a serem gerados.
    private static final int NUMERO_BYTES_CODIGO = 6;

    @Override
    public CodigoCurto gerar() {
        byte[] bytesAleatorios = new byte [NUMERO_BYTES_CODIGO];
        random.nextBytes(bytesAleatorios); // Preenche a array com bytes aleatorios;
        String valorCodigo = encoder.encodeToString(bytesAleatorios); // Converte os bytes para Base64

        return new CodigoCurto(valorCodigo);
    }
}
