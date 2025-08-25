package likelion.sajaboys.soboonsoboon.service.ai.recommend;

import likelion.sajaboys.soboonsoboon.domain.Post;

public record ScoredPostDto(Post post, int score) {
}
