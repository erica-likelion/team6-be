package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.post.ChatMessage;
import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.repository.ChatMessageRepository;
import likelion.sajaboys.soboonsoboon.service.ai.MessagePostedEvent;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatMessageService {
    private final ChatMessageRepository msgRepo;
    private final DomainEventPublisher eventPublisher;
    private final PostService postService;

    public ChatMessageService(ChatMessageRepository msgRepo, DomainEventPublisher eventPublisher, PostService postService) {
        this.msgRepo = msgRepo;
        this.eventPublisher = eventPublisher;
        this.postService = postService;
    }

    @Transactional
    public ChatMessage sendUserMessage(Long postId, Long userId, String content) {
        if (content == null || content.isBlank() || content.length() > 2000) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid content length");
        }

        ChatMessage saved = msgRepo.save(ChatMessage.builder()
                .postId(postId)
                .senderUserId(userId)
                .role(ChatMessage.Role.user)
                .content(content)
                .build());

        // 커밋 후 비동기 이벤트 발행 (멤버 수 확인: 1명이면 자동 응답 트리거 생략)
        Post post = postService.getOrThrow(saved.getPostId());
        if (post.getCurrentMembers() >= 2) {
            eventPublisher.publish(new MessagePostedEvent(postId, saved.getId(), userId));
        }

        return saved;
    }

    public List<ChatMessage> listLatest(Long postId, int limit) {
        // 간단 구현: Top50 고정 메서드 사용
        return msgRepo.findTop50ByPostIdOrderByIdDesc(postId);
    }

    public List<ChatMessage> listBefore(Long postId, Long beforeId) {
        if (beforeId == null || beforeId <= 0) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "beforeId must be positive");
        }
        return msgRepo.findByPostIdAndIdLessThanOrderByIdDesc(postId, beforeId);
    }

    // afterId보다 큰 신규 메시지를 오름차순으로 반환(폴링 append 용도)
    public List<ChatMessage> listAfterAsc(Long postId, Long afterId) {
        if (afterId == null || afterId < 0) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "afterId must be >= 0");
        }
        // afterId == 0이면, 모든 메시지를 의미하도록 허용(가장 처음 폴링)
        return msgRepo.findByPostIdAndIdGreaterThanOrderByIdAsc(postId, afterId);
    }
}
