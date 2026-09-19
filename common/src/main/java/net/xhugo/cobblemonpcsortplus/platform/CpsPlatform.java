package net.xhugo.cobblemonpcsortplus.platform;

import java.nio.file.Path;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** Loader-specific paths and serverbound packet delivery. */
public interface CpsPlatform {
    String loaderName();

    Path configDirectory();

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
