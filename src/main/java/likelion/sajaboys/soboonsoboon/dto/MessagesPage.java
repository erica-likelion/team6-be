package likelion.sajaboys.soboonsoboon.dto;

import java.util.List;

public record MessagesPage(
        List<MessageResponse> items,
        Long nextCursor // 더 없으면 null
) {
}
