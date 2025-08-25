package likelion.sajaboys.soboonsoboon.service.ai.receipt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.sajaboys.soboonsoboon.dto.ReceiptParsedResponse;
import likelion.sajaboys.soboonsoboon.dto.ReceiptParsedResponse.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ReceiptOcrService {

    private final WebClient openAiWebClient;   // OpenAiClientConfig 에서 주입되는 WebClient
    private final ObjectMapper om = new ObjectMapper();

    private final String visionModel;
    private final Integer maxTokens;

    public ReceiptOcrService(
            WebClient openAiWebClient,
            @Value("${openai.model}") String visionModel,
            @Value("${openai.max-tokens}") Integer maxTokens
    ) {
        this.openAiWebClient = openAiWebClient;
        this.visionModel = visionModel;
        this.maxTokens = maxTokens;
    }

    public ReceiptParsedResponse extractItemsAndTotal(MultipartFile file) throws Exception {
        // 1) 입력 검증
        String ct = file.getContentType();
        if (!(MediaType.IMAGE_JPEG_VALUE.equals(ct) || MediaType.IMAGE_PNG_VALUE.equals(ct))) {
            throw new IllegalArgumentException("unsupported content type");
        }

        // 2) 이미지 → data URL
        byte[] bytes = file.getBytes();
        String dataUrl = "data:" + ct + ";base64," + Base64.getEncoder().encodeToString(bytes);

        // 3) 한국어 프롬프트
        String systemPrompt =
                """
                        너는 영수증 인식기다. 아래 규칙을 반드시 지켜라.
                        - 출력은 지정된 JSON 스키마에 정확히 일치하는 JSON만 반환한다. 그 외 설명/문장/마크다운/코드는 금지한다.
                        - 각 품목은 name(문자열), quantity(정수, 1 이상), amount(숫자, 0 이상)만 포함한다.
                        - 총 금액은 total(숫자, 0 이상)로 반환한다.
                        - 통화기호(₩, 원, $, 등)와 천 단위 구분기호(,)를 제거하고 숫자만 사용한다.
                        - 품목으로 보기 애매한 줄(머리말/꼬리말/합계/세금/주소/연락처 등)은 items에서 제외한다.
                        - 추가 필드, 주석, 불필요한 텍스트는 절대 포함하지 않는다.""";

        String userPrompt =
                "다음 영수증 이미지에서 품목 목록(items: name, quantity, amount)과 총 금액(total)만 추출하라.\n" +
                        "반드시 스키마에 맞는 JSON만 반환하라. 추가 텍스트는 금지한다.";

        // 4) JSON 스키마(엄격)
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

        // 5) 요청 페이로드(비전 입력 + 구조화 출력)
        Map<String, Object> payload = Map.of(
                "model", visionModel,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "system", "content", List.of(
                                Map.of("type", "text", "text", systemPrompt)
                        )),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", userPrompt),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        ))
                ),
                "response_format", Map.of(
                        "type", "json_schema",
                        "json_schema", om.readTree(jsonSchema),
                        "strict", true
                )
        );

        // 6) OpenAI 호출 (프로젝트에 등록된 WebClient 사용)
        String raw = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("empty model response");
        }

        // 7) 응답 본문에서 JSON만 추출
        JsonNode root = om.readTree(raw.getBytes(StandardCharsets.UTF_8));
        JsonNode contentNode = null;
        if (root.has("choices") && root.get("choices").isArray() && !root.get("choices").isEmpty()) {
            contentNode = root.get("choices").get(0).path("message").path("content");
        }
        if (contentNode == null || contentNode.isMissingNode() || contentNode.isNull()) {
            throw new IllegalStateException("missing choices.message.content");
        }
        String json = contentNode.asText();

        // 8) 결과 JSON 후검증 및 DTO 매핑
        JsonNode result = om.readTree(json);
        if (!result.has("items") || !result.has("total")) {
            throw new IllegalStateException("invalid model output: missing fields");
        }

        List<Item> items = new ArrayList<>();
        for (JsonNode n : result.path("items")) {
            String name = n.path("name").asText(null);
            Integer qty = n.hasNonNull("quantity") ? n.get("quantity").asInt() : null;
            Double amt = n.hasNonNull("amount") ? n.get("amount").asDouble() : null;
            if (name == null || name.isBlank() || qty == null || qty < 1 || amt == null || amt < 0) {
                continue;
            }
            items.add(new Item(name, qty, amt));
        }
        if (items.isEmpty()) throw new IllegalStateException("no valid items");

        double total = result.get("total").asDouble();
        if (total < 0) throw new IllegalStateException("invalid total");

        return new ReceiptParsedResponse(items, total);
    }
}
