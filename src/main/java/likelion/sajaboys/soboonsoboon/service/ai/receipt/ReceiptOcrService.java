package likelion.sajaboys.soboonsoboon.service.ai.receipt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.sajaboys.soboonsoboon.dto.ReceiptParsedResponse;
import likelion.sajaboys.soboonsoboon.dto.ReceiptParsedResponse.Item;
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

    private final WebClient openAiWebClient;   // 설정된 WebClient (Authorization/timeout 포함)
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

    // 공개/프리사인 HTTPS URL을 입력으로 받아 파싱
    public ReceiptParsedResponse parseFromUrl(String imageHttpsUrl) throws Exception {
        long t0 = System.currentTimeMillis();

        // 1) 입력 검증
        URI u = URI.create(imageHttpsUrl);
        if (u.getScheme() == null || !(u.getScheme().equals("http") || u.getScheme().equals("https"))) {
            throw new IllegalArgumentException("imageUrl must be http/https");
        }
        log.info("[Receipt][URL] {}", safe(imageHttpsUrl));

        // 2) 한국어 프롬프트
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
                "다음 영수증 이미지에서 품목 목록(items: name, quantity, amount)과 총 금액(total)만 추출하라.\n" +
                        "반드시 스키마에 맞는 JSON만 반환하라. 추가 텍스트는 금지한다.";

        // 3) 구조화 출력 스키마(엄격)
        String jsonSchema = """
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

        // 4) 요청 페이로드(비전 입력 + 구조화 출력)
        Map<String, Object> payload = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "system", "content", List.of(
                                Map.of("type", "text", "text", systemPrompt)
                        )),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", userPrompt),
                                Map.of("type", "image_url", "image_url", Map.of(
                                        "url", imageHttpsUrl
                                ))
                        ))
                ),
                "response_format", Map.of(
                        "type", "json_schema",
                        "json_schema", om.readTree(jsonSchema),
                        "strict", true
                )
        );

        log.debug("[Receipt][REQ] model={} imageUrl={} maxTokens={}", model, safe(imageHttpsUrl), maxTokens);

        // 5) OpenAI 호출
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
        log.debug("[Receipt][RESP] raw.head={}", preview(raw, 600));

        // 6) 응답에서 JSON만 추출
        JsonNode root = om.readTree(raw.getBytes(StandardCharsets.UTF_8));
        JsonNode contentNode = (root.has("choices") && root.get("choices").isArray() && !root.get("choices").isEmpty())
                ? root.get("choices").get(0).path("message").path("content")
                : null;
        if (contentNode == null || contentNode.isMissingNode() || contentNode.isNull()) {
            log.error("[Receipt][PARSE] missing choices.message.content | raw.head={}", preview(raw, 600));
            throw new IllegalStateException("missing choices.message.content");
        }
        String json = contentNode.asText();
        log.debug("[Receipt][PARSE] json.head={}", preview(json, 600));

        // 7) 결과 후검증 + 매핑
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
        return new ReceiptParsedResponse(items, total);
    }

    private String safe(String s) {
        if (s == null) return null;
        // 쿼리 문자열은 길 수 있으니 일부 마스킹
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
