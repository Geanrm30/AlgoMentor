package ni.edu.uam.algomentor.api;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class OpenTriviaTextDecoder {

    private OpenTriviaTextDecoder() {
    }

    static String decode(String encodedText) {
        Objects.requireNonNull(encodedText, "El texto de la pregunta no puede ser nulo");
        String decoded = URLDecoder.decode(encodedText.replace("+", "%2B"), StandardCharsets.UTF_8);
        if (decoded.isBlank()) {
            throw new IllegalArgumentException("El texto de la pregunta no puede estar vacío");
        }
        return decoded;
    }
}
