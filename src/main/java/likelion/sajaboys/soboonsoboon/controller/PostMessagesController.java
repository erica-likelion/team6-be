package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.domain.post.ChatMessage;
import likelion.sajaboys.soboonsoboon.dto.MessageResponse;
import likelion.sajaboys.soboonsoboon.dto.MessagesPage;
import likelion.sajaboys.soboonsoboon.service.ChatMessageService;
import likelion.sajaboys.soboonsoboon.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts/{postId}/messages")
public class PostMessagesController {

    private final ChatMessageService msgService;

    public PostMessagesController(ChatMessageService msgService) {
        this.msgService = msgService;
    }

    // 1) 메시지 전송
    @PostMapping
    public ResponseEntity<ApiSuccess<MessageResponse>> send(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body
    ) {
        Long userId = CurrentUser.get();
        String content = body.get("content");
        var m = msgService.sendUserMessage(postId, userId, content);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccess.of(MapperUtil.toMessageResponse(m)));
    }

    // 2) 메시지 목록
    @GetMapping
    public ApiSuccess<MessagesPage> list(
            @PathVariable Long postId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(required = false) Long afterId
    ) {
        if (beforeId != null && afterId != null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "cannot use beforeId and afterId together");
        }
        int size = normalizeLimit(limit);

        List<ChatMessage> list;
        boolean alreadyAsc = false;

        if (afterId != null) {
            // 증분 폴링: id > afterId, ASC
            list = msgService.listAfterAsc(postId, afterId);
            if (list.size() > size) list = list.subList(0, size);
            alreadyAsc = true;
        } else if (beforeId != null) {
            // 과거 히스토리: id < beforeId, DESC
            list = msgService.listBefore(postId, beforeId);
            if (list.size() > size) list = list.subList(0, size);
        } else {
            // 기본 목록: 최신 N개(레포는 DESC)
            list = msgService.listLatest(postId, size);
        }

        // 반환은 오름차순으로 통일(클라 append 단순화)
        if (!alreadyAsc) {
            list = list.stream()
                    .sorted(Comparator.comparing(ChatMessage::getId))
                    .toList();
        }

        var items = list.stream().map(MapperUtil::toMessageResponse).toList();
        Long nextCursor = items.isEmpty() ? null : items.get(0).id() - 1;
        return ApiSuccess.of(new MessagesPage(items, nextCursor));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) return 50;
        if (limit < 1) return 1;
        return Math.min(limit, 100);
    }
}
