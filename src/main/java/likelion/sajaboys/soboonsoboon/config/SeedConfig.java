package likelion.sajaboys.soboonsoboon.config;

import likelion.sajaboys.soboonsoboon.domain.user.User;
import likelion.sajaboys.soboonsoboon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedConfig {
    @Bean
    CommandLineRunner seed(UserRepository userRepository, @Value("${app.test-user-username}") String username,
                           @Value("${app.test-user-id}") Long id) {
        return args -> {
            if (userRepository.findByUsername(username).isEmpty()) {
                userRepository.save(User.builder().username(username).build());
            }
        };
    }
}
