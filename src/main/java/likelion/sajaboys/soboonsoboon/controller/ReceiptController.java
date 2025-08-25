package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.dto.ReceiptDtos;
import likelion.sajaboys.soboonsoboon.service.ai.receipt.ReceiptOcrService;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptOcrService receiptOcrService;

    public ReceiptController(ReceiptOcrService receiptOcrService) {
        this.receiptOcrService = receiptOcrService;
    }

    @PostMapping(
            value = "/parse",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> parseByUrl(@RequestBody ReceiptDtos.ReceiptParseRequest req) {
        // 입력 검증
        if (req == null || req.image() == null || req.image().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", Map.of("code", "BAD_REQUEST", "message", "invalid input"))
            );
        }
        try {
            // URL 문법 검증
            URI u = URI.create(req.image());
            if (u.getScheme() == null || !(u.getScheme().equals("http") || u.getScheme().equals("https"))) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "error", Map.of("code", "BAD_REQUEST", "message", "image must be http/https"))
                );
            }

            ReceiptDtos.ReceiptParsedResponse data = receiptOcrService.parseFromUrl(req.image());
            return ResponseEntity.ok(ApiSuccess.of(data));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", Map.of("code", "INTERNAL_ERROR", "message", "receipt recognition failed"))
            );
        }
    }
}
