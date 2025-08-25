package likelion.sajaboys.soboonsoboon.controller;

import likelion.sajaboys.soboonsoboon.domain.post.Post;
import likelion.sajaboys.soboonsoboon.service.ai.recommend.TextScoringRecommendationService;
import likelion.sajaboys.soboonsoboon.util.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostsRecommendationController {

    private static final int LIMIT = 3;       // 고정 추천 개수
    private static final int CANDIDATES = 30; // 고정 후보군 크기

    private final TextScoringRecommendationService recoService;

    public PostsRecommendationController(TextScoringRecommendationService recoService) {
        this.recoService = recoService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> recommend() {
        // 테스트 사용자 고정
        Long userId = CurrentUser.get();

        // 추천 계산(텍스트 스코어링 기반)
        // 모델 점수 파싱 실패/최근 참여 없음 시 최신 오픈 포스트로 폴백
        var scored = recoService.recommendForUser(userId, LIMIT, CANDIDATES);

        // 점수 노출 요구사항: 항상 score 포함
        // - TextScoringRecommendationService가 점수를 함께 반환하지 않는 경우가 있으므로
        //   여기서는 점수 정보를 받을 수 있도록 서비스에 보조 메서드를 두거나,
        //   컨트롤러 단계에서 0으로 채워 일관성 유지
        // 권장: 서비스 측에서 점수와 매핑된 DTO를 반환하도록 확장
        // 임시: score를 0으로 채움(서비스가 점수 포함 DTO를 제공하면 교체)
        var items = new ArrayList<Map<String, Object>>();
        for (var sp : scored) {
            Post p = sp.post();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("type", p.getType().name());
            m.put("title", p.getTitle());
            m.put("place", p.getPlace());
            m.put("capacity", p.getCapacity());
            m.put("currentMembers", p.getCurrentMembers());
            m.put("price", p.getPrice()); // 숫자(소수) 유지
            m.put("timeStart", p.getTimeStart()); // ISO-8601 직렬화
            m.put("timeEnd", p.getTimeEnd());
            m.put("createdAt", p.getCreatedAt());
            m.put("score", sp.score());
            items.add(m);
        }

        // 항상 200 OK + items 배열(빈 배열 가능)
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("items", items)
        ));
    }
}
