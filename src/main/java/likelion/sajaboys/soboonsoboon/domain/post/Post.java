package likelion.sajaboys.soboonsoboon.domain.post;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_posts_type", columnList = "type"),
                @Index(name = "idx_posts_created_at", columnList = "created_at DESC"),
                @Index(name = "idx_posts_last_message_at", columnList = "last_message_at DESC"),
                @Index(name = "idx_posts_creator", columnList = "creator_id")
        })
public class Post {

    public enum Type { shopping, sharing }
    public enum Status { open, closed }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Type type;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column
    private String content; // NULL 허용

    @Column(columnDefinition = "json")
    private String imagesJson; // URL 배열(JSON 문자열)

    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "time_start")
    private Instant timeStart; // shopping 전용

    @Column(name = "time_end")
    private Instant timeEnd;   // shopping 전용

    @Column(precision = 12, scale = 2)
    private BigDecimal price;  // sharing 전용

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    @Getter @Setter
    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "payment_requester_id")
    private Long paymentRequesterId;

    @Column(name = "payment_done_user_ids_json", columnDefinition = "json")
    private String paymentDoneUserIdsJson;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.open;
    }

    public boolean isShopping() { return type == Type.shopping; }
    public boolean isSharing() { return type == Type.sharing; }

    public void markLastMessageNow() { this.lastMessageAt = Instant.now(); }
    public void requestPayment(Long requesterId) { this.paymentRequesterId = requesterId; }
}
