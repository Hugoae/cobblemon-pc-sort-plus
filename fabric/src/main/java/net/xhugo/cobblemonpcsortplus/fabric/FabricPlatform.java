package net.xhugo.cobblemonpcsortplus.fabric;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.xhugo.cobblemonpcsortplus.platform.CpsPlatform;

public final class FabricPlatform implements CpsPlatform {
    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
