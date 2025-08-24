package likelion.sajaboys.soboonsoboon.dto;

import java.time.Instant;

public class PaymentDtos {
    public record RequestResponse(Long requestedBy, Instant requestedAt) {
    }

    public record DoneResponse(Long doneUserId, Integer doneCount, Integer totalMembers) {
    }
}
