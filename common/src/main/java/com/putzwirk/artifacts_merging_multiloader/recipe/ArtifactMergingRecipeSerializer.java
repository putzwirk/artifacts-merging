package com.putzwirk.artifacts_merging_multiloader.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ArtifactMergingRecipeSerializer implements RecipeSerializer<ArtifactMergingRecipe> {
    private static final Codec<ArtifactMergingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
            .forGetter(ArtifactMergingRecipe::category)
    ).apply(instance, ArtifactMergingRecipe::new));

    @Override
    public Codec<ArtifactMergingRecipe> codec() {
        return CODEC;
    }

    @Override
    public ArtifactMergingRecipe fromNetwork(FriendlyByteBuf buffer) {
        return new ArtifactMergingRecipe(CraftingBookCategory.MISC);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ArtifactMergingRecipe recipe) {
    }
}
