package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.dto.ReceiptParsedResponse;
import likelion.sajaboys.soboonsoboon.service.ai.receipt.ReceiptOcrService;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptOcrService receiptOcrService;

    public ReceiptController(ReceiptOcrService receiptOcrService) {
        this.receiptOcrService = receiptOcrService;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> parse(@RequestPart("file") MultipartFile file) {
        // 입력 검증 실패 → BAD_REQUEST
        if (file == null || file.isEmpty()
                || file.getContentType() == null
                || !(MediaType.IMAGE_JPEG_VALUE.equals(file.getContentType())
                || MediaType.IMAGE_PNG_VALUE.equals(file.getContentType()))) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "error", Map.of("code", "BAD_REQUEST", "message", "invalid input"))
            );
        }

        try {
            ReceiptParsedResponse res = receiptOcrService.extractItemsAndTotal(file);
            return ResponseEntity.ok(ApiSuccess.of(res));
        } catch (Exception e) {
            // 서버 처리 실패 → INTERNAL_ERROR
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "error", Map.of("code", "INTERNAL_ERROR", "message", "receipt recognition failed"))
            );
        }
    }
}
