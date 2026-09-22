package com.putzwirk.artifacts_merging_multiloader.compat;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MergeRoll {
    private MergeRoll() {
    }

    @Nullable
    public static String pick(List<String> pool, Collection<String> excluded) {
        List<String> candidates = candidates(pool, excluded);
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    public static List<String> candidates(List<String> pool, Collection<String> excluded) {
        List<String> filtered = new ArrayList<>();
        List<String> available = new ArrayList<>();
        for (String id : pool) {
            if (!ItemLookup.exists(id)) {
                continue;
            }
            available.add(id);
            if (!excluded.contains(id)) {
                filtered.add(id);
            }
        }
        return filtered.isEmpty() ? available : filtered;
    }
}
