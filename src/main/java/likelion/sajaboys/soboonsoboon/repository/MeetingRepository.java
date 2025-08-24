package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.post.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByPostId(Long postId);

    boolean existsByPostId(Long postId);
}