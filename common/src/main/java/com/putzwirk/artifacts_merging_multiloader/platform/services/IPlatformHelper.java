package com.putzwirk.artifacts_merging_multiloader.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    boolean isClient();

    Path getConfigDir();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
