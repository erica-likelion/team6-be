package likelion.sajaboys.soboonsoboon.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.repository.PostRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepo;
    private final ObjectMapper om = new ObjectMapper();

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

    @Transactional
    public void touchLastMessageAt(Long postId, Instant now) {
        postRepo.updateLastMessageAt(postId, now);
    }

    // 결제 요청
    @Transactional
    public Post requestPayment(Long postId, Long requesterId) {
        Post p = getOrThrow(postId);
        if (p.getPaymentRequesterId() != null) {
            throw new ApiException(ErrorCode.CONFLICT, "payment already requested");
        }
        p.requestPayment(requesterId);
        return postRepo.save(p);
    }

    // 정산 완료 표시
    @Transactional
    public Post markPaymentDone(Long postId, Long userId) {
        Post p = getOrThrow(postId);
        List<Long> done = parseIds(p.getPaymentDoneUserIdsJson());
        if (!done.contains(userId)) {
            done.add(userId);
            p.setPaymentDoneUserIdsJson(toJson(done));
            postRepo.save(p);
        }
        return p;
    }

    private List<Long> parseIds(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return om.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String toJson(Object v) {
        try {
            if (v == null) return null;
            return om.writeValueAsString(v);
        } catch (Exception e) {
            return null;
        }
    }
}
