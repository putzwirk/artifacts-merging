package com.putzwirk.artifacts_merging_multiloader.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MergeEntryTest {

    private static MergeEntry entry(Map<String, String> names) {
        return new MergeEntry("x", "", 2, List.of("a"), names, null);
    }

    @Test
    void exactLanguageWins() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("de_de", "Deutsch");
        names.put("en_us", "English");
        assertEquals("Deutsch", entry(names).displayName("de_de"));
    }

    @Test
    void fallsBackToBasePrefixedLanguage() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("de_at", "Oesterreichisch");
        names.put("en_us", "English");
        assertEquals("Oesterreichisch", entry(names).displayName("de_de"));
    }

    @Test
    void fallsBackToEnglish() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("fr_fr", "Francais");
        names.put("en_us", "English");
        assertEquals("English", entry(names).displayName("zz_zz"));
    }

    @Test
    void fallsBackToFirstValue() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("fr_fr", "Francais");
        names.put("it_it", "Italiano");
        assertEquals("Francais", entry(names).displayName("zz_zz"));
    }
}
