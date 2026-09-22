package com.putzwirk.artifacts_merging_multiloader.registry;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ModItems {
    public static final String RANDOM_ARTIFACT_NAME = "random_artifact";
    public static final Identifier RANDOM_ARTIFACT_ID = Ids.of(Constants.MOD_ID, RANDOM_ARTIFACT_NAME);

    public static Item create() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RANDOM_ARTIFACT_ID);
        return new RandomArtifactItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).setId(key));
    }

    public static Item get() {
        Item item = ItemLookup.item(RANDOM_ARTIFACT_ID);
        if (item == null) {
            throw new IllegalStateException(Constants.MOD_ID + ":" + RANDOM_ARTIFACT_NAME + " is not registered");
        }
        return item;
    }

    public static boolean isRandomArtifact(ItemStack stack) {
        return !stack.isEmpty() && stack.is(get());
    }
}
