package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50ByPostIdOrderByIdDesc(Long postId);

    List<ChatMessage> findByPostIdAndIdLessThanOrderByIdDesc(Long postId, Long beforeId);

    List<ChatMessage> findByPostIdAndIdGreaterThanOrderByIdAsc(Long postId, Long afterId);

    @Query("""
                select m.postId as postId, max(m.id) as lastId
                from ChatMessage m
                where m.postId in :postIds
                group by m.postId
            """)
    List<Object[]> findLastMessageIdByPostIds(@Param("postIds") Collection<Long> postIds);

    List<ChatMessage> findByIdIn(Collection<Long> ids);
}
