package net.xhugo.cobblemonpcsortplus.sorter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.xhugo.cobblemonpcsortplus.config.SortProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxRouterTest {
    @Test
    void bugPlusIvIsAndSoANormalAlphaGoesToTheAlphaBox() {
        Scenario pc = new Scenario()
                .box(2, "BUG", "HIGH_IV").iv(2, 1)
                .box(3, "ALPHA");
        assertEquals(3, pc.pick(1, 1, "ALPHA", "NORMAL"));
    }

    @Test
    void moreTagsBeatFewerTagsWhenBothBoxesMatch() {
        Scenario pc = new Scenario()
                .box(2, "BUG", "HIGH_IV").iv(2, 1)
                .box(3, "ALPHA");
        assertEquals(2, pc.pick(1, 1, "ALPHA", "BUG"));
    }

    @Test
    void fullBestBoxOverflowsToTheNextMatch() {
        Scenario pc = new Scenario()
                .box(2, "BUG", "HIGH_IV").iv(2, 1)
                .box(3, "ALPHA")
                .full(2);
        assertEquals(3, pc.pick(1, 1, "ALPHA", "BUG"));
    }

    @Test
    void fullMatchesOverflowToFallbackThenStayUnmatched() {
        Scenario pc = new Scenario()
                .box(2, "WATER")
                .box(10, "FALLBACK")
                .full(2);
        assertEquals(10, pc.pick(1, 0, "WATER"));

        pc.full(10);
        assertEquals(BoxRouter.FULL, pc.pick(1, 0, "WATER"));

        Scenario noFallback = new Scenario().box(2, "WATER").full(2);
        assertEquals(BoxRouter.FULL, noFallback.pick(1, 0, "WATER"));

        Scenario nothing = new Scenario().box(2, "WATER");
        assertEquals(BoxRouter.NONE, nothing.pick(1, 0, "FIRE"));
    }

    @Test
    void dualTypeBoxesAreStricterThanSingleType() {
        Scenario pc = new Scenario()
                .box(2, "WATER")
                .box(3, "WATER", "GROUND");
        assertEquals(3, pc.pick(1, 0, "WATER", "GROUND"));
        assertEquals(2, pc.pick(1, 0, "WATER"));
    }

    @Test
    void waterPlusHighIvBeatsWaterAlone() {
        Scenario pc = new Scenario()
                .box(2, "WATER")
                .box(8, "WATER", "HIGH_IV").iv(8, 6);
        assertEquals(8, pc.pick(1, 6, "WATER"));
        assertEquals(2, pc.pick(1, 5, "WATER"));
    }

    @Test
    void equalTagCountPrefersTheStricterIvBoxThenTheLowerNumber() {
        Scenario ivVsType = new Scenario()
                .box(2, "WATER")
                .box(8, "HIGH_IV").iv(8, 6);
        assertEquals(8, ivVsType.pick(1, 6, "WATER"));
        assertEquals(2, ivVsType.pick(1, 5, "WATER"));

        Scenario twoIvBoxes = new Scenario()
                .box(4, "HIGH_IV").iv(4, 3)
                .box(9, "HIGH_IV").iv(9, 6);
        assertEquals(9, twoIvBoxes.pick(1, 6));
        assertEquals(4, twoIvBoxes.pick(1, 3));

        Scenario shinyVsWater = new Scenario()
                .box(5, "SHINY")
                .box(6, "WATER");
        assertEquals(5, shinyVsWater.pick(1, 0, "SHINY", "WATER"));

        Scenario waterFirst = new Scenario()
                .box(2, "WATER")
                .box(6, "SHINY");
        assertEquals(2, waterFirst.pick(1, 0, "SHINY", "WATER"));
    }

    @Test
    void alreadyInTheBestBoxStaysEvenIfAWorseMatchHasSpace() {
        Scenario pc = new Scenario()
                .box(3, "WATER", "HIGH_IV").iv(3, 4)
                .box(8, "WATER")
                .box(10, "WATER");
        assertEquals(BoxRouter.STAY, pc.pick(3, 6, "WATER"));
    }

    @Test
    void aWorseResidentMovesToABetterVacancyAndDoesNotSlideToAWorseBox() {
        Scenario pc = new Scenario()
                .box(3, "WATER", "HIGH_IV").iv(3, 4)
                .box(8, "WATER")
                .box(10, "WATER");
        assertEquals(3, pc.pick(8, 6, "WATER"));

        pc.full(3);
        assertEquals(BoxRouter.STAY, pc.pick(8, 6, "WATER"));
    }

    @Test
    void sourceAndProtectedBoxesNeverReceive() {
        Scenario pc = new Scenario()
                .box(2, "SOURCE", "ALPHA")
                .box(3, "PROTECTED", "ALPHA")
                .box(4, "ALPHA");
        assertEquals(4, pc.pick(1, 0, "ALPHA"));
    }

    @Test
    void fallbackIsIgnoredWhenTheBoxAlsoHasARoutingTag() {
        Scenario pc = new Scenario()
                .box(2, "FALLBACK", "WATER")
                .box(9, "FALLBACK");
        assertEquals(2, pc.pick(1, 0, "WATER"));
        assertEquals(9, pc.pick(1, 0, "FIRE"));
    }

    @Test
    void severalFallbackBoxesCompactTowardTheLowestNumber() {
        Scenario pc = new Scenario()
                .box(4, "FALLBACK")
                .box(11, "FALLBACK");
        assertEquals(4, pc.pick(11, 0, "DRAGON"));
        pc.full(4);
        assertEquals(BoxRouter.STAY, pc.pick(11, 0, "DRAGON"));
    }

    @Test
    void missingBoxesAreSkipped() {
        Scenario pc = new Scenario()
                .box(80, "WATER")
                .box(3, "WATER")
                .missing(80);
        assertEquals(3, pc.pick(1, 0, "WATER"));
    }

    @Test
    void rankedOrderIsSpecificityThenIvThenNumber() {
        SortProfile profile = SortProfile.defaults();
        profile.setTagsForBox(2, Set.of("WATER"));
        profile.setTagsForBox(5, Set.of("WATER", "HIGH_IV"));
        profile.setHighIvThresholdForBox(5, 3);
        profile.setTagsForBox(8, Set.of("WATER", "HIGH_IV"));
        profile.setHighIvThresholdForBox(8, 6);
        profile.setTagsForBox(9, Set.of("HIGH_IV"));
        profile.setHighIvThresholdForBox(9, 6);
        List<Integer> ranked = BoxRouter.rankedMatching(
                profile,
                6,
                "WATER"::equals,
                box -> true
        );
        assertEquals(List.of(8, 5, 9, 2), ranked);
    }

    @Test
    void routingTagsDropSourceProtectedAndFallback() {
        Set<String> routing = BoxRouter.routingTags(Set.of("source", "WATER", "protected", "fallback", "HIGH_IV"));
        assertEquals(Set.of("WATER", "HIGH_IV"), routing);
        assertFalse(BoxRouter.matchesAll(SortProfile.defaults(), 1, 0, tag -> true));
        SortProfile profile = SortProfile.defaults();
        profile.setTagsForBox(1, Set.of("BUG", "HIGH_IV"));
        profile.setHighIvThresholdForBox(1, 2);
        assertFalse(BoxRouter.matchesAll(profile, 1, 1, "BUG"::equals));
        assertTrue(BoxRouter.matchesAll(profile, 1, 2, "BUG"::equals));
        assertFalse(BoxRouter.matchesAll(profile, 1, 6, tag -> false));
    }

    private static final class Scenario {
        private final SortProfile profile = SortProfile.defaults();
        private final Set<Integer> full = new HashSet<>();
        private final Set<Integer> missing = new HashSet<>();

        private Scenario box(int number, String... tags) {
            profile.setTagsForBox(number, Set.of(tags));
            return this;
        }

        private Scenario iv(int box, int threshold) {
            profile.setHighIvThresholdForBox(box, threshold);
            return this;
        }

        private Scenario full(int... boxes) {
            for (int box : boxes) full.add(box);
            return this;
        }

        private Scenario missing(int... boxes) {
            for (int box : boxes) missing.add(box);
            return this;
        }

        private int pick(int current, int perfectIvs, String... traits) {
            Set<String> has = Set.of(traits);
            return BoxRouter.pick(
                    profile,
                    current,
                    perfectIvs,
                    has::contains,
                    box -> box >= 1 && !missing.contains(box),
                    box -> !full.contains(box)
            );
        }
    }
}
