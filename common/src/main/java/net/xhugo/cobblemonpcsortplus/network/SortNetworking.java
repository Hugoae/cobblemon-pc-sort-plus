package net.xhugo.cobblemonpcsortplus.network;

import com.cobblemon.mod.common.Cobblemon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.xhugo.cobblemonpcsortplus.config.BoxSortMode;
import net.xhugo.cobblemonpcsortplus.config.SortProfile;
import net.xhugo.cobblemonpcsortplus.config.SortProfileStore;
import net.xhugo.cobblemonpcsortplus.platform.Services;
import net.xhugo.cobblemonpcsortplus.sorter.PCSorter;
import net.xhugo.cobblemonpcsortplus.sorter.PcSnapshot;
import net.xhugo.cobblemonpcsortplus.sorter.SortResult;
import net.xhugo.cobblemonpcsortplus.sorter.SortUndoStore;

/** Server-authoritative profile updates and PC sorting requests. */
public final class SortNetworking {
    /** Payload protocol version. Bump this when a packet layout changes. */
    public static final String PROTOCOL_VERSION = "2";

    private static final Gson GSON = new Gson();
    private static final Type TAG_SET_TYPE = new TypeToken<Set<String>>() {}.getType();

    private SortNetworking() {
    }

    public static void handleRequestProfile(ServerPlayer player) {
        sendProfile(player);
    }

    public static void handleSort(ServerPlayer player) {
        sort(player, false);
    }

    public static void handleSortAll(ServerPlayer player) {
        sort(player, true);
    }

    public static void handleUndo(ServerPlayer player) {
        undo(player);
    }

    public static void handleUpdateTags(ServerPlayer player, UpdateBoxTagsPayload payload) {
        updateTags(player, payload);
    }

    public static void handleUpdateHighIvThreshold(ServerPlayer player, UpdateHighIvThresholdPayload payload) {
        updateHighIvThreshold(player, payload);
    }

    public static void handleUpdateBoxSortMode(ServerPlayer player, UpdateBoxSortModePayload payload) {
        updateBoxSortMode(player, payload);
    }

    private static void sendProfile(ServerPlayer player) {
        SortProfile profile = SortProfileStore.load(player.getUUID());
        send(player, new ProfilePayload(
                SortProfileStore.serialize(profile),
                SortUndoStore.has(player.getUUID(), worldRoot(player))
        ));
    }

    private static void sort(ServerPlayer player, boolean allBoxes) {
        SortProfile profile = SortProfileStore.load(player.getUUID());
        profile.normalize();
        if (profile.enabled) {
            SortUndoStore.replace(
                    player.getUUID(),
                    PcSnapshot.capture(Cobblemon.INSTANCE.getStorage().getPC(player)),
                    worldRoot(player)
            );
        }
        SortResult result = allBoxes
                ? PCSorter.sortAll(player, profile)
                : PCSorter.sort(player, profile);
        sendProfile(player);
        player.displayClientMessage(SortMessages.sortActionBar(result), true);
    }

    private static void undo(ServerPlayer player) {
        PcSnapshot snapshot = SortUndoStore.take(player.getUUID(), worldRoot(player));
        if (snapshot == null) {
            sendProfile(player);
            player.displayClientMessage(SortMessages.undoEmptyActionBar(), true);
            return;
        }
        snapshot.restore(Cobblemon.INSTANCE.getStorage().getPC(player));
        sendProfile(player);
        player.displayClientMessage(SortMessages.undoActionBar(), true);
    }

    private static void updateTags(ServerPlayer player, UpdateBoxTagsPayload payload) {
        if (payload.boxNumber() < 1) return;
        Set<String> tags;
        try {
            tags = GSON.fromJson(payload.tagsJson(), TAG_SET_TYPE);
        } catch (Exception ignored) {
            return;
        }
        if (tags == null) tags = new LinkedHashSet<>();
        SortProfile profile = SortProfileStore.load(player.getUUID());
        profile.setTagsForBox(payload.boxNumber(), tags);
        if (SortProfileStore.save(player.getUUID(), profile)) sendProfile(player);
    }

    private static void updateHighIvThreshold(ServerPlayer player, UpdateHighIvThresholdPayload payload) {
        if (payload.boxNumber() < 1) return;
        SortProfile profile = SortProfileStore.load(player.getUUID());
        profile.setHighIvThresholdForBox(payload.boxNumber(), payload.perfectStats());
        if (SortProfileStore.save(player.getUUID(), profile)) sendProfile(player);
    }

    private static void updateBoxSortMode(ServerPlayer player, UpdateBoxSortModePayload payload) {
        SortProfile profile = SortProfileStore.load(player.getUUID());
        profile.setFinalSortMode(BoxSortMode.from(payload.mode()));
        if (SortProfileStore.save(player.getUUID(), profile)) sendProfile(player);
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        Services.PLATFORM.sendToPlayer(player, payload);
    }

    private static Path worldRoot(ServerPlayer player) {
        var server = player.getServer();
        return server == null ? null : server.getWorldPath(LevelResource.ROOT);
    }
}
