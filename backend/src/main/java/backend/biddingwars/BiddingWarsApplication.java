package backend.biddingwars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Bidding Wars backend.
 * 
 * @EnableJpaAuditing enables automatic population of @CreatedDate and @LastModifiedDate fields
 */
@SpringBootApplication
@EnableJpaAuditing
public class BiddingWarsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiddingWarsApplication.class, args);
    }
}