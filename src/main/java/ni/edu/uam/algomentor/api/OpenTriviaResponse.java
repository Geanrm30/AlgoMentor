package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenTriviaResponse(
        @JsonProperty("response_code") int responseCode,
        List<OpenTriviaQuestion> results
) {
    public OpenTriviaResponse {
        results = List.copyOf(results);
    }
}
