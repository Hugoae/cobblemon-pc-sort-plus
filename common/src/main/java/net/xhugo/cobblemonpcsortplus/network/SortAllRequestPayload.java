package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Requests a full-PC pass rather than the normal SOURCE-only pass. */
public record SortAllRequestPayload() implements CustomPacketPayload {
    public static final Type<SortAllRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "sort_all_request")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SortAllRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new SortAllRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
