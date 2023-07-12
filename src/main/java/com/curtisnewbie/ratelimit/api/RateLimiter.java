package com.curtisnewbie.ratelimit.api;

/**
 * RateLimiter
 *
 * @author yongj.zhuang
 */
public interface RateLimiter {

    /**
     * Configure RateLimiter
     */
    void configure(BucketConf conf);

    /**
     * Acquire token from the bucket, return True if success else False
     */
    boolean acquire();

    /**
     * Ke prefix for each RateLimiter
     */
    default String keyPrefix() {
        return "";
    }
}
