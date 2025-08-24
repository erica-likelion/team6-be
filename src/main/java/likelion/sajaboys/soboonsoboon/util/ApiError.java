package likelion.sajaboys.soboonsoboon.util;

public record ApiError(boolean success, ErrorBody error) {
    public static ApiError of(ErrorCode code, String message) {
        return new ApiError(false, new ErrorBody(code.name(), message, null));
    }

    public static ApiError of(ErrorCode code, String message, Object details) {
        return new ApiError(false, new ErrorBody(code.name(), message, details));
    }

    public record ErrorBody(String code, String message, Object details) {
    }
}
