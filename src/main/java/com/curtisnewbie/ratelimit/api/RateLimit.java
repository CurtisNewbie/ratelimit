package com.curtisnewbie.ratelimit.api;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Annotation use to define rate limit for annotated methods or the method under an annotated class
 *
 * @author yongj.zhuang
 * @see RateLimitAspect
 */
@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Key for the rate limiter
     * <p>
     * By default it's {@code ${spring.application.name}} + ":" + class_name + ":" + method_name.
     */
    String key() default "";

    /**
     * Max number of invocation within the window, by default it's 500/s; if it's less than 1, rate-limiter is disabled.
     */
    long rate() default 500;

    /**
     * Window size, by default it's 1000ms; if it's less than 1, rate-limiter is disabled.
     */
    long interval() default 1000;

    /**
     * Window size time unit, by default it's milliseconds.
     */
    TimeUnit intervalUnit() default TimeUnit.MILLISECONDS;

    /**
     * Max wait time, by nature, rate-limiter requires invocations to be queued,
     * this defines the max wait time that we will wait before we get the chance to call the method.
     * <p>
     * By default it's 1000 ms
     */
    long waitTime() default 1000;

    /**
     * Wait time time unit, by default it's milliseconds.
     */
    TimeUnit waitTimeUnit() default TimeUnit.MILLISECONDS;

}

