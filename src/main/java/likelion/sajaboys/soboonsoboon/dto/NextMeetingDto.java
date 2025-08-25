package likelion.sajaboys.soboonsoboon.dto;

import likelion.sajaboys.soboonsoboon.domain.Post;

import java.time.Instant;

public record NextMeetingDto(Long postId, Post.Type type, String place, Instant time) {
}
