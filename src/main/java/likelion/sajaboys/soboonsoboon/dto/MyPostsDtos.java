package likelion.sajaboys.soboonsoboon.dto;

public class MyPostsDtos {
    public record Item(
            Long id,
            String type,
            String status,
            String title,
            String place,
            Integer capacity,
            Integer currentMembers,
            java.time.Instant createdAt,
            java.time.Instant lastMessageAt,
            Long lastMessageId, // 최신 메시지
            Long lastMessageSenderUserId,
            String lastMessageRole,
            String lastMessageContent,
            java.time.Instant lastMessageCreatedAt
    ) {
    }

    public record PageResponse(java.util.List<Item> items, PostDtos.PageMeta page) {
    }
}
