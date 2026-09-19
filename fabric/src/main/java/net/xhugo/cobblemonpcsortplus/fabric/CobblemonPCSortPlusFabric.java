package net.xhugo.cobblemonpcsortplus.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.xhugo.cobblemonpcsortplus.sorter.SortUndoStore;

public final class CobblemonPCSortPlusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricNetworking.register();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                SortUndoStore.dropPlayer(handler.getPlayer().getUUID()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SortUndoStore.dropAll());
    }
}
