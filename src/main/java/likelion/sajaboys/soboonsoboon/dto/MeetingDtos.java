package likelion.sajaboys.soboonsoboon.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class MeetingDtos {
    public record CreateRequest(@NotNull Instant time) {
    }

    public record Response(Long postId, Instant time) {
    }
}
