package likelion.sajaboys.soboonsoboon.util;

public final class CurrentUser {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    public static void set(Long userId) {
        HOLDER.set(userId);
    }

    public static Long get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    private CurrentUser() {
    }
}
