package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.dto.MyPostsDtos;
import likelion.sajaboys.soboonsoboon.service.MyPostsService;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/posts")
public class MyPostsController {

    private final MyPostsService svc;

    public MyPostsController(MyPostsService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiSuccess<MyPostsDtos.PageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = likelion.sajaboys.soboonsoboon.util.CurrentUser.get();
        var result = svc.listJoinedWithLastMessage(userId, page, size);

        var pageMeta = new likelion.sajaboys.soboonsoboon.dto.PostDtos.PageMeta(
                result.getNumber(), result.getSize(), result.getTotalElements()
        );
        return likelion.sajaboys.soboonsoboon.util.ApiSuccess.of(
                new MyPostsDtos.PageResponse(result.getContent(), pageMeta)
        );
    }
}
