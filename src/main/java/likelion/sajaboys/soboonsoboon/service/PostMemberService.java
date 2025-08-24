package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.post.PostMember;
import likelion.sajaboys.soboonsoboon.repository.PostMemberRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostMemberService {
    private final PostMemberRepository memberRepo;

    public PostMemberService(PostMemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    @Transactional
    public PostMember join(Long postId, Long userId, int capacity) {
        if (memberRepo.existsByPostIdAndUserId(postId, userId)) {
            throw new ApiException(ErrorCode.CONFLICT, "already joined");
        }
        int current = memberRepo.findAllByPostId(postId).size();
        if (current >= capacity) {
            throw new ApiException(ErrorCode.CONFLICT, "capacity exceeded");
        }
        PostMember m = PostMember.builder()
                .postId(postId)
                .userId(userId)
                .role(PostMember.Role.member)
                .build();
        return memberRepo.save(m);
    }

    @Transactional
    public void leave(Long postId, Long userId) {
        var list = memberRepo.findAllByPostId(postId);
        var me = list.stream().filter(m -> m.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "membership not found"));
        // 호스트 정책 필요 시 여기서 가드
        memberRepo.delete(me);
    }

    public int countMembers(Long postId) {
        return memberRepo.findAllByPostId(postId).size();
    }
}
