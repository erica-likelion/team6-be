package likelion.sajaboys.soboonsoboon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PostDtos {

    public record CreateRequest(
            @NotBlank String type,                  // "shopping" | "sharing"
            @NotBlank @Size(min = 1, max = 200) String title,
            String content,
            List<@NotBlank String> images,         // optional
            @NotBlank @Size(min = 1, max = 255) String place,
            @NotNull @Min(1) Integer capacity,
            Instant timeStart, // shopping only
            Instant timeEnd,   // shopping only
            BigDecimal price   // sharing only
    ) {
    }

    public record CreateResponse(
            Long id,
            String type,
            String status,
            Instant createdAt
    ) {
    }

    public record Item(
            Long id,
            String type,
            String status,
            String title,
            String place,
            Integer capacity,
            Integer currentMembers,
            BigDecimal price,
            Instant timeStart,
            Instant timeEnd,
            Instant createdAt
    ) {
    }

    public record PageResponse(
            java.util.List<Item> items,
            PageMeta page
    ) {
    }

    public record PageMeta(int page, int size, long total) {
    }

    public record Detail(
            Long id,
            String type,
            String status,
            String title,
            String content,
            java.util.List<String> images,
            String place,
            Integer capacity,
            Integer currentMembers,
            Instant timeStart,
            Instant timeEnd,
            BigDecimal price,
            Long creatorId,
            Instant createdAt,
            Instant lastMessageAt
    ) {
    }
}
