package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.dto.PostDtos;
import likelion.sajaboys.soboonsoboon.repository.PostRepository;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostQueriesController {

    private final PostRepository postRepo;

    public PostQueriesController(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @GetMapping
    public ApiSuccess<PostDtos.PageResponse> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null) ? 20 : Math.max(1, Math.min(size, 50));
        var pageable = PageRequest.of(p, s);

        Page<Post> result;
        if (type != null && status != null) {
            result = postRepo.findAllByTypeAndStatusOrderByCreatedAtDesc(
                    Post.Type.valueOf(type), Post.Status.valueOf(status), pageable);
        } else if (type != null) {
            result = postRepo.findAllByTypeOrderByCreatedAtDesc(Post.Type.valueOf(type), pageable);
        } else if (status != null) {
            result = postRepo.findAllByStatusOrderByCreatedAtDesc(Post.Status.valueOf(status), pageable);
        } else {
            result = postRepo.findAllByOrderByCreatedAtDesc(pageable);
        }

        List<PostDtos.Item> items = result.getContent().stream().map(pst ->
                new PostDtos.Item(
                        pst.getId(),
                        pst.getType().name(),
                        pst.getStatus().name(),
                        pst.getTitle(),
                        pst.getPlace(),
                        pst.getCapacity(),
                        pst.getCurrentMembers(),
                        pst.getPrice(),
                        pst.getTimeStart(),
                        pst.getTimeEnd(),
                        pst.getCreatedAt()
                )
        ).toList();

        var pageMeta = new PostDtos.PageMeta(result.getNumber(), result.getSize(), result.getTotalElements());
        return ApiSuccess.of(new PostDtos.PageResponse(items, pageMeta));
    }
}
