package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.service.MyMeetingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/meetings")
public class MyMeetingsController {

    private final MyMeetingsService myMeetingsService;

    public MyMeetingsController(MyMeetingsService myMeetingsService) {
        this.myMeetingsService = myMeetingsService;
    }

    @GetMapping("/next")
    public ResponseEntity<?> getNextFutureMeeting() {
        Long userId = likelion.sajaboys.soboonsoboon.util.CurrentUser.get();
        var opt = myMeetingsService.findNextFutureMeetingForUser(userId);
        if (opt.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(likelion.sajaboys.soboonsoboon.util.ApiSuccess.of(opt.get()));
    }
}

