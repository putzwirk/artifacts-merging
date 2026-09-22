package com.putzwirk.artifacts_merging_multiloader.registry;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.compat.Ids;
import com.putzwirk.artifacts_merging_multiloader.compat.RecipeLookup;
import com.putzwirk.artifacts_merging_multiloader.recipe.ArtifactMergingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {
    public static final String ARTIFACT_MERGING_NAME = "artifact_merging";
    public static final ResourceLocation ARTIFACT_MERGING_ID = Ids.of(Constants.MOD_ID, ARTIFACT_MERGING_NAME);

    public static RecipeSerializer<?> create() {
        return new CustomRecipe.Serializer<>(ArtifactMergingRecipe::new);
    }

    @SuppressWarnings("unchecked")
    public static RecipeSerializer<ArtifactMergingRecipe> get() {
        RecipeSerializer<?> serializer = RecipeLookup.serializer(ARTIFACT_MERGING_ID);
        if (serializer == null) {
            throw new IllegalStateException(Constants.MOD_ID + ":" + ARTIFACT_MERGING_NAME + " is not registered");
        }
        return (RecipeSerializer<ArtifactMergingRecipe>) serializer;
    }
}
