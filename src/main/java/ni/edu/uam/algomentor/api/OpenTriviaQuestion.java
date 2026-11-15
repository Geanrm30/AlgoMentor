package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenTriviaQuestion(
        String type,
        String difficulty,
        String category,
        String question,
        @JsonProperty("correct_answer") String correctAnswer,
        @JsonProperty("incorrect_answers") java.util.List<String> incorrectAnswers
) {
    public OpenTriviaQuestion {
        type = OpenTriviaTextDecoder.decode(type);
        difficulty = OpenTriviaTextDecoder.decode(difficulty);
        category = OpenTriviaTextDecoder.decode(category);
        question = OpenTriviaTextDecoder.decode(question);
        correctAnswer = OpenTriviaTextDecoder.decode(correctAnswer);
        incorrectAnswers = List.copyOf(Objects.requireNonNull(
                incorrectAnswers, "La lista de respuestas incorrectas no puede ser nula"))
                .stream()
                .map(OpenTriviaTextDecoder::decode)
                .toList();
    }
}
