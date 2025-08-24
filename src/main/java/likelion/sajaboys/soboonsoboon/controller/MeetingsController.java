package likelion.sajaboys.soboonsoboon.controller;

import jakarta.validation.Valid;
import likelion.sajaboys.soboonsoboon.dto.MeetingDtos;
import likelion.sajaboys.soboonsoboon.service.MeetingService;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/meeting")
public class MeetingsController {

    private final MeetingService meetingService;

    public MeetingsController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<?> get(@PathVariable Long postId) {
        var m = meetingService.getOrNull(postId);
        if (m == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiSuccess.of(null)); // {} 반환
        }
        return ResponseEntity.ok(ApiSuccess.of(new MeetingDtos.Response(m.getPostId(), m.getTime())));
    }

    @PostMapping
    public ResponseEntity<ApiSuccess<MeetingDtos.Response>> create(
            @PathVariable Long postId,
            @Valid @RequestBody MeetingDtos.CreateRequest req
    ) {
        var m = meetingService.create(postId, req.time());
        var res = new MeetingDtos.Response(m.getPostId(), m.getTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of(res));
    }
}
