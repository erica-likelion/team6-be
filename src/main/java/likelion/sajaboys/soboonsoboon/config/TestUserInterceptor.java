package likelion.sajaboys.soboonsoboon.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import likelion.sajaboys.soboonsoboon.util.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TestUserInterceptor implements HandlerInterceptor {

    private final Long testUserId;

    public TestUserInterceptor(@Value("${app.test-user-id:1}") Long testUserId) {
        this.testUserId = testUserId;
    }

    @Override
    public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) {
        CurrentUser.set(testUserId);
        return true;
    }

    @Override
    public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, @Nullable Exception ex) {
        CurrentUser.clear();
    }
}
