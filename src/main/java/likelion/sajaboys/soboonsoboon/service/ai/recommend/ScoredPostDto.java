package likelion.sajaboys.soboonsoboon.service.ai.recommend;

import likelion.sajaboys.soboonsoboon.domain.post.Post;

public record ScoredPostDto(Post post, int score) {
}
