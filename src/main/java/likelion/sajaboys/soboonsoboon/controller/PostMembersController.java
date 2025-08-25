package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.domain.PostMember;
import likelion.sajaboys.soboonsoboon.dto.PostMemberDtos;
import likelion.sajaboys.soboonsoboon.service.PostMemberService;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import likelion.sajaboys.soboonsoboon.util.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/members")
public class PostMembersController {

    private final PostMemberService memberService;

    public PostMembersController(PostMemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<ApiSuccess<PostMemberDtos.JoinResponse>> join(@PathVariable Long postId) {
        Long userId = CurrentUser.get();

        PostMember m = memberService.join(postId, userId);

        var res = new PostMemberDtos.JoinResponse(m.getPostId(), m.getUserId(), m.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of(res));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> leave(@PathVariable Long postId) {
        Long userId = CurrentUser.get();
        memberService.leave(postId, userId);

        return ResponseEntity.noContent().build();
    }
}
