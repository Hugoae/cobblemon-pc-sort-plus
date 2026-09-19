package net.xhugo.cobblemonpcsortplus.config;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortProfileTest {
    @Test
    void perBoxThresholdsAreIndependentAndClamped() {
        SortProfile profile = SortProfile.defaults();
        profile.setHighIvThresholdForBox(5, 6);
        profile.setHighIvThresholdForBox(6, 2);
        profile.setHighIvThresholdForBox(7, 99);
        assertEquals(6, profile.highIvThresholdForBox(5));
        assertEquals(2, profile.highIvThresholdForBox(6));
        assertEquals(6, profile.highIvThresholdForBox(7));
        assertEquals(3, profile.highIvThresholdForBox(8));
    }

    @Test
    void legacyGlobalThresholdRemainsTheFallback() {
        SortProfile profile = SortProfileStore.deserialize(
                "{\"highIvPerfectStats\":4,\"boxTags\":{\"5\":[\"HIGH_IV\"]}}\n"
        );
        assertEquals(4, profile.highIvThresholdForBox(5));
        assertEquals(4, profile.highIvThresholdForBox(12));
    }

    @Test
    void explicitThresholdSurvivesSerialization() {
        SortProfile profile = SortProfile.defaults();
        profile.setHighIvThresholdForBox(5, 6);
        profile.setHighIvThresholdForBox(6, 3);
        SortProfile restored = SortProfileStore.deserialize(SortProfileStore.serialize(profile));
        assertEquals(6, restored.highIvThresholdForBox(5));
        assertEquals(3, restored.highIvThresholdForBox(6));
    }

    @Test
    void invalidOrderFallsBackToNameAndModesCyclePredictably() {
        SortProfile profile = SortProfileStore.deserialize("{\"boxSortMode\":\"broken\"}");
        assertEquals(BoxSortMode.NAME, profile.finalSortMode());
        assertEquals(BoxSortMode.POKEDEX, profile.finalSortMode().next());
        assertEquals(BoxSortMode.LEVEL, BoxSortMode.POKEDEX.next());
        assertEquals(BoxSortMode.NONE, BoxSortMode.LEVEL.next());
        assertEquals(BoxSortMode.NAME, BoxSortMode.NONE.next());
    }

    @Test
    void obsoleteMaxFriendshipBoxTagsAreDropped() {
        SortProfile profile = SortProfileStore.deserialize(
                "{\"boxTags\":{\"4\":[\"WATER\",\"MAX_FRIENDSHIP\"]}}"
        );
        assertEquals(Set.of("WATER"), profile.tagsForBox(4));
    }

    @Test
    void leftoverPriorityFieldsAreIgnored() {
        SortProfile profile = SortProfileStore.deserialize(
                "{\"priority\":[\"LEGENDARY\",\"ALPHA\",\"SHINY\"],\"boxTags\":{\"2\":[\"WATER\"]}}"
        );
        assertEquals(Set.of("WATER"), profile.tagsForBox(2));
    }

    @Test
    void alphaBoxTagSurvivesSerialization() {
        SortProfile profile = SortProfile.defaults();
        profile.setTagsForBox(3, Set.of("alpha", "shiny"));
        SortProfile restored = SortProfileStore.deserialize(SortProfileStore.serialize(profile));
        assertEquals(Set.of("ALPHA", "SHINY"), restored.tagsForBox(3));
    }

    @Test
    void protectedBoxesSurviveNormalizationAndSerialization() {
        SortProfile profile = SortProfile.defaults();
        profile.setTagsForBox(60, Set.of("protected", "water"));
        assertTrue(profile.isProtectedBox(60));
        assertFalse(profile.isProtectedBox(59));
        SortProfile restored = SortProfileStore.deserialize(SortProfileStore.serialize(profile));
        assertTrue(restored.isProtectedBox(60));
        assertEquals(Set.of("PROTECTED", "WATER"), restored.tagsForBox(60));
    }
}
