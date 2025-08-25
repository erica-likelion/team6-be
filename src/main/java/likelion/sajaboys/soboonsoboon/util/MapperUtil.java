package likelion.sajaboys.soboonsoboon.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.sajaboys.soboonsoboon.domain.ChatMessage;
import likelion.sajaboys.soboonsoboon.dto.ChatMessageDtos;

import java.util.List;

public final class MapperUtil {

    private static final ObjectMapper om = new ObjectMapper();

    private MapperUtil() {
    }

    // ChatMessage -> MessageResponse
    public static ChatMessageDtos.MessageResponse toMessageResponse(ChatMessage m) {
        return new ChatMessageDtos.MessageResponse(
                m.getId(),
                m.getPostId(),
                m.getSenderUserId(),
                m.getRole() == null ? null : m.getRole().name(),
                m.getContent(),
                m.getSystemType() == null ? null : m.getSystemType().name(),
                jsonToObject(m.getSystemPayloadJson()),
                m.getVisibilityScope() == null ? null : m.getVisibilityScope().name(),
                m.getVisibilityUserId(),
                m.getCreatedAt()
        );
        // createdAt은 엔티티 @PrePersist에서 세팅됨
    }

    // 문자열 JSON -> Object(Map/List/Value)
    public static Object jsonToObject(String json) {
        try {
            if (json == null || json.isBlank()) return null;
            return om.readValue(json, Object.class);
        } catch (Exception e) {
            // 파싱 실패 시 null 반환(시연 간결성 우선)
            return null;
        }
    }

    // Object -> 문자열 JSON
    public static String toJson(Object value) {
        try {
            if (value == null) return null;
            return om.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    // imagesJson 전용: 문자열 JSON 배열 -> List<String>
    public static List<String> parseImagesJson(String imagesJson) {
        try {
            if (imagesJson == null || imagesJson.isBlank()) return List.of();
            return om.readValue(imagesJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
