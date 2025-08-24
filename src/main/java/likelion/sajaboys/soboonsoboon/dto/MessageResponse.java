package likelion.sajaboys.soboonsoboon.dto;

import java.time.Instant;

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
