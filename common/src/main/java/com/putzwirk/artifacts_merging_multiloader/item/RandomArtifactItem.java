package com.putzwirk.artifacts_merging_multiloader.item;

import com.putzwirk.artifacts_merging_multiloader.client.ClientNames;
import com.putzwirk.artifacts_merging_multiloader.compat.ItemLookup;
import com.putzwirk.artifacts_merging_multiloader.compat.MergeData;
import com.putzwirk.artifacts_merging_multiloader.compat.MergeRoll;
import com.putzwirk.artifacts_merging_multiloader.compat.StackData;
import com.putzwirk.artifacts_merging_multiloader.config.MergeConfigManager;
import com.putzwirk.artifacts_merging_multiloader.config.MergeEntry;
import com.putzwirk.artifacts_merging_multiloader.platform.Services;
import com.putzwirk.artifacts_merging_multiloader.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RandomArtifactItem extends Item {

    public static final String TOOLTIP_KEY = "item.artifactsmerging.random_artifact.tooltip";
    public static final String EMPTY_KEY = "item.artifactsmerging.random_artifact.empty";

    public RandomArtifactItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(String groupId, List<String> inputIds) {
        MergeEntry entry = MergeConfigManager.byId(groupId);
        List<String> pool = entry == null ? List.of() : new ArrayList<>(entry.items);
        ItemStack stack = new ItemStack(ModItems.get());
        StackData.write(stack, new MergeData(groupId, inputIds, pool, null));
        return stack;
    }

    public static ItemStack generic() {
        return new ItemStack(ModItems.get());
    }

    @Nullable
    public static ItemStack open(ItemStack box) {
        if (!ModItems.isRandomArtifact(box)) {
            return null;
        }
        MergeData data = StackData.read(box);
        Item stored = data == null || data.result() == null ? null : ItemLookup.item(data.result());
        if (stored != null) {
            return new ItemStack(stored);
        }
        List<String> excluded = data == null ? List.of() : data.excluded();
        String resultId = MergeRoll.pick(poolIds(box), excluded);
        Item result = resultId == null ? null : ItemLookup.item(resultId);
        return result == null ? null : new ItemStack(result);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        ItemStack opened = open(stack);
        if (opened == null) {
            player.displayClientMessage(Component.translatable(EMPTY_KEY), true);
            return InteractionResultHolder.fail(stack);
        }
        stack.shrink(1);
        if (!player.getInventory().add(opened)) {
            player.drop(opened, false);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(TOOLTIP_KEY).withStyle(ChatFormatting.GRAY));
    }

    public static String groupId(ItemStack stack) {
        MergeData data = StackData.read(stack);
        if (data != null && !data.groupId().isEmpty()) {
            return data.groupId();
        }
        List<MergeEntry> groups = MergeConfigManager.groups();
        return groups.isEmpty() ? "" : groups.get(0).id;
    }

    public static List<String> poolIds(ItemStack stack) {
        MergeData data = StackData.read(stack);
        if (data != null && !data.pool().isEmpty()) {
            return new ArrayList<>(data.pool());
        }
        MergeEntry entry = MergeConfigManager.byId(groupId(stack));
        return entry == null ? new ArrayList<>() : new ArrayList<>(entry.items);
    }

    @Nullable
    public static String resultId(ItemStack stack) {
        MergeData data = StackData.read(stack);
        return data == null ? null : data.result();
    }

    @Override
    public Component getName(ItemStack stack) {
        if (Services.PLATFORM.isClient()) {
            String name = ClientNames.displayName(groupId(stack));
            if (name != null && !name.isBlank()) {
                return Component.literal(name);
            }
        }
        return super.getName(stack);
    }
}
