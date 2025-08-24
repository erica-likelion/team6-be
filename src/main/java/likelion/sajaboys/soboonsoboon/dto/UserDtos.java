package likelion.sajaboys.soboonsoboon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UserDtos {
    public record CreateRequest(
            @NotBlank @Size(max = 50) String username
    ) {
    }

    public record Response(
            Long id,
            String username,
            Instant createdAt
    ) {
    }
}
