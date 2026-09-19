package net.xhugo.cobblemonpcsortplus.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.xhugo.cobblemonpcsortplus.client.CpsKeybinds;
import net.xhugo.cobblemonpcsortplus.network.ClientNetworking;
import net.xhugo.cobblemonpcsortplus.network.ProfilePayload;

public final class CobblemonPCSortPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientNetworking.bindToServer(ClientPlayNetworking::send);
        ClientPlayNetworking.registerGlobalReceiver(ProfilePayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientNetworking.onProfile(payload)));
        for (KeyMapping mapping : CpsKeybinds.mappings()) {
            KeyBindingHelper.registerKeyBinding(mapping);
        }
    }
}
