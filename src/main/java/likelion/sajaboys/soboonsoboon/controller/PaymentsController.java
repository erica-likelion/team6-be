package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.dto.PaymentDtos;
import likelion.sajaboys.soboonsoboon.service.PostMemberService;
import likelion.sajaboys.soboonsoboon.service.PostService;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import likelion.sajaboys.soboonsoboon.util.CurrentUser;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/posts/{postId}/payment")
public class PaymentsController {

    private final PostService postService;
    private final PostMemberService memberService;

    public PaymentsController(PostService postService, PostMemberService memberService) {
        this.postService = postService;
        this.memberService = memberService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiSuccess<PaymentDtos.RequestResponse>> request(@PathVariable Long postId) {
        Long userId = CurrentUser.get();
        // 멤버 아니면 403
        if (!isMember(postId, userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "not a member");
        }
        var p = postService.requestPayment(postId, userId);
        var res = new PaymentDtos.RequestResponse(p.getPaymentRequesterId(), Instant.now());
        return ResponseEntity.ok(ApiSuccess.of(res));
    }

    @PostMapping("/done")
    public ResponseEntity<ApiSuccess<PaymentDtos.DoneResponse>> done(@PathVariable Long postId) {
        Long userId = CurrentUser.get();
        var p = postService.markPaymentDone(postId, userId);
        int total = p.getCurrentMembers();
        var res = new PaymentDtos.DoneResponse(userId, countDone(p.getPaymentDoneUserIdsJson()), total);
        return ResponseEntity.ok(ApiSuccess.of(res));
    }

    private boolean isMember(Long postId, Long userId) {
        try {
            return memberService.findMembers(postId).stream().anyMatch(m -> m.getUserId().equals(userId));
        } catch (Exception e) {
            return false;
        }
    }

    private int countDone(String json) {
        try {
            if (json == null || json.isBlank()) return 0;
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<Long> ids = om.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<>() {
            });
            return ids == null ? 0 : ids.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
