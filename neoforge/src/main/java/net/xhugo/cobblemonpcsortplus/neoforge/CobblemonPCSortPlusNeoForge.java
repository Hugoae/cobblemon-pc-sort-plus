package net.xhugo.cobblemonpcsortplus.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;
import net.xhugo.cobblemonpcsortplus.sorter.SortUndoStore;

@Mod(CobblemonPCSortPlus.MOD_ID)
public final class CobblemonPCSortPlusNeoForge {
    public CobblemonPCSortPlusNeoForge(IEventBus modBus) {
        modBus.addListener(NeoForgeNetworking::register);
        NeoForge.EVENT_BUS.addListener(CobblemonPCSortPlusNeoForge::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(CobblemonPCSortPlusNeoForge::onServerStopping);
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SortUndoStore.dropPlayer(event.getEntity().getUUID());
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        SortUndoStore.dropAll();
    }
}
