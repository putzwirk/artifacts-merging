package com.putzwirk.artifacts_merging_multiloader.recipe;

import com.putzwirk.artifacts_merging_multiloader.compat.RecipeMatcher;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class ArtifactMergingRecipe extends CustomRecipe {
    public ArtifactMergingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return RecipeMatcher.findGroupId(RecipeMatcher.inputIds(container)) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registries) {
        List<String> inputIds = RecipeMatcher.inputIds(container);
        String groupId = RecipeMatcher.findGroupId(inputIds);
        if (groupId == null) {
            return ItemStack.EMPTY;
        }
        return RandomArtifactItem.create(groupId, inputIds);
    }

    @Override
    public RecipeSerializer<ArtifactMergingRecipe> getSerializer() {
        return ModRecipes.get();
    }
}
