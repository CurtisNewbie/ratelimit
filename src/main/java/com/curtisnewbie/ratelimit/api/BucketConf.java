package com.curtisnewbie.ratelimit.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for each RateLimiter
 *
 * @author yongj.zhuang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BucketConf {

    /**
     * Key for the rate limiter
     */
    private String key;

    /**
     * Max number of invocation within the window
     */
    private long rate;

    /**
     * Window size
     */
    private long interval;

    /**
     * Window size time unit
     */
    private TimeUnit intervalUnit;

    /**
     * Max wait time
     */
    private long waitTime;

    /**
     * Wait time time unit
     */
    private TimeUnit waitTimeUnit;
}
