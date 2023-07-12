package com.curtisnewbie.ratelimit.plugin;

import com.curtisnewbie.ratelimit.api.BucketConf;
import com.curtisnewbie.ratelimit.api.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Simple RateLimiter based on Lua script
 *
 * @author yongj.zhuang
 */
@Slf4j
public class SimpleLuaRateLimiter implements RateLimiter {

    @Resource(name = "simpleRateLimiterScript")
    private RedisScript<Boolean> script;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private BucketConf conf;

    @Override
    public void configure(BucketConf conf) {
        this.conf = conf;
    }

    @Override
    public boolean acquire() {
        final long interval = conf.getIntervalUnit().toMillis(conf.getInterval());

        final CompletableFuture<Boolean> fut = CompletableFuture.supplyAsync(() ->
                redisTemplate.execute(script, Collections.singletonList(conf.getKey()), conf.getRate(), interval)
        );

        try {
            Boolean throttled = fut.get(conf.getWaitTime(), conf.getWaitTimeUnit());
            return throttled == null || !throttled;
        } catch (ExecutionException e) {
            log.error("RateLimiter thrown exception when trying to acquire token", e);
        } catch (InterruptedException | TimeoutException e) {
            log.warn("RateLimiter timeout when trying to acquire token", e);
        }
        return false;
    }

    @Override
    public String keyPrefix() {
        return "SIM:";
    }
}
