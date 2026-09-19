package net.xhugo.cobblemonpcsortplus.neoforge;

import java.nio.file.Path;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xhugo.cobblemonpcsortplus.platform.CpsPlatform;

public final class NeoForgePlatform implements CpsPlatform {
    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
