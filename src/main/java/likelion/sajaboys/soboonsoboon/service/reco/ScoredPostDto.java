package likelion.sajaboys.soboonsoboon.service.reco;

import likelion.sajaboys.soboonsoboon.domain.post.Post;

public record ScoredPostDto(Post post, int score) {
}
