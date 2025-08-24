package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.domain.post.Meeting;
import likelion.sajaboys.soboonsoboon.repository.MeetingRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class MeetingService {
    private final MeetingRepository meetingRepo;

    public MeetingService(MeetingRepository meetingRepo) {
        this.meetingRepo = meetingRepo;
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
        return meetingRepo.save(Meeting.builder().postId(postId).time(time).build());
    }
}
