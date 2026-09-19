package net.xhugo.cobblemonpcsortplus.sorter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.xhugo.cobblemonpcsortplus.config.SortProfile;

/**
 * Picks a destination box by AND-matching every routing tag, then breaking ties
 * by specificity (more tags), stricter High IV threshold, then lower box number.
 * Source and protected boxes never receive. Fallback is only used when no
 * matching destination has space.
 */
public final class BoxRouter {
    public static final int NONE = 0;
    public static final int FULL = -1;
    public static final int STAY = -2;

    private BoxRouter() {
    }

    public static int pick(
            SortProfile profile,
            int currentBox,
            int perfectIvs,
            Predicate<String> matchesTag,
            IntPredicate boxExists,
            IntPredicate hasEmptySlot
    ) {
        int specific = resolve(
                rankedMatching(profile, perfectIvs, matchesTag, boxExists),
                currentBox,
                boxExists,
                hasEmptySlot
        );
        if (specific > 0 || specific == STAY) return specific;

        int fallback = resolve(
                rankedFallback(profile, boxExists),
                currentBox,
                boxExists,
                hasEmptySlot
        );
        if (fallback != NONE) return fallback;
        return specific;
    }

    static List<Integer> rankedMatching(
            SortProfile profile,
            int perfectIvs,
            Predicate<String> matchesTag,
            IntPredicate boxExists
    ) {
        List<Integer> boxes = new ArrayList<>();
        profile.boxTags.forEach((box, tags) -> {
            if (box == null || !boxExists.test(box) || !isSpecificDestination(profile, box)) return;
            if (matchesAll(profile, box, perfectIvs, matchesTag)) boxes.add(box);
        });
        boxes.sort((left, right) -> compareDestinations(profile, left, right));
        return boxes;
    }

    static List<Integer> rankedFallback(SortProfile profile, IntPredicate boxExists) {
        List<Integer> boxes = new ArrayList<>();
        profile.boxTags.forEach((box, tags) -> {
            if (box == null || !boxExists.test(box) || !isFallbackDestination(profile, box)) return;
            boxes.add(box);
        });
        boxes.sort(Integer::compareTo);
        return boxes;
    }

    static int compareDestinations(SortProfile profile, int left, int right) {
        int bySpecificity = Integer.compare(routingCount(profile, right), routingCount(profile, left));
        if (bySpecificity != 0) return bySpecificity;
        int byIv = Integer.compare(ivRank(profile, right), ivRank(profile, left));
        if (byIv != 0) return byIv;
        return Integer.compare(left, right);
    }

    static boolean matchesAll(
            SortProfile profile,
            int box,
            int perfectIvs,
            Predicate<String> matchesTag
    ) {
        Set<String> routing = routingTags(profile.tagsForBox(box));
        if (routing.isEmpty()) return false;
        for (String tag : routing) {
            if (SortProfile.HIGH_IV_TAG.equals(tag)) {
                if (perfectIvs < profile.highIvThresholdForBox(box)) return false;
            } else if (!matchesTag.test(tag)) {
                return false;
            }
        }
        return true;
    }

    static Set<String> routingTags(Set<String> tags) {
        Set<String> routing = new LinkedHashSet<>();
        for (String tag : tags) {
            String normalised = SortProfile.normaliseTag(tag);
            if (normalised.isBlank() || isRoleTag(normalised)) continue;
            routing.add(normalised);
        }
        return routing;
    }

    private static int resolve(
            List<Integer> ranked,
            int currentBox,
            IntPredicate boxExists,
            IntPredicate hasEmptySlot
    ) {
        boolean hasUsable = false;
        for (int box : ranked) {
            if (!boxExists.test(box)) continue;
            hasUsable = true;
            if (box == currentBox) return STAY;
            if (hasEmptySlot.test(box)) return box;
        }
        return hasUsable ? FULL : NONE;
    }

    private static boolean isSpecificDestination(SortProfile profile, int box) {
        Set<String> tags = profile.tagsForBox(box);
        return !profile.isProtectedBox(box)
                && !tags.contains(SortProfile.SOURCE_TAG)
                && !routingTags(tags).isEmpty();
    }

    private static boolean isFallbackDestination(SortProfile profile, int box) {
        Set<String> tags = profile.tagsForBox(box);
        return tags.contains(SortProfile.FALLBACK_TAG)
                && !profile.isProtectedBox(box)
                && !tags.contains(SortProfile.SOURCE_TAG)
                && routingTags(tags).isEmpty();
    }

    private static int routingCount(SortProfile profile, int box) {
        return routingTags(profile.tagsForBox(box)).size();
    }

    private static int ivRank(SortProfile profile, int box) {
        return routingTags(profile.tagsForBox(box)).contains(SortProfile.HIGH_IV_TAG)
                ? profile.highIvThresholdForBox(box)
                : 0;
    }

    private static boolean isRoleTag(String tag) {
        return SortProfile.SOURCE_TAG.equals(tag)
                || SortProfile.PROTECTED_TAG.equals(tag)
                || SortProfile.FALLBACK_TAG.equals(tag);
    }
}
