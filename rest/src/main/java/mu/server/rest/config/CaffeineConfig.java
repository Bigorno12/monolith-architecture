package mu.server.rest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CaffeineConfig {

    @Bean
    public Caffeine<@NonNull Object, @NonNull Object> caffeine() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .initialCapacity(10)
                .executor(ForkJoinPool.commonPool())
                .expireAfterWrite(300, TimeUnit.SECONDS)
                .evictionListener((key, _, cause) -> log.info("Key {} was evicted {}", key, cause))
                .removalListener((key, _, cause) -> log.info("Key {} was removed {}", key, cause))
                .scheduler(Scheduler.systemScheduler())
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine());
        caffeineCacheManager.setCacheNames(List.of("jsonPlaceHolder", "userCache", "todoCache", "adminCache", "keycloakCache", "profileCache"));

        caffeineCacheManager.registerCustomCache("fingerprintCache",
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .executor(ForkJoinPool.commonPool())
                        .scheduler(Scheduler.systemScheduler())
                        .evictionListener((key, _, cause) -> log.info("Fingerprint evicted for token {} reason: {}", key, cause))
                        .build());

        return caffeineCacheManager;
    }

}
