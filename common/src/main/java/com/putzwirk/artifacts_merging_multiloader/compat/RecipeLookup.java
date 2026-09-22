package com.putzwirk.artifacts_merging_multiloader.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jspecify.annotations.Nullable;

public final class RecipeLookup {
    private RecipeLookup() {
    }

    @Nullable
    public static RecipeSerializer<?> serializer(Identifier id) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getValue(id);
    }
}
