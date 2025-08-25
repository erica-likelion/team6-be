package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.Meeting;
import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.repository.MeetingRepository;
import likelion.sajaboys.soboonsoboon.repository.PostRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class MeetingService {
    private final MeetingRepository meetingRepo;
    private final PostRepository postRepo;

    public MeetingService(MeetingRepository meetingRepo, PostRepository postRepo) {
        this.meetingRepo = meetingRepo;
        this.postRepo = postRepo;
    }

    public Meeting getOrNull(Long postId) {
        return meetingRepo.findByPostId(postId).orElse(null);
    }

    @Transactional
    public Meeting create(Long postId, Instant time) {
        if (time.isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "time in the past");
        }
        if (meetingRepo.findByPostId(postId).isPresent()) {
            throw new ApiException(ErrorCode.CONFLICT, "meeting already exists for this post");
        }

        // Meeting 생성
        Meeting meeting = meetingRepo.save(Meeting.builder().postId(postId).time(time).build());

        // Post 상태를 closed로 변경
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "post not found"));
        if (post.getStatus() != Post.Status.closed) {
            post.close();
            postRepo.save(post);
        }

        return meeting;
    }
}
