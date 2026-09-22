package com.putzwirk.artifacts_merging_multiloader.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public final class RecipeLookup {
    private RecipeLookup() {
    }

    @Nullable
    public static RecipeSerializer<?> serializer(ResourceLocation id) {
        return BuiltInRegistries.RECIPE_SERIALIZER.get(id);
    }
}
