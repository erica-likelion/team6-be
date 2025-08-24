package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.repository.PostRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepo;

    public PostService(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    public Post getOrThrow(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "post not found"));
    }

    @Transactional
    public Post createShopping(String title, String content, String imagesJson, String place, Integer capacity,
                               Instant timeStart, Instant timeEnd, Long creatorId) {
        if (title == null || title.isBlank() || title.length() > 200) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid title");
        }
        if (place == null || place.isBlank() || place.length() > 255) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid place");
        }
        if (capacity == null || capacity < 1) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid capacity");
        }
        if (timeStart == null || timeEnd == null || !timeStart.isBefore(timeEnd)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "invalid field combination for type");
        }
        Post p = Post.builder()
                .type(Post.Type.shopping)
                .title(title)
                .content(content)
                .imagesJson(imagesJson)
                .place(place)
                .capacity(capacity)
                .timeStart(timeStart)
                .timeEnd(timeEnd)
                .creatorId(creatorId)
                .build();
        return postRepo.save(p);
    }

    @Transactional
    public Post createSharing(String title, String content, String imagesJson, String place, Integer capacity,
                              BigDecimal price, Long creatorId) {
        if (title == null || title.isBlank() || title.length() > 200) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid title");
        }
        if (place == null || place.isBlank() || place.length() > 255) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid place");
        }
        if (capacity == null || capacity < 1) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "invalid capacity");
        }
        if (price == null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "invalid field combination for type");
        }
        Post p = Post.builder()
                .type(Post.Type.sharing)
                .title(title)
                .content(content)
                .imagesJson(imagesJson)
                .place(place)
                .capacity(capacity)
                .price(price)
                .creatorId(creatorId)
                .build();
        return postRepo.save(p);
    }
}
