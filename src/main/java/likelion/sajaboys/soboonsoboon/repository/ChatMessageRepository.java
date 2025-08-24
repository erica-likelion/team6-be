package likelion.sajaboys.soboonsoboon.repository;

import likelion.sajaboys.soboonsoboon.domain.post.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50ByPostIdOrderByIdDesc(Long postId);

    List<ChatMessage> findByPostIdAndIdLessThanOrderByIdDesc(Long postId, Long beforeId);

    List<ChatMessage> findByPostIdAndIdGreaterThanOrderByIdAsc(Long postId, Long afterId);
}
