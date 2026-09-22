package com.putzwirk.artifacts_merging_multiloader.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ArtifactMergingRecipeSerializer implements RecipeSerializer<ArtifactMergingRecipe> {
    @Override
    public ArtifactMergingRecipe fromJson(ResourceLocation id, JsonObject json) {
        return new ArtifactMergingRecipe(id, CraftingBookCategory.MISC);
    }

    @Override
    public ArtifactMergingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return new ArtifactMergingRecipe(id, CraftingBookCategory.MISC);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ArtifactMergingRecipe recipe) {
    }
}
