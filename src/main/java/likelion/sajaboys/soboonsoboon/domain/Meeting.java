package likelion.sajaboys.soboonsoboon.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "meetings",
        uniqueConstraints = @UniqueConstraint(name = "uk_meeting_post", columnNames = "post_id"),
        indexes = @Index(name = "idx_meeting_time", columnList = "time"))
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "time", nullable = false)
    private Instant time;
}
