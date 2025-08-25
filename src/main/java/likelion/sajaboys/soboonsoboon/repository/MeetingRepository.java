package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.post.Meeting;
import likelion.sajaboys.soboonsoboon.dto.NextMeetingDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByPostId(Long postId);

    boolean existsByPostId(Long postId);

    @Query("""
                select new likelion.sajaboys.soboonsoboon.dto.NextMeetingDto(
                    p.id,
                    p.type,
                    p.place,
                    m.time
                )
                from Meeting m
                join Post p on p.id = m.postId
                join PostMember pm on pm.postId = p.id
                where pm.userId = :userId
                  and m.time > :now
                order by m.time asc, p.id asc
            """)
    List<NextMeetingDto> findNextFutureMeetingForUser(
            @Param("userId") Long userId,
            @Param("now") Instant now,
            Pageable pageable
    );
}