package com.goormthon.careroad.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Redis 라이브러리가 클래스패스에 없는 경우 사용되는 기본 캐시 설정.
 * 스프링 부트 3.x에서는 @ConditionalOnClass의 inverse 옵션이 없으므로
 * '없을 때' 조건은 @ConditionalOnMissingClass 를 사용한다.
 */
@Configuration
@EnableCaching
@ConditionalOnMissingClass("org.springframework.data.redis.connection.RedisConnectionFactory")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager();
        mgr.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(100)
                        .maximumSize(1_000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
        );
        // 필요하면 캐시 이름별 TTL 분리 로직을 추가하세요 (B와 합의 필요)
        return mgr;
    }
}
