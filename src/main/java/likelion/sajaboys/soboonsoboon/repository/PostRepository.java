package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("update Post p set p.lastMessageAt = :ts where p.id = :postId")
    void updateLastMessageAt(@Param("postId") Long postId, @Param("ts") Instant ts);

    List<Post> findByStatusOrderByCreatedAtDesc(Post.Status status);

    // 리스트 조회용 페이징
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findAllByTypeOrderByCreatedAtDesc(Post.Type type, Pageable pageable);

    Page<Post> findAllByStatusOrderByCreatedAtDesc(Post.Status status, Pageable pageable);

    Page<Post> findAllByTypeAndStatusOrderByCreatedAtDesc(Post.Type type, Post.Status status, Pageable pageable);
}
