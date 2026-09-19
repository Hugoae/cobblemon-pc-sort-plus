package net.xhugo.cobblemonpcsortplus.sorter;

/** Counts reported on the action bar after a sort. */
public record SortResult(int moved, int unmatched, int fullDestinations, int skippedInvalidBoxes) {
}
