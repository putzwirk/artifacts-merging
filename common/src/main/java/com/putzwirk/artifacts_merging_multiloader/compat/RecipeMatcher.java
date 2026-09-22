package com.putzwirk.artifacts_merging_multiloader.compat;

import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.config.MergeEntry;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class RecipeMatcher {
    private RecipeMatcher() {
    }

    public static List<String> inputIds(CraftingContainer container) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
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
