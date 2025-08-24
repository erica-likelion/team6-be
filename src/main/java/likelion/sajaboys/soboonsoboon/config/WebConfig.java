package likelion.sajaboys.soboonsoboon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TestUserInterceptor testUserInterceptor;

    public WebConfig(TestUserInterceptor testUserInterceptor) {
        this.testUserInterceptor = testUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(testUserInterceptor).addPathPatterns("/**");
    }
}
