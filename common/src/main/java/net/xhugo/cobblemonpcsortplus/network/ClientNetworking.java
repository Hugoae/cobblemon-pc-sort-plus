package net.xhugo.cobblemonpcsortplus.network;

import com.google.gson.Gson;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-only transport used by controls embedded in the Cobblemon PC screen. */
public final class ClientNetworking {
    private static final Gson GSON = new Gson();
    private static Consumer<CustomPacketPayload> toServer = payload -> {
        throw new IllegalStateException("Client networking is not bound");
    };

    private ClientNetworking() {
    }

    public static void bindToServer(Consumer<CustomPacketPayload> sender) {
        toServer = sender;
    }

    public static void onProfile(ProfilePayload payload) {
        ClientProfileCache.replace(payload.profileJson(), payload.canUndo());
    }

    public static void requestProfile() {
        ClientProfileCache.beginRequest();
        toServer.accept(new RequestProfilePayload());
    }

    public static void requestSort() {
        toServer.accept(new SortRequestPayload());
    }

    public static void requestSortAll() {
        toServer.accept(new SortAllRequestPayload());
    }

    public static void requestUndo() {
        toServer.accept(new UndoSortPayload());
    }

    public static void updateBoxTags(int boxNumber, Set<String> tags) {
        toServer.accept(new UpdateBoxTagsPayload(boxNumber, GSON.toJson(tags)));
    }

    public static void updateHighIvThreshold(int boxNumber, int threshold) {
        toServer.accept(new UpdateHighIvThresholdPayload(boxNumber, threshold));
    }

    public static void updateBoxSortMode(String mode) {
        toServer.accept(new UpdateBoxSortModePayload(mode));
    }
}
