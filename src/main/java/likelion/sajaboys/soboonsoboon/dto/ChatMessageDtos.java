package likelion.sajaboys.soboonsoboon.dto;

import java.time.Instant;
import java.util.List;

public class ChatMessageDtos {
    public record MessageResponse(
            Long id,
            Long postId,
            Long senderUserId,
            String role,               // 'user' | 'system'
            String content,
            String systemType,         // enum name or null
            Object systemPayload,      // JSON -> Map/List/Object or null
            String visibilityScope,    // enum name or null
            Long visibilityUserId,     // 대상 유저 or null
            Instant createdAt
    ) {
    }

    public record MessagesPage(
            List<ChatMessageDtos.MessageResponse> items,
            Long nextCursor // 더 없으면 null
    ) {
    }
}
