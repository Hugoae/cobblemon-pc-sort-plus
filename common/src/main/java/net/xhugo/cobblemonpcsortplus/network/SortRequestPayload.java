package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Asks the server to sort Pokémon that currently sit in Source boxes. */
public record SortRequestPayload() implements CustomPacketPayload {
    public static final Type<SortRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "sort_request")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SortRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new SortRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
