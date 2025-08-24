package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Query("update Post p set p.lastMessageAt = :ts where p.id = :postId")
    void updateLastMessageAt(@Param("postId") Long postId, @Param("ts") Instant ts);
}
