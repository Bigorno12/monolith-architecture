package mu.server.rest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CaffeineConfig {

    @Bean
    public Caffeine<@NonNull Object, @NonNull Object> caffeine() {
        return Caffeine.newBuilder()
                .maximumSize(10)
                .initialCapacity(50)
                .executor(ForkJoinPool.commonPool())
                .expireAfterWrite(300, TimeUnit.SECONDS)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine());
        caffeineCacheManager.setCacheNames(List.of("jsonPlaceHolder", "userCache", "todoCache", "adminCache"));

        return caffeineCacheManager;
    }

}

