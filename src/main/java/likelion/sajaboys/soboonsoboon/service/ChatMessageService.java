package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.post.ChatMessage;
import likelion.sajaboys.soboonsoboon.repository.ChatMessageRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatMessageService {
    private final ChatMessageRepository msgRepo;

    public ChatMessageService(ChatMessageRepository msgRepo) {
        this.msgRepo = msgRepo;
    }

    @Transactional
    public ChatMessage sendUserMessage(Long postId, Long userId, String content) {
        if (content == null || content.isBlank() || content.length() > 2000) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid content length");
        }
        ChatMessage m = ChatMessage.builder()
                .postId(postId)
                .senderUserId(userId)
                .role(ChatMessage.Role.user)
                .content(content)
                .build();
        return msgRepo.save(m);
    }

    public List<ChatMessage> listLatest(Long postId, int limit) {
        // 간단 구현: Top50 고정 메서드 사용. 필요하면 limit 반영하도록 레포 확장
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
