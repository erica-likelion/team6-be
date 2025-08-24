package likelion.sajaboys.soboonsoboon.domain.post;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "post_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_member", columnNames = {"post_id", "user_id"}),
        indexes = @Index(name = "idx_post_member_user", columnList = "user_id"))
public class PostMember {

    public enum Role {host, member}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) joinedAt = Instant.now();
        if (role == null) role = Role.member;
    }
}
