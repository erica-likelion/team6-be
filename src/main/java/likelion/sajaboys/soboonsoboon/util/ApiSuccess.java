package likelion.sajaboys.soboonsoboon.util;

public record ApiSuccess<T>(boolean success, T data) {
    public static <T> ApiSuccess<T> of(T data) {
        return new ApiSuccess<>(true, data);
    }
}
