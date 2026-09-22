package com.putzwirk.artifacts_merging_multiloader;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.platform.Services;

public class ArtifactsMergingCommon {

    public static void init() {
        MergeConfigManager.load(Services.PLATFORM.getConfigDir());
        Constants.LOG.info("Artifacts Merging loaded {} merge group(s) on {}",
            MergeConfigManager.groups().size(), Services.PLATFORM.getPlatformName());
    }
}
