package likelion.sajaboys.soboonsoboon.service.ai.recommend;

import likelion.sajaboys.soboonsoboon.domain.post.Post;

import java.util.List;

import static likelion.sajaboys.soboonsoboon.util.TextSummarizer.briefContent;
import static likelion.sajaboys.soboonsoboon.util.TextSummarizer.oneLinePost;

public final class RecommenderPromptBuilder {

    private RecommenderPromptBuilder() {
    }

    public static String systemPrompt() {
        return """
                너는 추천 랭커다. 아래 지시를 반드시 따른다.
                - 한국어로만 생각하되, 출력은 반드시 JSON만 반환한다.
                - JSON 배열 형식으로 반환: [{"id": <후보ID>, "score": <0~100 정수>} , ...]
                - 불필요한 텍스트/설명/코드 블록/문장은 출력하지 않는다. 오직 JSON만 반환한다.
                - 다시 한번 강조한다. 반드시 JSON만 반환한다. 불필요한 텍스트/설명/코드 블록/문장은 출력하지 않는다.
                - 점수 기준: 최근 참여 모임과 주제/장소/가격/시간 맥락이 비슷할수록 높은 점수.
                - 같은 후보에 대해 중복 항목을 만들지 않는다.
                """;
    }

    public static String userPrompt(List<Post> recent, List<Post> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("최근 참여 모임 요약:\n");
        for (Post p : recent) {
            sb.append("- ").append(oneLinePost(p));
            String body = briefContent(p.getContent(), 120);
            if (!body.isBlank()) sb.append(" | ").append(body);
            sb.append("\n");
        }

        sb.append("\n후보 모임 목록:\n");
        for (Post p : candidates) {
            sb.append("- id=").append(p.getId()).append(" :: ").append(oneLinePost(p));
            String body = briefContent(p.getContent(), 120);
            if (!body.isBlank()) sb.append(" | ").append(body);
            sb.append("\n");
        }

        sb.append("""
                \n요청:
                - 위 후보 각각에 대해 0~100 점수를 매겨 JSON 배열만 출력하라.
                - 점수가 같으면 최근성(최근 생성된 모임)에 가점하라.
                - 출력 예시: [{"id": 123, "score": 78}, {"id": 456, "score": 65}]
                """);
        return sb.toString();
    }

    public static String appendTypeHint(String baseUserPrompt, Post.Type type) {
        if (type == null) return baseUserPrompt;
        return baseUserPrompt + "\n요청된 모임 타입: " + type.name() + "\n- 위 타입과의 주제/맥락 유사성이 높을수록 점수를 높여라.\n";
    }
}
