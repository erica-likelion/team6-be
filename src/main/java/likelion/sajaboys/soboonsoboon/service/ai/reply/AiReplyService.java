package likelion.sajaboys.soboonsoboon.service.ai.reply;

import likelion.sajaboys.soboonsoboon.domain.ChatMessage;
import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.domain.PostMember;
import likelion.sajaboys.soboonsoboon.repository.ChatMessageRepository;
import likelion.sajaboys.soboonsoboon.service.PostMemberService;
import likelion.sajaboys.soboonsoboon.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Service
public class AiReplyService {

    private final ChatMessageRepository msgRepo;
    private final OpenAiChatClient chatClient;
    private final PostMemberService memberService;
    private final PostService postService;

    private final int maxTokens;

    public AiReplyService(ChatMessageRepository msgRepo,
                          OpenAiChatClient chatClient,
                          PostMemberService memberService,
                          PostService postService,
                          @Value("${openai.max-tokens:120}") int maxTokens) {
        this.msgRepo = msgRepo;
        this.chatClient = chatClient;
        this.memberService = memberService;
        this.postService = postService;
        this.maxTokens = maxTokens;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onUserMessage(MessagePostedEvent event) {
        try {
            // 이중 가드: 멤버가 2명 미만이면 스킵
            Post post = postService.getOrThrow(event.postId());
            if (post.getCurrentMembers() < 2) return;

            // 1) 최근 대화 컨텍스트(최신 20개 → 오름차순 정렬)
            List<ChatMessage> recentDesc = msgRepo.findTop50ByPostIdOrderByIdDesc(event.postId());

            // 컨텍스트 필터링:
            // - system 메시지 제외
            // - visibility_scope가 전원에게 보이는 메시지(ALL)만 포함
            List<ChatMessage> context = recentDesc.stream()
                    .filter(m -> m.getRole() == ChatMessage.Role.user)
                    .filter(m -> m.getVisibilityScope() == ChatMessage.VisibilityScope.ALL)
                    .sorted(Comparator.comparing(ChatMessage::getId))
                    .collect(Collectors.toList());

            // 최근 20개만 남기기
            int size = context.size();
            if (size > 20) {
                context = context.subList(size - 20, size);
            }

            // 2) 프롬프트 구성(한국어, 존댓말, 길이 제약 강화)
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(context);

            // 3) OpenAI 호출
            String replyText = chatClient.complete(systemPrompt, userPrompt, maxTokens);
            if (replyText == null || replyText.isBlank()) return;

            // 4) 응답 주체 선택(랜덤, sender 제외)
            Long responderId = pickResponderUserId(event.postId(), event.senderUserId());
            if (responderId == null) return;

            // 5) 메시지 저장
            ChatMessage reply = ChatMessage.builder()
                    .postId(event.postId())
                    .senderUserId(responderId)
                    .role(ChatMessage.Role.user)
                    .content(replyText)
                    .build();
            msgRepo.save(reply);

            // 6) 후처리: last_message_at 갱신
            postService.touchLastMessageAt(event.postId(), Instant.now());

        } catch (Exception e) {
            // 필요시 로그 추가
            // log.warn("AI reply failed postId={} msgId={} err={}", event.postId(), event.messageId(), e.toString());
        }
    }

    private String buildSystemPrompt() {
        // 한국어, 존댓말, 길이 제약 강화
        return """
                당신은 장보기(쇼핑) 모임 혹은 공동구매 소분 모임의 일원입니다. 한국어로 답하세요.
                - 반말은 금지하고, 공손한 존댓말을 사용합니다.
                - 간결하게 최대 2문장, 200자 이내로 답합니다.
                - 정보가 부족하면 추가 질문을 정확히 1개만 합니다.
                """;
    }

    private String buildUserPrompt(List<ChatMessage> contextAsc) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 최근 대화입니다. 아래 맥락을 고려하여 자연스러운 다음 발화를 생성하세요.\n");
        for (ChatMessage m : contextAsc) {
            // senderUserId 힌트 추가(짧게). 과도한 메타는 지양.
            Long uid = m.getSenderUserId();
            sb.append("user");
            if (uid != null) sb.append("#").append(uid);
            sb.append(": ").append(trim(safe(m.getContent()))).append("\n"); // 메시지 길이 컷오프(토큰 절약)
        }
        sb.append("\n제약: 최대 2문장(200자 이내), 존댓말, 필요 시 질문 1개.");
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String trim(String s) {
        if (s == null) return "";
        return s.length() <= 400 ? s : s.substring(0, 400);
    }

    private Long pickResponderUserId(Long postId, Long senderUserId) {
        var members = memberService.findMembers(postId);
        if (members == null || members.isEmpty()) return null;
        Collections.shuffle(members);
        return members.stream()
                .map(PostMember::getUserId)
                .filter(uid -> !uid.equals(senderUserId)) // sender 제외
                .findFirst()
                .orElse(null);
    }
}
