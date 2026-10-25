package com.putzwirk.artifacts_merging_multiloader.recipe;

import com.putzwirk.artifacts_merging_multiloader.Constants;
import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.compat.MergeData;
import com.putzwirk.artifacts_merging_multiloader.compat.StackData;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactMergingRecipeTest {

    private static final String GROUP_ID = "test/group";
    private static final List<String> GROUP_ITEMS =
        List.of("minecraft:diamond", "minecraft:emerald", "minecraft:gold_ingot");
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
    void craftsSealedBoxFromConfiguredGroup() {
        loadTestGroup();

        TransientCraftingContainer container = containerOf(
            new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD));

        ArtifactMergingRecipe recipe = new ArtifactMergingRecipe(
            new ResourceLocation("artifactsmerging", "artifact_merging"), CraftingBookCategory.MISC);

        assertTrue(recipe.matches(container, null));

        ItemStack out = recipe.assemble(container, null);
        assertFalse(out.isEmpty());
        assertTrue(ModItems.isRandomArtifact(out));
        assertNull(RandomArtifactItem.resultId(out));
        assertEquals(GROUP_ID, RandomArtifactItem.groupId(out));
    }

    @Test
    void openingBoxRollsFromGroupPoolWithoutInputs() {
        loadTestGroup();

        ItemStack box = RandomArtifactItem.create(GROUP_ID,
            List.of("minecraft:diamond", "minecraft:emerald"));

        ItemStack opened = RandomArtifactItem.open(box);
        assertNotNull(opened);
        assertEquals(Items.GOLD_INGOT, opened.getItem());
    }

    @Test
    void openingLegacyBoxUsesStoredResult() {
        loadTestGroup();

        ItemStack box = RandomArtifactItem.create(GROUP_ID,
            List.of("minecraft:diamond", "minecraft:emerald"));
        StackData.write(box, new MergeData(GROUP_ID,
            List.of("minecraft:diamond", "minecraft:emerald"), GROUP_ITEMS, "minecraft:emerald"));

        ItemStack opened = RandomArtifactItem.open(box);
        assertNotNull(opened);
        assertEquals(Items.EMERALD, opened.getItem());
    }

    @Test
    void openingBoxWithoutDataFallsBackToFirstGroup() {
        loadTestGroup();

        ItemStack opened = RandomArtifactItem.open(RandomArtifactItem.generic());
        assertNotNull(opened);
        assertTrue(GROUP_ITEMS.contains(ItemLookup.id(opened)));
    }

    @Test
    void openingNonBoxReturnsNull() {
        loadTestGroup();
        assertNull(RandomArtifactItem.open(new ItemStack(Items.DIRT)));
    }

    @Test
    void recipeExposesGenericBoxAsResultItem() {
        loadTestGroup();

        ArtifactMergingRecipe recipe = new ArtifactMergingRecipe(
            new ResourceLocation("artifactsmerging", "artifact_merging"), CraftingBookCategory.MISC);

        assertTrue(ModItems.isRandomArtifact(recipe.getResultItem(null)));
    }

    @Test
    void rejectsContainerWithMismatchedItem() {
        loadTestGroup();

        TransientCraftingContainer container = containerOf(
            new ItemStack(Items.DIAMOND), new ItemStack(Items.DIRT));

        ArtifactMergingRecipe recipe = new ArtifactMergingRecipe(
            new ResourceLocation("artifactsmerging", "artifact_merging"), CraftingBookCategory.MISC);

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
