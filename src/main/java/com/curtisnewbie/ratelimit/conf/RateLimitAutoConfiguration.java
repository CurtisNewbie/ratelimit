package com.curtisnewbie.ratelimit.conf;

import com.curtisnewbie.ratelimit.api.RateLimitAspect;
import com.curtisnewbie.ratelimit.plugin.PluginLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * Rate Limit Auto Configuration
 *
 * @author yongj.zhuang
 */
@Configuration
public class RateLimitAutoConfiguration {

    @Bean
    public RateLimitAspect rateLimitAspect() {
        return new RateLimitAspect();
    }

    @Bean
    public PluginLoader pluginLoader() {
        return new PluginLoader();
    }

    @Bean
    public RedisScript<Boolean> simpleRateLimiterScript() {
        return RedisScript.of(new ClassPathResource("scripts/simpleRateLimiter.lua"), Boolean.class);
    }
}
