package com.putzwirk.artifacts_merging_multiloader.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class StackData {
    private static final String GROUP = "MergeGroup";
    private static final String EXCLUDED = "MergeExcluded";
    private static final String POOL = "MergePool";
    private static final String RESULT = "MergeResult";

    private StackData() {
    }

    public static void write(ItemStack stack, MergeData data) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(GROUP, data.groupId());
        tag.put(EXCLUDED, stringList(data.excluded()));
        tag.put(POOL, stringList(data.pool()));
        if (data.result() != null) {
            tag.putString(RESULT, data.result());
        }
    }

    @Nullable
    public static MergeData read(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(GROUP)) {
            return null;
        }
        String result = tag.contains(RESULT) ? tag.getString(RESULT) : null;
        return new MergeData(tag.getString(GROUP), stringList(tag, EXCLUDED), stringList(tag, POOL), result);
    }

    private static ListTag stringList(List<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }

    private static List<String> stringList(CompoundTag tag, String key) {
        List<String> out = new ArrayList<>();
        ListTag list = tag.getList(key, 8);
        for (int i = 0; i < list.size(); i++) {
            out.add(list.getString(i));
        }
        return out;
    }
}
