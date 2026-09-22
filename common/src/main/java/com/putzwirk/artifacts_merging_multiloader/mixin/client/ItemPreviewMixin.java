package com.putzwirk.artifacts_merging_multiloader.mixin.client;

import com.putzwirk.artifacts_merging_multiloader.client.ClientIconCache;
import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ItemModelResolver.class)
public abstract class ItemPreviewMixin {
    @Inject(method = "appendItemLayers", at = @At("HEAD"), cancellable = true)
    private void artifactsmerging$cycleModel(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, @Nullable Level level, @Nullable ItemOwner owner, int seed, CallbackInfo ci) {
        if (!ModItems.isRandomArtifact(stack)) {
            return;
        }
        if (ClientIconCache.hasIcon(RandomArtifactItem.groupId(stack))) {
            return;
        }
        List<Item> preview = ItemLookup.items(RandomArtifactItem.poolIds(stack));
        if (preview.isEmpty()) {
            return;
        }
        long step = System.currentTimeMillis() / 150L;
        ItemStack substitute = new ItemStack(preview.get((int) (step % preview.size())));
        ((ItemModelResolver) (Object) this).appendItemLayers(renderState, substitute, displayContext, level, owner, seed);
        ci.cancel();
    }
}
