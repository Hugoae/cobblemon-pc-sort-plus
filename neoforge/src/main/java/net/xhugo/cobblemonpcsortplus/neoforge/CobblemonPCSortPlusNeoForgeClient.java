package net.xhugo.cobblemonpcsortplus.neoforge;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;
import net.xhugo.cobblemonpcsortplus.client.CpsKeybinds;
import net.xhugo.cobblemonpcsortplus.network.ClientNetworking;

@Mod(value = CobblemonPCSortPlus.MOD_ID, dist = Dist.CLIENT)
public final class CobblemonPCSortPlusNeoForgeClient {
    public CobblemonPCSortPlusNeoForgeClient(IEventBus modBus) {
        ClientNetworking.bindToServer(PacketDistributor::sendToServer);
        modBus.addListener(CobblemonPCSortPlusNeoForgeClient::registerKeys);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        for (KeyMapping mapping : CpsKeybinds.mappings()) {
            event.register(mapping);
        }
    }
}
