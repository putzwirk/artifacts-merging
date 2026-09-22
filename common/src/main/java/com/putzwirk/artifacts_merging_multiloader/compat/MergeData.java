package com.putzwirk.artifacts_merging_multiloader.compat;

import java.util.List;

public record MergeData(String groupId, List<String> excluded, List<String> pool, String result) {
    public MergeData {
        excluded = List.copyOf(excluded);
        pool = List.copyOf(pool);
    }
}
