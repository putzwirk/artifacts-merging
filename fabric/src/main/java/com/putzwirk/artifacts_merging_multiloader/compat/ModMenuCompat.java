package com.putzwirk.artifacts_merging_multiloader.compat;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MergeConfigScreen::create;
    }
}
