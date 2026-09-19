package net.xhugo.cobblemonpcsortplus.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Player-owned sorting configuration. Box numbers are one-based, matching Cobblemon's PC UI.
 */
public final class SortProfile {
    public static final String SOURCE_TAG = "SOURCE";
    public static final String PROTECTED_TAG = "PROTECTED";
    public static final String FALLBACK_TAG = "FALLBACK";
    public static final String HIGH_IV_TAG = "HIGH_IV";

    public boolean enabled = true;
    /** Used when a High IV box has no per-box threshold of its own. */
    public int highIvPerfectStats = 3;
    public Map<Integer, Integer> boxHighIvPerfectStats = new LinkedHashMap<>();
    public String boxSortMode = BoxSortMode.NAME.serializedName();
    public Map<Integer, Set<String>> boxTags = new LinkedHashMap<>();

    public static SortProfile defaults() {
        return new SortProfile();
    }

    public void normalize() {
        if (boxTags == null) boxTags = new LinkedHashMap<>();
        if (boxHighIvPerfectStats == null) boxHighIvPerfectStats = new LinkedHashMap<>();
        Map<Integer, Set<String>> normalisedTags = new LinkedHashMap<>();
        boxTags.forEach((box, tags) -> {
            if (box != null && box > 0 && tags != null) {
                Set<String> normalised = new LinkedHashSet<>();
                tags.stream()
                        .map(SortProfile::normaliseTag)
                        // MAX_FRIENDSHIP was removed; strip it from older profile files.
                        .filter(tag -> !tag.isBlank() && !"MAX_FRIENDSHIP".equals(tag))
                        .forEach(normalised::add);
                if (!normalised.isEmpty()) normalisedTags.put(box, normalised);
            }
        });
        boxTags = normalisedTags;
        highIvPerfectStats = Math.max(0, Math.min(6, highIvPerfectStats));
        Map<Integer, Integer> normalisedThresholds = new LinkedHashMap<>();
        boxHighIvPerfectStats.forEach((box, threshold) -> {
            if (box != null && box > 0 && threshold != null) {
                normalisedThresholds.put(box, Math.max(0, Math.min(6, threshold)));
            }
        });
        boxHighIvPerfectStats = normalisedThresholds;
        boxSortMode = BoxSortMode.from(boxSortMode).serializedName();
    }

    public Set<String> tagsForBox(int boxNumber) {
        return new LinkedHashSet<>(boxTags.getOrDefault(boxNumber, Set.of()));
    }

    /** Protected boxes are skipped by routing and by the post-sort box order. */
    public boolean isProtectedBox(int boxNumber) {
        return tagsForBox(boxNumber).contains(PROTECTED_TAG);
    }

    public void setTagsForBox(int boxNumber, Set<String> tags) {
        Set<String> clean = new LinkedHashSet<>();
        tags.stream().map(SortProfile::normaliseTag).filter(tag -> !tag.isBlank()).forEach(clean::add);
        if (clean.isEmpty()) boxTags.remove(boxNumber);
        else boxTags.put(boxNumber, clean);
    }

    public int highIvThresholdForBox(int boxNumber) {
        return boxHighIvPerfectStats.getOrDefault(boxNumber, highIvPerfectStats);
    }

    public void setHighIvThresholdForBox(int boxNumber, int threshold) {
        if (boxNumber < 1) return;
        boxHighIvPerfectStats.put(boxNumber, Math.max(0, Math.min(6, threshold)));
    }

    public BoxSortMode finalSortMode() {
        return BoxSortMode.from(boxSortMode);
    }

    public void setFinalSortMode(BoxSortMode mode) {
        boxSortMode = (mode == null ? BoxSortMode.NAME : mode).serializedName();
    }

    public static String normaliseTag(String tag) {
        return tag == null ? "" : tag.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}
