package com.putzwirk.artifacts_merging_multiloader.mixin.client;

import com.putzwirk.artifacts_merging_multiloader.client.ClientIconCache;
import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ItemModelResolver.class)
public abstract class ItemPreviewMixin {
    @ModifyVariable(method = "updateForTopItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack artifactsmerging$cycleModel(ItemStack stack) {
        if (!ModItems.isRandomArtifact(stack)) {
            return stack;
        }
        if (ClientIconCache.hasIcon(RandomArtifactItem.groupId(stack))) {
            return stack;
        }
        List<Item> preview = ItemLookup.items(RandomArtifactItem.poolIds(stack));
        if (preview.isEmpty()) {
            return stack;
        }
        long step = System.currentTimeMillis() / 150L;
        return new ItemStack(preview.get((int) (step % preview.size())));
    }
}
