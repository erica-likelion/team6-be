package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.post.PostMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMemberRepository extends JpaRepository<PostMember, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<PostMember> findAllByPostId(Long postId);

    long countByPostId(Long postId);

    List<PostMember> findAllByPostIdOrderByJoinedAtAsc(Long postId);
}