package com.curtisnewbie.ratelimit.plugin;

import com.curtisnewbie.ratelimit.api.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * RateLimiter implementation loader
 *
 * @author yongj.zhuang
 */
@Slf4j
@Component
public class PluginLoader {

    private static final Class<? extends RateLimiter> pluginClazz;
    private static final Constructor<? extends RateLimiter> pluginConstructor;

    static {
        // use SPI to load the class of the RateLimiter implementation, notice that ServiceLoader isn't thread-safe
        // we will instantiate the object ourself
        final ServiceLoader<RateLimiter> loader = ServiceLoader.load(RateLimiter.class);
        final Iterator<RateLimiter> it = loader.iterator();
        if (!it.hasNext())
            throw new IllegalStateException("Failed to load RateLimiter from META-INF/services");

        pluginClazz = it.next().getClass();
        try {
            pluginConstructor = pluginClazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(String.format("Failed to load RateLimiter, missing default constructor, clazz: %s", pluginClazz.getName()), e);
        }

        // test the default constructor
        _instantiateRateLimiter();

        log.info("RateLimiter loaded, implementation: {}", pluginClazz.getName());
    }

    @Autowired
    private ApplicationContext ctx;

    public RateLimiter newRateLimiter() {
        final RateLimiter rateLimiter = _instantiateRateLimiter();
        ctx.getAutowireCapableBeanFactory().autowireBean(rateLimiter);
        return rateLimiter;
    }

    private static RateLimiter _instantiateRateLimiter() {
        try {
            return pluginConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(String.format("Failed to instantiate RateLimiter, clazz: %s", pluginClazz.getName()), e);
        }
    }

}
