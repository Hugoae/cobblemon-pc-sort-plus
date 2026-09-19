package net.xhugo.cobblemonpcsortplus.fabric;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.xhugo.cobblemonpcsortplus.network.ProfilePayload;
import net.xhugo.cobblemonpcsortplus.network.RequestProfilePayload;
import net.xhugo.cobblemonpcsortplus.network.SortAllRequestPayload;
import net.xhugo.cobblemonpcsortplus.network.SortNetworking;
import net.xhugo.cobblemonpcsortplus.network.SortRequestPayload;
import net.xhugo.cobblemonpcsortplus.network.UndoSortPayload;
import net.xhugo.cobblemonpcsortplus.network.UpdateBoxSortModePayload;
import net.xhugo.cobblemonpcsortplus.network.UpdateBoxTagsPayload;
import net.xhugo.cobblemonpcsortplus.network.UpdateHighIvThresholdPayload;

final class FabricNetworking {
    private FabricNetworking() {
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(RequestProfilePayload.TYPE, RequestProfilePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SortRequestPayload.TYPE, SortRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(SortAllRequestPayload.TYPE, SortAllRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UndoSortPayload.TYPE, UndoSortPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateBoxTagsPayload.TYPE, UpdateBoxTagsPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateHighIvThresholdPayload.TYPE, UpdateHighIvThresholdPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateBoxSortModePayload.TYPE, UpdateBoxSortModePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ProfilePayload.TYPE, ProfilePayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestProfilePayload.TYPE, (payload, context) -> {
            handle(context, SortNetworking::handleRequestProfile);
        });
        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.TYPE, (payload, context) -> {
            handle(context, SortNetworking::handleSort);
        });
        ServerPlayNetworking.registerGlobalReceiver(SortAllRequestPayload.TYPE, (payload, context) -> {
            handle(context, SortNetworking::handleSortAll);
        });
        ServerPlayNetworking.registerGlobalReceiver(UndoSortPayload.TYPE, (payload, context) -> {
            handle(context, SortNetworking::handleUndo);
        });
        ServerPlayNetworking.registerGlobalReceiver(UpdateBoxTagsPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> SortNetworking.handleUpdateTags(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(UpdateHighIvThresholdPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> SortNetworking.handleUpdateHighIvThreshold(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(UpdateBoxSortModePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> SortNetworking.handleUpdateBoxSortMode(player, payload));
        });
    }

    private static void handle(ServerPlayNetworking.Context context, java.util.function.Consumer<ServerPlayer> handler) {
        ServerPlayer player = context.player();
        context.server().execute(() -> handler.accept(player));
    }
}
