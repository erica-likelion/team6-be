package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.domain.post.PostMember;
import likelion.sajaboys.soboonsoboon.dto.MemberDtos;
import likelion.sajaboys.soboonsoboon.service.PostMemberService;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import likelion.sajaboys.soboonsoboon.util.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/members")
public class PostMembersController {

    private final PostMemberService memberService;

    public PostMembersController(PostMemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<ApiSuccess<MemberDtos.JoinResponse>> join(@PathVariable Long postId) {
        Long userId = CurrentUser.get();

        PostMember m = memberService.join(postId, userId);

        var res = new MemberDtos.JoinResponse(m.getPostId(), m.getUserId(), m.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of(res));
    }
}
