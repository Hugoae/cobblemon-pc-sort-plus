package net.xhugo.cobblemonpcsortplus.platform;

import java.util.ServiceLoader;

/** Resolves the loader implementation packed in the same JAR. */
public final class Services {
    public static final CpsPlatform PLATFORM = load(CpsPlatform.class);

    private Services() {
    }

    public static <T> T load(Class<T> type) {
        return ServiceLoader.load(type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No implementation of " + type.getName()));
    }
}
