package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.xhugo.cobblemonpcsortplus.sorter.SortResult;

/** Gray action-bar confirmations for sort and undo. Never writes to chat. */
final class SortMessages {
    private static final Style MUTED = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final Style VALUE = Style.EMPTY.withColor(ChatFormatting.WHITE);
    private static final Component BAR_SEP = Component.literal(" · ").withStyle(MUTED);

    private SortMessages() {
    }

    static Component sortActionBar(SortResult result) {
        MutableComponent line = moved(result.moved());
        if (result.unmatched() > 0) {
            line.append(BAR_SEP).append(counted("message.cobblemonpcsortplus.sort.bar.unmatched", result.unmatched()));
        }
        if (result.fullDestinations() > 0) {
            line.append(BAR_SEP).append(plural(
                    result.fullDestinations(),
                    "message.cobblemonpcsortplus.sort.bar.full.one",
                    "message.cobblemonpcsortplus.sort.bar.full.many"
            ));
        }
        if (result.skippedInvalidBoxes() > 0) {
            line.append(BAR_SEP).append(plural(
                    result.skippedInvalidBoxes(),
                    "message.cobblemonpcsortplus.sort.bar.skipped.one",
                    "message.cobblemonpcsortplus.sort.bar.skipped.many"
            ));
        }
        return line;
    }

    static Component undoActionBar() {
        return Component.translatable("message.cobblemonpcsortplus.undo.done").withStyle(MUTED);
    }

    static Component undoEmptyActionBar() {
        return Component.translatable("message.cobblemonpcsortplus.undo.empty").withStyle(MUTED);
    }

    private static MutableComponent moved(int count) {
        if (count <= 0) {
            return Component.translatable("message.cobblemonpcsortplus.sort.bar.none").withStyle(MUTED);
        }
        return plural(count, "message.cobblemonpcsortplus.sort.bar.moved.one", "message.cobblemonpcsortplus.sort.bar.moved.many");
    }

    private static MutableComponent plural(int count, String oneKey, String manyKey) {
        return Component.translatable(count == 1 ? oneKey : manyKey, value(count)).withStyle(MUTED);
    }

    private static MutableComponent counted(String key, int count) {
        return Component.translatable(key, value(count)).withStyle(MUTED);
    }

    private static Component value(int count) {
        return Component.literal(Integer.toString(count)).withStyle(VALUE);
    }
}
