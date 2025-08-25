package likelion.sajaboys.soboonsoboon.config;

import likelion.sajaboys.soboonsoboon.domain.ChatMessage;
import likelion.sajaboys.soboonsoboon.domain.Meeting;
import likelion.sajaboys.soboonsoboon.domain.Post;
import likelion.sajaboys.soboonsoboon.domain.PostMember;
import likelion.sajaboys.soboonsoboon.domain.User;
import likelion.sajaboys.soboonsoboon.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class DummyDataSeedRunner {

    private final UserRepository userRepo;
    private final PostRepository postRepo;
    private final PostMemberRepository memberRepo;
    private final ChatMessageRepository msgRepo;
    private final MeetingRepository meetingRepo;

    public DummyDataSeedRunner(UserRepository userRepo,
                               PostRepository postRepo,
                               PostMemberRepository memberRepo,
                               ChatMessageRepository msgRepo,
                               MeetingRepository meetingRepo) {
        this.userRepo = userRepo;
        this.postRepo = postRepo;
        this.memberRepo = memberRepo;
        this.msgRepo = msgRepo;
        this.meetingRepo = meetingRepo;
    }

    // 제목-본문 쌍
    private record SamplePair(String title, String content) {
    }

    // 장보기 10개
    private static final List<SamplePair> shoppingPairs = List.of(
            new SamplePair(
                    "코스트코 같이 장보러 가실 분",
                    "코스트코에서 같이 고기 사러 가실 분 구해요.\n약 2시간 정도 예상돼요. 차량은 있어요(픽업 가능).\n결제는 각자 혹은 더치 정산으로 진행할게요. 시간/장소는 채팅으로 조율해요."
            ),
            new SamplePair(
                    "이마트 대형 장보기 동행 구해요",
                    "이마트에서 생필품 중심으로 같이 장보실 분 찾습니다.\n장보기 1~2시간 예상, 카트 같이 쓰면 더 편해요.\n정산은 계좌이체/현금 모두 가능해요."
            ),
            new SamplePair(
                    "트레이더스 공동 장보기 인원 모집",
                    "트레이더스에서 냉동식품/음료 위주로 담으려구요.\n차로 픽업 가능해서 부피 좀 있어도 괜찮아요.\n끝나고 근처 카페에서 잠깐 정산해도 좋아요."
            ),
            new SamplePair(
                    "홈플러스 특가 같이 담으실 분",
                    "홈플러스 주말 특가 노려보려 합니다.\n유제품/베이커리 위주, 1~2시간이면 충분할 듯해요.\n정산은 현장 간단히, 영수증 공유드릴게요."
            ),
            new SamplePair(
                    "롯데마트 주말 장보기 같이 하실래요",
                    "롯데마트에서 과일/정육 위주로 같이 볼 분 구해요.\n차량 없습니다. 버스로 이동 예정이에요.\n시간 맞춰 함께 돌고 정산은 더치로 가볍게 해요."
            ),
            new SamplePair(
                    "노브랜드 할인 같이 보실 분",
                    "노브랜드에서 과자/음료 위주로 담으려 합니다.\n1시간 내외로 빠르게 돌고, 정산은 각자 결제로 진행해요.\n시간은 유동적이라 채팅으로 조율해요."
            ),
            new SamplePair(
                    "하나로마트 산지직송 특가 장보러 가요",
                    "하나로마트에서 과일/야채 신선 코너 위주로 볼 예정이에요.\n차량 픽업 가능, 동선 맞으면 태워드릴게요.\n대략 1~2시간 예상합니다."
            ),
            new SamplePair(
                    "코스트코 대용량 생필품 함께 구매",
                    "휴지/세제/주방용품 등 대용량 생필품 위주로 구매하려고 해요.\n무거운 제품 위주라 카트 같이 쓰면 편합니다.\n정산은 계산서 기준으로 깔끔하게 나눠요."
            ),
            new SamplePair(
                    "이케아 주방용품 같이 보실래요",
                    "이케아에서 주방 가구 및 소품 몇 가지 살 예정입니다.\n구경 포함 2시간 정도 생각하고 있어요.\n대중교통 이동이고, 정산은 각자 결제로 정리해요."
            ),
            new SamplePair(
                    "새벽특가 마트 런 같이 하실 분",
                    "개점 시간 맞춰서 빠르게 장보실 분 찾습니다.\n음료/유제품 위주, 1시간 컷 목표!\n정산은 각자, 영수증 사진 공유드려요."
            )
    );

    // 소분용 10개
    private static final List<SamplePair> sharingPairs = List.of(
            new SamplePair(
                    "냉동 새우 소분하실 분",
                    "냉동 새우 2kg 주문했는데 너무 많아서 소분하실 분 4분 구합니다.\n장소 조율은 채팅으로 할게요.\n시간은 되도록이면 낮에 만나요."
            ),
            new SamplePair(
                    "김장용 배추 소분 인원 구해요",
                    "김장용 배추 10포기 묶음이라 1/2~1/4로 나눌 분 찾습니다.\n신선도 좋아서 당일 소분 희망합니다.\n정산은 계좌이체 가능합니다."
            ),
            new SamplePair(
                    "감자 10kg 나누실 분",
                    "감자 10kg 벌크 구매해서 3~4분과 나누려 합니다.\n상태 좋아요, 무거우니 근처에서 빠르게 나눠요.\n현금/이체 모두 가능합니다."
            ),
            new SamplePair(
                    "삼겹살 벌크 구매 소분 모집",
                    "삼겹살 2kg 대량으로 사서 3분과 소분하고 싶어요.\n아이스박스/보냉팩은 각자 준비 부탁드려요.\n당일 픽업 환영합니다."
            ),
            new SamplePair(
                    "대파 나누실 분",
                    "대파가 너무 많아서 필요한 만큼 나누실 분 구해요.\n조금씩 가져가셔도 됩니다.\n시간/장소는 채팅으로 조율해요."
            ),
            new SamplePair(
                    "시리얼 세트 나누실 분",
                    "시리얼 4박스 세트 구매했는데 과해서 2박스 나누실 분 구해요.\n미개봉 새 제품이며 가격은 1/n로 정리해요."
            ),
            new SamplePair(
                    "양파 5kg 소분 모집",
                    "양파 5kg가 많아서 2~3분과 나누려 합니다.\n보관은 서늘한 곳 권장, 소분은 지퍼백으로 간단히 진행해요."
            ),
            new SamplePair(
                    "쌀 10kg 반씩 나눌 분",
                    "쌀 10kg 구매했는데 절반만 필요해요.\n미개봉 상태, 근처에서 빠르게 교환해요."
            ),
            new SamplePair(
                    "베이글 12개입 소분",
                    "베이글 12개 묶음이라 6개/6개로 나눌 분 찾아요.\n냉동 보관 추천, 픽업 시간은 채팅으로 맞춰요."
            ),
            new SamplePair(
                    "치즈 대용량 슬라이스 나눔",
                    "슬라이스 치즈 대용량 구매분 함께 나누실 분 구해요.\n유통기한 넉넉, 아이스팩은 각자 준비 부탁드려요."
            )
    );

    // 상세 지역명
    private static final String[] places = {
            "서울시 성동구 성수동",
            "서울시 강남구 대치동",
            "서울시 송파구 잠실동",
            "서울시 마포구 합정동",
            "서울시 용산구 이태원동",
            "서울시 동작구 사당동",
            "경기도 고양시 일산동구 백석동",
            "경기도 수원시 팔달구 인계동",
            "경기도 안산시 상록구 사동",
            "경기도 성남시 분당구 정자동",
            "경기도 용인시 수지구 상현동",
            "경기도 김포시 장기동",
            "경기도 파주시 금촌동",
            "인천시 남동구 구월동",
            "인천시 부평구 산곡동",
            "부산시 해운대구 좌동",
            "부산시 수영구 광안동",
            "대구시 수성구 범어동",
            "대전시 서구 둔산동",
            "광주시 서구 화정동",
            "울산시 남구 삼산동",
            "충북 청주시 상당구 금천동",
            "충남 천안시 서북구 불당동",
            "전북 전주시 완산구 효자동",
            "전남 순천시 조례동",
            "강원도 춘천시 석사동",
            "제주특별자치도 제주시 노형동"
    };

    // 채팅 10개
    private static final String[] chatLines = {
            "안녕하세요, 참여 가능할까요?",
            "시간대는 몇 시가 괜찮으세요?",
            "만나는 위치를 대략 어디로 볼까요?",
            "정산은 현금이 편하실까요, 계좌이체가 나을까요?",
            "차량 있으신가요? 없으면 대중교통으로 이동해도 됩니다.",
            "혹시 필요한 추가 품목 있으신가요?",
            "늦게 합류해도 될까요?",
            "인원 다 차면 알려주세요!",
            "급한 일 생기면 바로 공유드릴게요.",
            "연락처는 남겨주시면 감사하겠습니다 교환해요."
    };

    @Transactional
    public void run() {
        // 이미 데이터 있으면 스킵
        if (postRepo.count() > 0L) return;

        Random rnd = new Random(42);

        // 1) Users: 50명 (username UNIQUE)
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            users.add(userRepo.save(User.builder()
                    .username("user%03d".formatted(i))
                    .build()));
        }

        // 2) Posts: 총 50개 (open 25 + closed 25)
        List<Post> posts = new ArrayList<>();

        // closed 중 user000 강제 배정 카운터
        int forcedClosedForUser000 = 0;
        int forcedClosedTarget = 10;

        for (int i = 0; i < 50; i++) {
            boolean isOpen = i < 25; // open 25, closed 25
            Post.Type type = (i % 2 == 0) ? Post.Type.shopping : Post.Type.sharing;
            int cap = 2 + rnd.nextInt(7); // 2~8

            // creator 선택 로직
            User creator;
            if (!isOpen && forcedClosedForUser000 < forcedClosedTarget) {
                // closed 포스트 10개까지는 user000(=users.get(0))으로 고정
                creator = users.get(0);
                forcedClosedForUser000++;
            } else {
                // 나머지는 랜덤
                creator = users.get(rnd.nextInt(users.size()));
            }

            Instant createdAt = Instant.now().minus(Duration.ofHours(1 + rnd.nextInt(240)));
            Instant timeStart;
            Instant timeEnd;
            BigDecimal price;

            if (type == Post.Type.shopping) {
                timeStart = Instant.now().plus(Duration.ofHours(1 + rnd.nextInt(72)));
                timeEnd = timeStart.plus(Duration.ofHours(1 + rnd.nextInt(6)));
                price = null;
            } else {
                price = BigDecimal.valueOf(5_000 + rnd.nextInt(25_000));
                timeStart = null;
                timeEnd = null;
            }

            // 제목-본문 한 쌍 선택
            SamplePair pair = samplePostPair(type, rnd);

            Post p = Post.builder()
                    .type(type)
                    .title(pair.title)
                    .content(pair.content)
                    .imagesJson(null)
                    .place(samplePlace(rnd))
                    .capacity(cap)
                    .timeStart(timeStart)
                    .timeEnd(timeEnd)
                    .price(price)
                    .creatorId(creator.getId()) // NOT NULL, FK
                    .createdAt(createdAt)
                    .status(isOpen ? Post.Status.open : Post.Status.closed)
                    .lastMessageAt(null)
                    .paymentRequesterId(null)
                    .paymentDoneUserIdsJson(null)
                    .build();

            posts.add(postRepo.save(p));
        }

        // 3) PostMember: host + (0 ~ capacity-2)
        for (Post p : posts) {
            // host(creator)
            memberRepo.save(PostMember.builder()
                    .postId(p.getId())
                    .userId(p.getCreatorId())
                    .role(PostMember.Role.host)
                    .build());

            // 테스트 사용자가 1명 더 참여해도 초과하지 않도록 capacity-2까지만 허용
            int maxAdd = Math.max(0, p.getCapacity() - 2);
            int add = (maxAdd == 0) ? 0 : rnd.nextInt(maxAdd + 1); // 0 ~ (capacity-2)

            List<User> shuffled = new ArrayList<>(users);
            Collections.shuffle(shuffled, rnd);
            Set<Long> used = new HashSet<>();
            used.add(p.getCreatorId());

            int idx = 0, added = 0;
            while (added < add && idx < shuffled.size()) {
                Long uid = shuffled.get(idx++).getId();
                if (used.add(uid)) {
                    memberRepo.save(PostMember.builder()
                            .postId(p.getId())
                            .userId(uid)
                            .role(PostMember.Role.member)
                            .build());
                    added++;
                }
            }
        }

        // 4) ChatMessage: 각 post 0~5개, 시스템 메시지 생성하지 않음
        for (Post p : posts) {
            int cnt = rnd.nextInt(6); // 0~5
            if (cnt == 0) continue;

            List<PostMember> members = memberRepo.findAllByPostId(p.getId());
            List<Long> memberIds = members.stream().map(PostMember::getUserId).toList();

            Instant latest = p.getLastMessageAt();

            for (int k = 0; k < cnt; k++) {
                Long senderUserId = memberIds.isEmpty() ? null : memberIds.get(rnd.nextInt(memberIds.size()));
                String content = sampleChatLine(rnd);

                Instant createdAt = p.getCreatedAt()
                        .plus(Duration.ofMinutes(rnd.nextInt(60 * 24 * 10)));
                if (createdAt.isAfter(Instant.now())) createdAt = Instant.now();

                ChatMessage m = ChatMessage.builder()
                        .postId(p.getId())
                        .senderUserId(senderUserId)
                        .role(ChatMessage.Role.user) // 사용자 메시지만
                        .content(content)
                        .systemType(null)
                        .systemPayloadJson(null)
                        .visibilityScope(ChatMessage.VisibilityScope.ALL)
                        .visibilityUserId(null)
                        .createdAt(createdAt)
                        .build();

                msgRepo.save(m);
                if (latest == null || createdAt.isAfter(latest)) latest = createdAt;
            }

            if (latest != null) {
                p.setLastMessageAt(latest);
                postRepo.save(p);
            }
        }

        // 5) Meeting: open에는 생성하지 않고, closed의 100%에 생성
        List<Post> closedPosts = posts.stream()
                .filter(pp -> pp.getStatus() == Post.Status.closed)
                .toList();

        for (Post p : closedPosts) {
            if (!meetingRepo.existsByPostId(p.getId())) {
                // closed 특성상 과거 또는 최근 시점으로 설정
                Instant time = p.getCreatedAt().plus(Duration.ofHours(1 + rnd.nextInt(72)));
                if (time.isAfter(Instant.now())) {
                    time = Instant.now().minus(Duration.ofHours(1 + rnd.nextInt(24)));
                }

                meetingRepo.save(Meeting.builder()
                        .postId(p.getId())
                        .time(time)
                        .build());
            }
        }

        for (Post p : posts) {
            int current = (int) memberRepo.countByPostId(p.getId());
            p.setCurrentMembers(current);
            postRepo.save(p);
        }

    }

    // ==================== 샘플 유틸 ====================

    private SamplePair samplePostPair(Post.Type type, Random rnd) {
        return (type == Post.Type.shopping)
                ? shoppingPairs.get(rnd.nextInt(shoppingPairs.size()))
                : sharingPairs.get(rnd.nextInt(sharingPairs.size()));
    }

    private String samplePlace(Random rnd) {
        return places[rnd.nextInt(places.length)];
    }

    private String sampleChatLine(Random rnd) {
        return chatLines[rnd.nextInt(chatLines.length)];
    }
}
