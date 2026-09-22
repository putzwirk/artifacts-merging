package com.putzwirk.artifacts_merging_multiloader.mixin.client;

import com.putzwirk.artifacts_merging_multiloader.client.ClientIconCache;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiIconMixin {
    @Inject(method = "renderItem(Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At("HEAD"), cancellable = true)
    private void artifactsmerging$customIcon(ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci) {
        if (!ModItems.isRandomArtifact(stack)) {
            return;
        }
        ClientIconCache.Icon icon = ClientIconCache.icon(RandomArtifactItem.groupId(stack));
        if (icon == null) {
            return;
        }
        GuiGraphics self = (GuiGraphics) (Object) this;
        self.blit(icon.location(), x, y, 0, 0.0F, 0.0F, 16, 16, icon.width(), icon.height());
        ci.cancel();
    }

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private void artifactsmerging$resultBackground(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!ModItems.isRandomArtifact(stack)) {
            return;
        }
        ((GuiGraphics) (Object) this).fill(x, y, x + 16, y + 16, 0xFFFFFF00);
    }
}
