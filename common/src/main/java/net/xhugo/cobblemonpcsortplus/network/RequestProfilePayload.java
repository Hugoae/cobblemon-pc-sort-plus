package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Asks the server for the current profile. */
public record RequestProfilePayload() implements CustomPacketPayload {
    public static final Type<RequestProfilePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "request_profile")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestProfilePayload> STREAM_CODEC =
            StreamCodec.unit(new RequestProfilePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
