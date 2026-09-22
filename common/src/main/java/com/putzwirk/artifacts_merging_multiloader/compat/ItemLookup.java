package com.putzwirk.artifacts_merging_multiloader.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class ItemLookup {
    private ItemLookup() {
    }

    public static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    @Nullable
    public static Item item(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item == Items.AIR ? null : item;
    }

    @Nullable
    public static Item item(String id) {
        ResourceLocation location = Ids.parse(id);
        return location == null ? null : item(location);
    }

    public static boolean exists(String id) {
        return item(id) != null;
    }

    public static List<Item> items(List<String> ids) {
        List<Item> out = new ArrayList<>();
        for (String id : ids) {
            Item item = item(id);
            if (item != null) {
                out.add(item);
            }
        }
        return out;
    }
}
