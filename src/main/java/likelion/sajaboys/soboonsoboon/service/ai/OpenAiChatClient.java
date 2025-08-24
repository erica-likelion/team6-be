package likelion.sajaboys.soboonsoboon.service.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class OpenAiChatClient {

    private final WebClient client;
    private final String model;

    public OpenAiChatClient(WebClient openAiWebClient,
                            @Value("${openai.chat-model}") String model) {
        this.client = openAiWebClient;
        this.model = model;
    }

    public String complete(String systemPrompt, String userPrompt, int maxTokens) {
        var req = new ChatCompletionsRequest(
                model,
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user", userPrompt)
                ),
                maxTokens
        );
        var resp = client.post()
                .uri("/chat/completions")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ChatCompletionsResponse.class)
                .onErrorResume(e -> Mono.empty())
                .block();

        if (resp == null || resp.choices == null || resp.choices.isEmpty()) return null;
        return resp.choices.get(0).message.content;
    }

    // DTO들
    public record ChatCompletionsRequest(
            String model,
            List<Message> messages,
            @JsonProperty("max_tokens") Integer maxTokens
    ) {
    }

    public record Message(String role, String content) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatCompletionsResponse {
        public List<Choice> choices;

        public static class Choice {
            public Message message;
            public int index;
            public Object logprobs;
            public String finish_reason;
        }
    }
}
