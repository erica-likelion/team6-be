package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.domain.PostMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostMemberRepository extends JpaRepository<PostMember, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<PostMember> findAllByPostId(Long postId);

    long countByPostId(Long postId);

    List<PostMember> findAllByPostIdOrderByJoinedAtAsc(Long postId);

    List<PostMember> findAllByUserIdOrderByJoinedAtDesc(Long userId);

    @Query("""
                select p from PostMember pm
                join Post p on p.id = pm.postId
                where pm.userId = :userId
                order by p.lastMessageAt desc nulls last, p.createdAt desc
            """)
    Page<Post> findJoinedPostsOrderByLastMessageDesc(@Param("userId") Long userId, Pageable pageable);
}