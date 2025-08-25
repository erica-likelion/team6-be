package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.service.ai.recommend.TextScoringRecommendationService;
import likelion.sajaboys.soboonsoboon.util.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostsRecommendationController {

    private static final int LIMIT = 3;      // 추천 개수
    private static final int CANDIDATES = 30; // 후보군 크기

    private final TextScoringRecommendationService recoService;

    public PostsRecommendationController(TextScoringRecommendationService recoService) {
        this.recoService = recoService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> recommend(@RequestParam(required = false) Post.Type type) {
        Long userId = CurrentUser.get();

        var scored = recoService.recommendForUser(userId, LIMIT, CANDIDATES, type);

        var items = new ArrayList<Map<String, Object>>();
        for (var sp : scored) {
            var p = sp.post();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("type", p.getType().name());
            m.put("title", p.getTitle());
            m.put("place", p.getPlace());
            m.put("capacity", p.getCapacity());
            m.put("currentMembers", p.getCurrentMembers());
            m.put("price", p.getPrice());
            m.put("timeStart", p.getTimeStart());
            m.put("timeEnd", p.getTimeEnd());
            m.put("createdAt", p.getCreatedAt());
            m.put("score", sp.score());
            items.add(m);
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("items", items)
        ));
    }
}
