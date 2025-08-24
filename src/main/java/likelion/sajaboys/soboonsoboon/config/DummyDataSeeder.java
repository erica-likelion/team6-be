package likelion.sajaboys.soboonsoboon.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DummyDataSeeder {

    @Bean
    public CommandLineRunner seedData(DummyDataSeedRunner runner) {
        return args -> runner.run();
    }
}
