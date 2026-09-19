package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Asks the server to restore the last captured PC snapshot. */
public record UndoSortPayload() implements CustomPacketPayload {
    public static final Type<UndoSortPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "undo_sort")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UndoSortPayload> STREAM_CODEC =
            StreamCodec.unit(new UndoSortPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
