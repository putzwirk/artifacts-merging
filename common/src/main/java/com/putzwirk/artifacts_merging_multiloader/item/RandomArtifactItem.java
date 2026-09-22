package com.putzwirk.artifacts_merging_multiloader.item;

import com.putzwirk.artifacts_merging_multiloader.client.ClientNames;
import com.putzwirk.artifacts_merging_multiloader.compat.MergeData;
import com.putzwirk.artifacts_merging_multiloader.compat.MergeRoll;
import com.putzwirk.artifacts_merging_multiloader.compat.StackData;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.config.MergeEntry;
import com.putzwirk.artifacts_merging_multiloader.platform.Services;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RandomArtifactItem extends Item {

    public RandomArtifactItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(String groupId, List<String> inputIds) {
        MergeEntry entry = MergeConfigManager.byId(groupId);
        List<String> pool = entry == null ? List.of() : entry.items;
        String result = MergeRoll.pick(pool, inputIds);
        ItemStack stack = new ItemStack(ModItems.get());
        StackData.write(stack, new MergeData(groupId, inputIds, pool, result));
        return stack;
    }

    public static String groupId(ItemStack stack) {
        MergeData data = StackData.read(stack);
        if (data != null && !data.groupId().isEmpty()) {
            return data.groupId();
        }
        List<MergeEntry> groups = MergeConfigManager.groups();
        return groups.isEmpty() ? "" : groups.get(0).id;
    }

    public static List<String> poolIds(ItemStack stack) {
        MergeData data = StackData.read(stack);
        if (data != null && !data.pool().isEmpty()) {
            return new ArrayList<>(data.pool());
        }
        MergeEntry entry = MergeConfigManager.byId(groupId(stack));
        return entry == null ? new ArrayList<>() : new ArrayList<>(entry.items);
    }

    @Nullable
    public static String resultId(ItemStack stack) {
        MergeData data = StackData.read(stack);
        return data == null ? null : data.result();
    }

    @Override
    public Component getName(ItemStack stack) {
        if (Services.PLATFORM.isClient()) {
            String name = ClientNames.displayName(groupId(stack));
            if (name != null && !name.isBlank()) {
                return Component.literal(name);
            }
        }
        return super.getName(stack);
    }
}
