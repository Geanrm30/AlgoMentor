package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenTriviaResponse(
        @JsonProperty("response_code") int responseCode,
        List<OpenTriviaQuestion> results
) {
    public OpenTriviaResponse {
        results = List.copyOf(Objects.requireNonNull(
                results, "La lista de resultados de Open Trivia DB no puede ser nula"));
    }
}
