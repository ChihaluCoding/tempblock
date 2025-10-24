package com.aprilcarlson.autobreakblock.platform;

import java.util.ServiceLoader;

import com.aprilcarlson.autobreakblock.AutoBreakBlockMod;
import com.aprilcarlson.autobreakblock.platform.services.IPlatformHelper;

public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    private static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        AutoBreakBlockMod.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
