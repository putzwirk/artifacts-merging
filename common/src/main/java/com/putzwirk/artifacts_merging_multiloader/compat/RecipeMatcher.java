package com.putzwirk.artifacts_merging_multiloader.compat;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.config.MergeEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class RecipeMatcher {
    private RecipeMatcher() {
    }

    public static List<String> inputIds(CraftingInput container) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                ids.add(ItemLookup.id(stack));
            }
        }
        return ids;
    }

    @Nullable
    public static String findGroupId(List<String> inputIds) {
        if (inputIds.isEmpty()) {
            return null;
        }
        for (MergeEntry entry : MergeConfigManager.groups()) {
            if (entry.items.isEmpty() || inputIds.size() != entry.count) {
                continue;
            }
            if (entry.items.containsAll(inputIds)) {
                return entry.id;
            }
        }
        return null;
    }
}
