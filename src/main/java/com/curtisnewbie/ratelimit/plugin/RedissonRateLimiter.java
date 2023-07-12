package com.curtisnewbie.ratelimit.plugin;

import com.curtisnewbie.ratelimit.api.BucketConf;
import com.curtisnewbie.ratelimit.api.RateLimiter;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Redisson based RateLimiter
 *
 * @author yongj.zhuang
 */
public class RedissonRateLimiter implements RateLimiter {

    @Autowired
    private RedissonClient redissonClient;

    private BucketConf conf;

    @Override
    public void configure(BucketConf conf) {
        this.conf = conf;

        RRateLimiter rr = redissonClient.getRateLimiter(this.conf.getKey());
        final long rateInterval = this.conf.getIntervalUnit().toMillis(this.conf.getInterval());

        // one-time configuration for the rate-limiter
        rr.trySetRate(RateType.OVERALL, this.conf.getRate(), rateInterval, RateIntervalUnit.MILLISECONDS);
    }

    @Override
    public boolean acquire() {
        RRateLimiter rr = redissonClient.getRateLimiter(this.conf.getKey());
        final long timeout = this.conf.getWaitTimeUnit().toMillis(this.conf.getWaitTime());
        return rr.tryAcquire(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public String keyPrefix() {
        return "RED:";
    }
}
