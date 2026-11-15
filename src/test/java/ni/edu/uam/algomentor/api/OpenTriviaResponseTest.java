package ni.edu.uam.algomentor.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenTriviaResponseTest {

    @Test
    void deserializaRespuestaDeOpenTrivia() throws IOException {
        String json = """
                {
                  "response_code": 0,
                  "results": [
                    {
                      "type": "multiple",
                      "difficulty": "hard",
                      "category": "Science%3A%20Computers",
                      "question": "What%20was%20the%20first%20company%20to%20use%20the%20term%20%22Golden%20Master%22%3F",
                      "correct_answer": "Apple",
                      "incorrect_answers": ["IBM", "Microsoft", "Google"]
                    }
                  ]
                }
                """;

        OpenTriviaResponse response = new ObjectMapper().readValue(json, OpenTriviaResponse.class);

        assertEquals(0, response.responseCode());
        assertEquals(1, response.results().size());
        OpenTriviaQuestion question = response.results().get(0);
        assertEquals("multiple", question.type());
        assertEquals("hard", question.difficulty());
        assertEquals("Apple", question.correctAnswer());
        assertEquals(3, question.incorrectAnswers().size());
    }
}
