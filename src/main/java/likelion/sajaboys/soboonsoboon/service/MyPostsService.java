package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.ChatMessage;
import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.dto.MyPostsDtos;
import likelion.sajaboys.soboonsoboon.repository.ChatMessageRepository;
import likelion.sajaboys.soboonsoboon.repository.PostMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MyPostsService {

    private final PostMemberRepository memberRepo;
    private final ChatMessageRepository msgRepo;

    public MyPostsService(PostMemberRepository memberRepo, ChatMessageRepository msgRepo) {
        this.memberRepo = memberRepo;
        this.msgRepo = msgRepo;
    }

    public Page<MyPostsDtos.Item> listJoinedWithLastMessage(Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 50)));

        Page<Post> postsPage = memberRepo.findJoinedPostsOrderByLastMessageDesc(userId, pageable);

        List<Post> posts = postsPage.getContent();
        if (posts.isEmpty()) {
            return new PageImpl<>(List.of(), postsPage.getPageable(), postsPage.getTotalElements());
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        Map<Long, ChatMessage> tmpMap = Collections.emptyMap();
        if (!postIds.isEmpty()) {
            List<Object[]> idRows = msgRepo.findLastMessageIdByPostIds(postIds);

            List<Long> lastIds = idRows.stream()
                    .map(r -> (Long) r[1]) // r[1] = lastId
                    .filter(Objects::nonNull)
                    .toList();

            List<ChatMessage> msgs = lastIds.isEmpty() ? List.of() : msgRepo.findByIdIn(lastIds);

            Map<Long, ChatMessage> byMessageId = msgs.stream()
                    .collect(Collectors.toMap(ChatMessage::getId, m -> m));

            Map<Long, ChatMessage> byPost = new HashMap<>();
            for (Object[] row : idRows) {
                Long pid = (Long) row[0]; // postId
                Long mid = (Long) row[1]; // lastId
                if (pid == null || mid == null) continue;
                ChatMessage m = byMessageId.get(mid);
                if (m != null) {
                    byPost.put(pid, m);
                }
            }
            tmpMap = byPost;
        }

        final Map<Long, ChatMessage> lastMsgMap = tmpMap;

        List<MyPostsDtos.Item> items = posts.stream().map(p -> {
            ChatMessage m = lastMsgMap.get(p.getId());
            return new MyPostsDtos.Item(
                    p.getId(),
                    p.getType().name(),
                    p.getStatus().name(),
                    p.getTitle(),
                    p.getPlace(),
                    p.getCapacity(),
                    p.getCurrentMembers(),
                    p.getCreatedAt(),
                    p.getLastMessageAt(),
                    m == null ? null : m.getId(),
                    m == null ? null : m.getSenderUserId(),
                    m == null ? null : (m.getRole() == null ? null : m.getRole().name()),
                    m == null ? null : m.getContent(),
                    m == null ? null : m.getCreatedAt()
            );
        }).toList();

        return new PageImpl<>(items, postsPage.getPageable(), postsPage.getTotalElements());
    }
}
