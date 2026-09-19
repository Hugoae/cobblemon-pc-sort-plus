package net.xhugo.cobblemonpcsortplus.network;

import java.util.LinkedHashSet;
import java.util.Set;
import net.xhugo.cobblemonpcsortplus.config.BoxSortMode;
import net.xhugo.cobblemonpcsortplus.config.SortProfile;
import net.xhugo.cobblemonpcsortplus.config.SortProfileStore;

/** A read-through copy of the current player's server profile, used only to render the PC controls. */
public final class ClientProfileCache {
    private static SortProfile profile = SortProfile.defaults();
    private static boolean loaded;
    private static boolean canUndo;
    private static int revision;

    private ClientProfileCache() {
    }

    public static void beginRequest() {
        loaded = false;
        revision++;
    }

    public static void replace(String profileJson, boolean undoAvailable) {
        profile = SortProfileStore.deserialize(profileJson);
        canUndo = undoAvailable;
        loaded = true;
        revision++;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean canUndo() {
        return loaded && canUndo;
    }

    public static int revision() {
        return revision;
    }

    public static Set<String> tagsForBox(int boxNumber) {
        return profile.tagsForBox(boxNumber);
    }

    public static Set<String> clearTags(int boxNumber) {
        profile.setTagsForBox(boxNumber, Set.of());
        revision++;
        return Set.of();
    }

    public static boolean boxHasTags(int boxNumber) {
        return loaded && !profile.tagsForBox(boxNumber).isEmpty();
    }

    public static Set<String> toggleTag(int boxNumber, String tag) {
        Set<String> tags = new LinkedHashSet<>(profile.tagsForBox(boxNumber));
        String normalised = SortProfile.normaliseTag(tag);
        if (!tags.add(normalised)) tags.remove(normalised);
        profile.setTagsForBox(boxNumber, tags);
        revision++;
        return tags;
    }

    public static int highIvThreshold(int boxNumber) {
        return profile.highIvThresholdForBox(boxNumber);
    }

    public static int cycleHighIvThreshold(int boxNumber) {
        int threshold = (profile.highIvThresholdForBox(boxNumber) + 1) % 7;
        profile.setHighIvThresholdForBox(boxNumber, threshold);
        revision++;
        return threshold;
    }

    public static BoxSortMode finalSortMode() {
        return profile.finalSortMode();
    }

    public static BoxSortMode cycleFinalSortMode() {
        BoxSortMode mode = profile.finalSortMode().next();
        profile.setFinalSortMode(mode);
        revision++;
        return mode;
    }
}
