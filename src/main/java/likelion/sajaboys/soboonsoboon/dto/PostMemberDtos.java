package likelion.sajaboys.soboonsoboon.dto;

public class PostMemberDtos {
    public record JoinResponse(Long postId, Long userId, String role) {
    }
}
