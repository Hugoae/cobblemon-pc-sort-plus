package net.xhugo.cobblemonpcsortplus.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.xhugo.cobblemonpcsortplus.network.ClientNetworking;
import net.xhugo.cobblemonpcsortplus.network.ProfilePayload;
import net.xhugo.cobblemonpcsortplus.network.RequestProfilePayload;
import net.xhugo.cobblemonpcsortplus.network.SortAllRequestPayload;
import net.xhugo.cobblemonpcsortplus.network.SortNetworking;
import net.xhugo.cobblemonpcsortplus.network.SortRequestPayload;
import net.xhugo.cobblemonpcsortplus.network.UndoSortPayload;
import net.xhugo.cobblemonpcsortplus.network.UpdateBoxSortModePayload;
import net.xhugo.cobblemonpcsortplus.network.UpdateBoxTagsPayload;
import net.xhugo.cobblemonpcsortplus.network.UpdateHighIvThresholdPayload;

final class NeoForgeNetworking {
    private NeoForgeNetworking() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SortNetworking.PROTOCOL_VERSION);
        registrar.playToServer(
                RequestProfilePayload.TYPE,
                RequestProfilePayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleRequestProfile(player);
                    }
                }
        );
        registrar.playToServer(
                SortRequestPayload.TYPE,
                SortRequestPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleSort(player);
                    }
                }
        );
        registrar.playToServer(
                SortAllRequestPayload.TYPE,
                SortAllRequestPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleSortAll(player);
                    }
                }
        );
        registrar.playToServer(
                UndoSortPayload.TYPE,
                UndoSortPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleUndo(player);
                    }
                }
        );
        registrar.playToServer(
                UpdateBoxTagsPayload.TYPE,
                UpdateBoxTagsPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleUpdateTags(player, payload);
                    }
                }
        );
        registrar.playToServer(
                UpdateHighIvThresholdPayload.TYPE,
                UpdateHighIvThresholdPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleUpdateHighIvThreshold(player, payload);
                    }
                }
        );
        registrar.playToServer(
                UpdateBoxSortModePayload.TYPE,
                UpdateBoxSortModePayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SortNetworking.handleUpdateBoxSortMode(player, payload);
                    }
                }
        );
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registrar.playToClient(
                    ProfilePayload.TYPE,
                    ProfilePayload.STREAM_CODEC,
                    (payload, context) -> ClientNetworking.onProfile(payload)
            );
        } else {
            registrar.playToClient(ProfilePayload.TYPE, ProfilePayload.STREAM_CODEC, (payload, context) -> {
            });
        }
    }
}
