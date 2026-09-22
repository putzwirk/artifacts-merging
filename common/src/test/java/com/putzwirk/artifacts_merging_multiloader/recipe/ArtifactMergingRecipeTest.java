package com.putzwirk.artifacts_merging_multiloader.recipe;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactMergingRecipeTest {

    private static final String GROUP_ID = "test/group";
    private static boolean initialized;

    @TempDir
    Path tempDir;

    @BeforeAll
    static void initializeMinecraft() {
        if (initialized) {
            return;
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        unfreezeAndRegisterItem();
        initialized = true;
    }

    private static void unfreezeAndRegisterItem() {
        try {
            Field frozen = MappedRegistry.class.getDeclaredField("frozen");
            Field intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            frozen.setAccessible(true);
            intrusive.setAccessible(true);
            frozen.set(BuiltInRegistries.ITEM, false);
            intrusive.set(BuiltInRegistries.ITEM, new IdentityHashMap<>());
            Registry.register(BuiltInRegistries.ITEM, ModItems.RANDOM_ARTIFACT_ID, ModItems.create());
            intrusive.set(BuiltInRegistries.ITEM, null);
            frozen.set(BuiltInRegistries.ITEM, true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to register the test random artifact item", e);
        }
    }

    @Test
    void craftsRandomArtifactFromConfiguredGroup() {
        loadTestGroup();

        TransientCraftingContainer container = containerOf(
            new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD));

        ArtifactMergingRecipe recipe = new ArtifactMergingRecipe(
            new ResourceLocation(Constants.MOD_ID, "artifact_merging"), CraftingBookCategory.MISC);

        assertTrue(recipe.matches(container, null));

        ItemStack out = recipe.assemble(container, null);
        assertFalse(out.isEmpty());

        String result = RandomArtifactItem.resultId(out);
        assertNotNull(result);
        assertTrue(MergeConfigManager.byId(GROUP_ID).items.contains(result));
        assertNotEquals("minecraft:diamond", result);
        assertNotEquals("minecraft:emerald", result);
        assertTrue(ModItems.isRandomArtifact(out));
    }

    @Test
    void rejectsContainerWithMismatchedItem() {
        loadTestGroup();

        TransientCraftingContainer container = containerOf(
            new ItemStack(Items.DIAMOND), new ItemStack(Items.DIRT));

        ArtifactMergingRecipe recipe = new ArtifactMergingRecipe(
            new ResourceLocation(Constants.MOD_ID, "artifact_merging"), CraftingBookCategory.MISC);

        assertFalse(recipe.matches(container, null));
    }

    private static TransientCraftingContainer containerOf(ItemStack... stacks) {
        NonNullList<ItemStack> items = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
        for (int i = 0; i < stacks.length; i++) {
            items.set(i, stacks[i]);
        }
        return new TransientCraftingContainer(null, 1, stacks.length, items);
    }

    private void loadTestGroup() {
        Path root = tempDir.resolve(Constants.CONFIG_DIR_NAME);
        try {
            Files.createDirectories(root.resolve("test"));
            Files.writeString(root.resolve("test/group.json"),
                "{\"count\":2,\"items\":[\"minecraft:diamond\",\"minecraft:emerald\",\"minecraft:gold_ingot\"]}");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        MergeConfigManager.load(tempDir);
    }
}
