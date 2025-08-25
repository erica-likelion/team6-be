package likelion.sajaboys.soboonsoboon.service.ai.receipt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.sajaboys.soboonsoboon.dto.ReceiptDtos;
import likelion.sajaboys.soboonsoboon.dto.ReceiptDtos.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReceiptOcrService {
    private final WebClient openAiWebClient;
    private final ObjectMapper om = new ObjectMapper();

    private final String model;
    private final Integer maxTokens;

    public ReceiptOcrService(
            WebClient openAiWebClient,
            @Value("${openai.model}") String model,
            @Value("${openai.max-tokens}") Integer maxTokens
    ) {
        this.openAiWebClient = openAiWebClient;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public ReceiptDtos.ReceiptParsedResponse parseFromUrl(String imageHttpsUrl) throws Exception {
        long t0 = System.currentTimeMillis();

        URI u = URI.create(imageHttpsUrl);
        if (u.getScheme() == null || !(u.getScheme().equals("http") || u.getScheme().equals("https"))) {
            throw new IllegalArgumentException("imageUrl must be http/https");
        }
        log.info("[Receipt][URL] {}", safe(imageHttpsUrl));

        String systemPrompt =
                """
                        너는 영수증 인식기다. 아래 규칙을 반드시 지켜라.
                        - 출력은 지정된 JSON 스키마에 정확히 일치하는 JSON만 반환한다. 그 외 설명/문장/마크다운/코드는 금지한다.
                        - 각 품목은 name(문자열), quantity(정수, 1 이상), amount(숫자, 0 이상)만 포함한다.
                        - 총 금액은 total(숫자, 0 이상)으로 반환한다.
                        - 통화기호(₩, 원, $, 등)와 천 단위 구분기호(,)를 제거하고 숫자만 사용한다.
                        - 품목으로 보기 애매한 줄(머리말/꼬리말/합계/세금/주소/연락처 등)은 items에서 제외한다.
                        - 추가 필드, 주석, 불필요한 텍스트는 절대 포함하지 않는다.
                        """;

        String userPrompt =
                "다음 영수증 이미지에서 품목 목록(items: name, quantity, amount)과 총 금액(total)만 추출하라. 스키마에 맞는 JSON만 반환.";

        String schemaBody = """
                {
                  "type": "object",
                  "properties": {
                    "items": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "name": { "type": "string", "minLength": 1 },
                          "quantity": { "type": "integer", "minimum": 1 },
                          "amount": { "type": "number", "minimum": 0 }
                        },
                        "required": ["name","quantity","amount"],
                        "additionalProperties": false
                      }
                    },
                    "total": { "type": "number", "minimum": 0 }
                  },
                  "required": ["items","total"],
                  "additionalProperties": false
                }
                """.trim();

        Map<String, Object> payload = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", List.of(
                                        Map.of("type", "input_text", "text", systemPrompt)
                                )
                        ),
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of("type", "input_text", "text", userPrompt),
                                        Map.of("type", "input_image", "image_url", imageHttpsUrl)
                                )
                        )
                ),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "receipt_schema",
                                "strict", true,
                                "schema", om.readTree(schemaBody)
                        )
                ),
                "max_output_tokens", maxTokens
        );


        log.debug("[Receipt][REQ] model={} imageUrl={} maxTokens={}", model, safe(imageHttpsUrl), maxTokens);

        String raw = openAiWebClient.post()
                .uri("/responses")
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            log.error("[Receipt][HTTP-ERR] status={} body.head={}", resp.statusCode(), preview(body, 1000));
                            return Mono.error(new RuntimeException("openai http error " + resp.statusCode()));
                        })
                )
                .bodyToMono(String.class)
                .doOnError(e -> log.error("[Receipt][CALL][ERR] {}", e.toString(), e))
                .block();

        long tCall = System.currentTimeMillis() - t0;
        log.info("[Receipt][CALL] elapsed={}ms", tCall);

        if (raw == null || raw.isBlank()) {
            log.warn("[Receipt][RESP] raw is blank or null");
            throw new IllegalStateException("empty model response");
        }
        log.debug("[Receipt][RESP] raw.head={}", preview(raw, 800));

        JsonNode root = om.readTree(raw.getBytes(StandardCharsets.UTF_8));

        JsonNode parsed = root.path("text").path("output_parsed");
        String json;
        if (!parsed.isMissingNode() && !parsed.isNull()) {
            json = om.writeValueAsString(parsed);
        } else {
            JsonNode output = root.path("output");
            String found = null;
            if (output.isArray()) {
                outer:
                for (JsonNode msg : output) {
                    JsonNode content = msg.path("content");
                    if (content.isArray()) {
                        for (JsonNode c : content) {
                            if ("output_text".equals(c.path("type").asText()) && c.has("text")) {
                                found = c.get("text").asText();
                                break outer;
                            }
                        }
                    }
                }
            }
            if (found == null || found.isBlank()) {
                log.error("[Receipt][PARSE] missing structured text in output | raw.head={}", preview(raw, 800));
                throw new IllegalStateException("missing structured output");
            }
            json = found;
        }

        log.debug("[Receipt][PARSE] json.head={}", preview(json, 600));

        JsonNode result = om.readTree(json);
        if (!result.has("items") || !result.has("total")) {
            log.error("[Receipt][VALIDATE] missing fields: items/total");
            throw new IllegalStateException("invalid model output: missing fields");
        }

        List<Item> items = new ArrayList<>();
        for (JsonNode n : result.path("items")) {
            String name = n.path("name").asText(null);
            Integer qty = n.hasNonNull("quantity") ? n.get("quantity").asInt() : null;
            Double amt = n.hasNonNull("amount") ? n.get("amount").asDouble() : null;
            if (name == null || name.isBlank() || qty == null || qty < 1 || amt == null || amt < 0) {
                log.debug("[Receipt][FILTER] drop item name={} qty={} amt={}", name, qty, amt);
                continue;
            }
            items.add(new Item(name, qty, amt));
        }
        if (items.isEmpty()) {
            log.error("[Receipt][VALIDATE] no valid items");
            throw new IllegalStateException("no valid items");
        }

        Double total = result.get("total").asDouble();
        if (total < 0) {
            log.error("[Receipt][VALIDATE] negative total={}", total);
            throw new IllegalStateException("invalid total");
        }

        long done = System.currentTimeMillis() - t0;
        log.info("[Receipt][OK] items={} total={} elapsed={}ms", items.size(), total, done);
        return new ReceiptDtos.ReceiptParsedResponse(items, total);
    }

    private String safe(String s) {
        if (s == null) return null;
        String trimmed = s.length() > 300 ? s.substring(0, 300) + "...(truncated)" : s;
        return trimmed.replaceAll("[\\r\\n]", " ");
    }

    private String preview(String s, int limit) {
        if (s == null) return "null";
        if (s.length() <= limit) return s;
        int tail = Math.min(64, limit / 3);
        int head = limit - tail - 5;
        if (head < 0) head = limit;
        return s.substring(0, head) + "...||..." + s.substring(s.length() - tail);
    }

}