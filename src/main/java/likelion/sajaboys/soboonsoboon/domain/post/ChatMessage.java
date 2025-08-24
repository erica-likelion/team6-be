package likelion.sajaboys.soboonsoboon.domain.post;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_messages_post_id_id_desc", columnList = "post_id, id DESC"),
                @Index(name = "idx_chat_messages_sender", columnList = "sender_user_id")
        })
public class ChatMessage {

    public enum Role {user, system}

    public enum SystemType {
        MEETING_CREATED,
        PAYMENT_REQUESTED,
        GO_TO_PAYMENT,
        GO_TO_PAYMENT_DONE,
        PAYMENT_FINISHED_NOTICE,
        SHOPPING_ENDED
    }

    public enum VisibilityScope {ALL, REQUESTER_ONLY, EXCLUDE_REQUESTER, USER_ONLY}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_type", length = 40)
    private SystemType systemType;

    @Column(name = "system_payload_json", columnDefinition = "json")
    private String systemPayloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_scope", length = 32)
    private VisibilityScope visibilityScope;

    @Column(name = "visibility_user_id")
    private Long visibilityUserId; // USER_ONLY/REQUESTER_ONLY 대상 유저

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (visibilityScope == null) visibilityScope = VisibilityScope.ALL;
    }
}
