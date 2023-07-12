package com.curtisnewbie.ratelimit.api;

import com.curtisnewbie.ratelimit.plugin.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static org.springframework.util.StringUtils.hasText;

/**
 * Aspect for {@link RateLimit} Annotation
 *
 * @author yongj.zhuang
 * @see RateLimit
 */
@Slf4j
@Aspect
public class RateLimitAspect {

    private static final String PRE = "rate:limit:";
    private static final ConcurrentMap<String, RateLimiter> rateLimiterRegistry = new ConcurrentHashMap<>();

    @Value("${spring.application.name}")
    private String appName;
    @Autowired
    private PluginLoader pluginLoader;

    @PostConstruct
    public void postConstruct() {
        log.info("RateLimitAspect initialized, will limit rates to annotated classes and methods");
    }

    @Around("@within(rateLimit) || @annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {

        // annotated on the class
        if (rateLimit == null) {
            rateLimit = pjp.getTarget().getClass().getDeclaredAnnotation(RateLimit.class);
        }

        // key for the rate-limiter
        final String key = PRE + (!hasText(rateLimit.key()) ? defaultKey(appName, pjp) : rateLimit.key());

        if (rateLimit.rate() < 1 || rateLimit.interval() < 1) return pjp.proceed();

        // RateLimiter
        final RateLimit finalRateLimit = rateLimit;
        RateLimiter rateLimiter = rateLimiterRegistry.computeIfAbsent(key, k -> {
            final BucketConf bk = BucketConf.builder()
                    .key(k)
                    .rate(finalRateLimit.rate())
                    .interval(finalRateLimit.interval())
                    .intervalUnit(finalRateLimit.intervalUnit())
                    .waitTime(finalRateLimit.waitTime())
                    .waitTimeUnit(finalRateLimit.waitTimeUnit())
                    .build();
            return buildRateLimiter(bk);
        });

        if (rateLimiter.acquire()) {
            return pjp.proceed();
        }

        log.warn("RateLimiter threshold exceeded or acquire timeout, threshold: {}/{} {}", rateLimit.rate(), rateLimit.interval(), rateLimit.intervalUnit());
        throw new IllegalStateException("Rate limit exceeded, please try again later");
    }

    private RateLimiter buildRateLimiter(BucketConf bk) {
        final RateLimiter rateLimiter = pluginLoader.newRateLimiter();
        bk.setKey(rateLimiter.keyPrefix() + bk.getKey());
        rateLimiter.configure(bk);
        return rateLimiter;
    }

    private static String defaultKey(String appName, ProceedingJoinPoint pjp) {
        return appName + ":" + pjp.getTarget().getClass().getName() + ":" + getMethod(pjp).getName();
    }

    private static Method getMethod(ProceedingJoinPoint pjp) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method m = sig.getMethod();

        // this could be the method of the interface, not the implementation class
        if (m.getDeclaringClass().isInterface()) {
            try {
                m = pjp.getTarget().getClass().getDeclaredMethod(pjp.getSignature().getName(),
                        m.getParameterTypes());
            } catch (SecurityException | NoSuchMethodException e) {
                throw new IllegalStateException(format("Failed to retrieve implementing method from %s", sig.toLongString()), e);
            }
        }
        return m;
    }
}


