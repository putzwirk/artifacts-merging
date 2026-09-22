package com.putzwirk.artifacts_merging_multiloader.mixin.client;

import com.putzwirk.artifacts_merging_multiloader.client.ClientIconCache;
import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemPreviewMixin {
    @Shadow
    public abstract BakedModel getModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int seed);

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void artifactsmerging$cycleModel(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
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
        cir.setReturnValue(getModel(new ItemStack(preview.get((int) (step % preview.size()))), level, entity, seed));
    }
}
