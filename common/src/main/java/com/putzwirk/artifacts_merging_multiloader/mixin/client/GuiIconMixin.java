package com.putzwirk.artifacts_merging_multiloader.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.putzwirk.artifacts_merging_multiloader.client.ClientIconCache;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class GuiIconMixin {
    @Inject(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    private void artifactsmerging$customIcon(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!ModItems.isRandomArtifact(stack)) {
            return;
        }
        ClientIconCache.Icon icon = ClientIconCache.icon(RandomArtifactItem.groupId(stack));
        if (icon == null) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y, 100.0F);
        RenderSystem.setShaderTexture(0, icon.location());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GuiComponent.blit(poseStack, 0, 0, 0, 0.0F, 0.0F, 16, 16, icon.width(), icon.height());
        ci.cancel();
    }

    @Inject(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
    private void artifactsmerging$resultBackground(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!ModItems.isRandomArtifact(stack)) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        GuiComponent.fill(poseStack, x, y, x + 16, y + 16, 0xFFFFFF00);
    }
}
