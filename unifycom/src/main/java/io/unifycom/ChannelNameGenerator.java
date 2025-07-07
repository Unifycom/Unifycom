package io.unifycom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelNameGenerator {

    private final Class<?> clazz;
    private final AtomicInteger counter = new AtomicInteger(0);

    public ChannelNameGenerator(Class<?> clazz) {

        this.clazz = clazz;
    }

    public String generate() {

        return clazz.getSimpleName() + "-" + counter.getAndIncrement();
    }

    private static final Map<Class<?>, ChannelNameGenerator> GENERATORS = new ConcurrentHashMap<>();

    public static String generate(Class<?> clazz) {

        return GENERATORS.compute(clazz, (k, v) -> v == null ? new ChannelNameGenerator(k) : v).generate();
    }
}
