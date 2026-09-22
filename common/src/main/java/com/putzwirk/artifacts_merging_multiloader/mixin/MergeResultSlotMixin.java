package com.putzwirk.artifacts_merging_multiloader.mixin;

import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.item.RandomArtifactItem;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class MergeResultSlotMixin extends Slot {
    public MergeResultSlotMixin(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void artifactsmerging$onTake(Player player, ItemStack stack, CallbackInfo ci) {
        if (artifactsmerging$transform(player, stack)) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (artifactsmerging$transform(player, player.getInventory().getItem(i))) {
                break;
            }
        }
    }

    @Unique
    private boolean artifactsmerging$transform(Player player, ItemStack stack) {
        if (!ModItems.isRandomArtifact(stack)) {
            return false;
        }
        String resultId = RandomArtifactItem.resultId(stack);
        if (resultId == null) {
            return false;
        }
        Item picked = ItemLookup.item(resultId);
        if (picked == null) {
            return false;
        }
        ItemStack result = new ItemStack(picked);
        if (player.containerMenu.getCarried() == stack) {
            player.containerMenu.setCarried(result);
            stack.setCount(0);
        } else {
            int slot = player.getInventory().findSlotMatchingItem(stack);
            if (slot != -1) {
                player.getInventory().setItem(slot, result);
            }
        }
        player.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        return true;
    }
}
