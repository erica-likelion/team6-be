package likelion.sajaboys.soboonsoboon.service.ai.recommend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.domain.PostMember;
import likelion.sajaboys.soboonsoboon.repository.PostMemberRepository;
import likelion.sajaboys.soboonsoboon.repository.PostRepository;
import likelion.sajaboys.soboonsoboon.service.ai.reply.OpenAiChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static likelion.sajaboys.soboonsoboon.service.ai.recommend.RecommenderPromptBuilder.*;

@Service
@Transactional(readOnly = true)
public class TextScoringRecommendationService {

    private final PostRepository postRepo;
    private final PostMemberRepository memberRepo;
    private final OpenAiChatClient chat;
    private final ObjectMapper om = new ObjectMapper();
    private final int maxTokens;

    private static final Logger log = LoggerFactory.getLogger(TextScoringRecommendationService.class);

    public TextScoringRecommendationService(PostRepository postRepo,
                                            PostMemberRepository memberRepo,
                                            OpenAiChatClient chat,
                                            @Value("${openai.max-tokens:300}") int maxTokens) {
        this.postRepo = postRepo;
        this.memberRepo = memberRepo;
        this.chat = chat;
        this.maxTokens = maxTokens;
    }

    // 타입 필터 추가 버전
    public List<ScoredPostDto> recommendForUser(Long userId, int limit, int candidateSize, Post.Type type) {
        // 1) 최근 참여 N개
        var recentIds = memberRepo.findAllByUserIdOrderByJoinedAtDesc(userId).stream()
                .limit(10).map(PostMember::getPostId).toList();
        List<Post> recent = recentIds.isEmpty() ? List.of() : postRepo.findAllById(recentIds);

        // 2) 후보군(오픈 최신) M개 + 타입 필터
        List<Post> rawCandidates = (type == null)
                ? postRepo.findByStatusOrderByCreatedAtDesc(Post.Status.open)
                : postRepo.findByStatusAndTypeOrderByCreatedAtDesc(Post.Status.open, type);
        List<Post> candidates = rawCandidates.stream()
                .limit(Math.max(candidateSize, limit))
                .collect(Collectors.toList());

        // fallback: 최근/후보 비었을 때 최신순
        if (recent.isEmpty() || candidates.isEmpty()) {
            if (recent.isEmpty()) log.debug("recent is empty, Fallback");
            if (candidates.isEmpty()) log.debug("candidate is empty, Fallback");
            return candidates.stream()
                    .limit(limit)
                    .map(p -> new ScoredPostDto(p, 0))
                    .collect(Collectors.toList());
        }

        // 3) 프롬프트 생성 (+ 타입 힌트)
        String sys = systemPrompt();
        String usr = userPrompt(recent, candidates);
        usr = appendTypeHint(usr, type);

        // 4) 모델 호출 → JSON 점수
        String json = chat.complete(sys, usr, maxTokens);
        List<Scored> scored = parseScores(json);

        // 로그
        logPromptAndResponse(sys, usr, json);

        if (scored.isEmpty()) {
            // 파싱 실패/빈 결과 → 최신순 fallback
            return candidates.stream()
                    .limit(limit)
                    .map(p -> new ScoredPostDto(p, 0))
                    .collect(Collectors.toList());
        }

        // 5) 후보와 점수 매칭, 동점이면 최근 createdAt 우선
        Map<Long, Post> candMap = candidates.stream().collect(Collectors.toMap(Post::getId, p -> p));
        List<ScoredPost> ranked = new ArrayList<>();
        for (Scored s : scored) {
            Post p = candMap.get(s.id());
            if (p != null) ranked.add(new ScoredPost(p, s.score()));
        }
        if (ranked.isEmpty()) {
            return candidates.stream()
                    .limit(limit)
                    .map(p -> new ScoredPostDto(p, 0))
                    .collect(Collectors.toList());
        }
        ranked.sort((a, b) -> {
            int c = Integer.compare(b.score, a.score);
            if (c != 0) return c;
            Instant ca = b.post.getCreatedAt();
            Instant cb = a.post.getCreatedAt();
            return cb.compareTo(ca);
        });

        // 6) 상위 limit 반환
        return ranked.stream()
                .limit(limit)
                .map(sp -> new ScoredPostDto(sp.post, sp.score))
                .collect(Collectors.toList());
    }

    // 이하 기존 메서드/유틸 그대로 (parseScores, clamp, Scored, ScoredPost, logPromptAndResponse, truncate)

    private List<Scored> parseScores(String json) {
        if (json == null || json.isBlank()) {
            log.warn("[Reco][AI] parseScores got blank json");
            return List.of();
        }
        try {
            int l = json.indexOf('[');
            int r = json.lastIndexOf(']');
            if (l >= 0 && r > l) {
                json = json.substring(l, r + 1);
            } else {
                log.warn("[Reco][AI] parseScores: JSON array brackets not found. head={}",
                        json.substring(0, Math.min(1000, json.length())));
                return List.of();
            }
            var list = om.readValue(json, new TypeReference<List<Scored>>() {
            });
            return list.stream()
                    .filter(s -> s.id != null && s.id > 0)
                    .map(s -> new Scored(s.id, clamp(s.score)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[Reco][AI] parseScores error: {} | raw head={}",
                    e, json.substring(0, Math.min(1000, json.length())));
            return List.of();
        }
    }

    private int clamp(Integer x) {
        if (x == null) return 0;
        if (x < 0) return 0;
        if (x > 100) return 100;
        return x;
    }

    public record Scored(Long id, Integer score) {
        @com.fasterxml.jackson.annotation.JsonCreator
        public Scored(@com.fasterxml.jackson.annotation.JsonProperty("id") Long id,
                      @com.fasterxml.jackson.annotation.JsonProperty("score") Integer score) {
            this.id = id;
            this.score = score;
        }
    }

    private static class ScoredPost {
        final Post post;
        final int score;

        ScoredPost(Post p, int s) {
            this.post = p;
            this.score = s;
        }
    }

    private void logPromptAndResponse(String systemPrompt, String userPrompt, String rawResponse) {
        try {
            String sys = truncate(systemPrompt);
            String usr = truncate(userPrompt);
            String rsp = (rawResponse == null) ? "null" : truncate(rawResponse);
            log.debug("[Reco][AI] prompt+response\nsystem:\n{}\n\nuser:\n{}\n\nresponse(len={}):\n{}",
                    sys, usr, (rawResponse == null ? 0 : rawResponse.length()), rsp);
        } catch (Exception e) {
            log.warn("[Reco][AI] logPromptAndResponse failed: {}", e.toString());
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= 4000) return s;
        int head = 4000 / 2;
        int tail = 4000 - head;
        return s.substring(0, head) + "\n...[truncated]...\n" + s.substring(s.length() - tail);
    }
}
