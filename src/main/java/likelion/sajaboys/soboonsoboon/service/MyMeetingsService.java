package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.dto.NextMeetingDto;
import likelion.sajaboys.soboonsoboon.repository.MeetingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MyMeetingsService {

    private final MeetingRepository meetingRepository;

    public MyMeetingsService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public Optional<NextMeetingDto> findNextFutureMeetingForUser(Long userId) {
        var now = Instant.now();
        var list = meetingRepository.findNextFutureMeetingForUser(userId, now, PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
