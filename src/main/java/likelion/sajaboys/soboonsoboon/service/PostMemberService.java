package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.domain.post.PostMember;
import likelion.sajaboys.soboonsoboon.repository.PostMemberRepository;
import likelion.sajaboys.soboonsoboon.repository.PostRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostMemberService {
    private final PostMemberRepository memberRepo;
    private final PostRepository postRepo;

    public PostMemberService(PostMemberRepository memberRepo, PostRepository postRepo) {
        this.memberRepo = memberRepo;
        this.postRepo = postRepo;
    }

    @Transactional
    public PostMember join(Long postId, Long userId) {
        if (memberRepo.existsByPostIdAndUserId(postId, userId)) {
            throw new ApiException(ErrorCode.CONFLICT, "already joined");
        }

        // Post 읽기 + 동시성 가드
        Post post = postRepo.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "post not found"));

        // 닫힘 / 정원 검사
        if (post.isClosed()) {
            throw new ApiException(ErrorCode.CONFLICT, "post closed");
        }
        if (post.isFull()) {
            throw new ApiException(ErrorCode.CONFLICT, "capacity exceeded");
        }

        PostMember m = PostMember.builder()
                .postId(postId)
                .userId(userId)
                .role(PostMember.Role.member)
                .build();
        PostMember saved = memberRepo.save(m);

        // 현재 인원 +1
        post.increaseMember();
        postRepo.save(post);

        return saved;
    }

    @Transactional
    public void leave(Long postId, Long userId) {
        var list = memberRepo.findAllByPostId(postId);
        var me = list.stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "membership not found"));

        memberRepo.delete(me);

        Post post = postRepo.findById(postId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "post not found"));

        // 현재 인원 -1
        post.decreaseMember();
        postRepo.save(post);
    }

    public List<PostMember> findMembers(Long postId) {
        return memberRepo.findAllByPostIdOrderByJoinedAtAsc(postId);
    }

}
