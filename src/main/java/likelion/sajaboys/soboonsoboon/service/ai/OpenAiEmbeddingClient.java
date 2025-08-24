package likelion.sajaboys.soboonsoboon.service.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class OpenAiEmbeddingClient {

    private final WebClient client;
    private final String model;
    private final int timeoutSeconds;

    public OpenAiEmbeddingClient(
            WebClient openAiWebClient,
            @Value("${openai.embedding-model:text-embedding-3-small}") String model,
            @Value("${openai.timeout-seconds:10}") int timeoutSeconds
    ) {
        this.client = openAiWebClient;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
    }

    public float[] embed(String text) {
        // 방어: 모델/입력 검증
        if (model == null || model.isBlank() || text == null || text.isBlank()) {
            return new float[0];
        }
        try {
            var req = new EmbeddingRequest(model, List.of(text));

            var resp = client.post()
                    .uri("/embeddings")
                    .bodyValue(req)
                    .retrieve()
                    // 상태코드 별 본문 로깅용 예외로 변환
                    .onStatus(
                            s -> s.is4xxClientError() || s.is5xxServerError(),
                            r -> r.bodyToMono(String.class).defaultIfEmpty("")
                                    .flatMap(body -> {
                                        String msg = "OpenAI embeddings error: HTTP " + r.statusCode().value() + " body=" + truncate(body);
                                        return Mono.error(new RuntimeException(msg));
                                    })
                    )
                    .bodyToMono(EmbeddingResponse.class)
                    .block(Duration.ofSeconds(timeoutSeconds));

            if (resp == null || resp.data == null || resp.data.isEmpty()) return new float[0];

            List<Double> vec = resp.data.get(0).embedding;
            if (vec == null || vec.isEmpty()) return new float[0];

            float[] out = new float[vec.size()];
            for (int i = 0; i < vec.size(); i++) out[i] = vec.get(i).floatValue();
            return out;
        } catch (Exception e) {
            // 여기서 예외를 흡수하고 안전하게 빈 벡터 반환
            // log.warn("OpenAI embed failed: {}", e.toString());
            return new float[0];
        }
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() <= 500 ? s : s.substring(0, 500) + "...";
    }

    public record EmbeddingRequest(String model, List<String> input) {
    }

    public static class EmbeddingResponse {
        public List<Item> data;

        public static class Item {
            public String object;
            public List<Double> embedding;
            public int index;
        }

        @JsonProperty("model")
        public String modelName;
        public String object;
    }
}
