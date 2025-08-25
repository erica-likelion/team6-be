package likelion.sajaboys.soboonsoboon.controller;

import jakarta.validation.Valid;
import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.dto.PostDtos;
import likelion.sajaboys.soboonsoboon.service.PostService;
import likelion.sajaboys.soboonsoboon.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostService postService;

    public PostsController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiSuccess<PostDtos.CreateResponse>> create(@Valid @RequestBody PostDtos.CreateRequest req) {
        Long creatorId = CurrentUser.get();
        String imagesJson = MapperUtil.toJson(req.images());
        Post saved;

        if ("shopping".equals(req.type())) {
            saved = postService.createShopping(
                    req.title(),
                    req.content(),
                    imagesJson,
                    req.place(),
                    req.capacity(),
                    req.timeStart(),
                    req.timeEnd(),
                    creatorId
            );
        } else if ("sharing".equals(req.type())) {
            BigDecimal price = req.price();
            saved = postService.createSharing(
                    req.title(),
                    req.content(),
                    imagesJson,
                    req.place(),
                    req.capacity(),
                    price,
                    creatorId
            );
        } else {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid type");
        }

        var res = new PostDtos.CreateResponse(saved.getId(), saved.getType().name(), saved.getStatus().name(), saved.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of(res));
    }

    @GetMapping("/{id}")
    public ApiSuccess<PostDtos.Detail> get(@PathVariable Long id) {
        Post p = postService.getOrThrow(id);
        List<String> images = MapperUtil.parseImagesJson(p.getImagesJson());
        var res = new PostDtos.Detail(
                p.getId(),
                p.getType().name(),
                p.getStatus().name(),
                p.getTitle(),
                p.getContent(),
                images,
                p.getPlace(),
                p.getCapacity(),
                p.getCurrentMembers(),
                p.getTimeStart(),
                p.getTimeEnd(),
                p.getPrice(),
                p.getCreatorId(),
                p.getCreatedAt(),
                p.getLastMessageAt()
        );
        return ApiSuccess.of(res);
    }
}
