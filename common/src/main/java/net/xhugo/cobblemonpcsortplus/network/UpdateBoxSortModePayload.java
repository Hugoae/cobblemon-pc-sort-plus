package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Box order applied after Sort all (name, Pokédex, level, or none). */
public record UpdateBoxSortModePayload(String mode) implements CustomPacketPayload {
    public static final Type<UpdateBoxSortModePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "update_box_sort_mode")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBoxSortModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpdateBoxSortModePayload::mode,
            UpdateBoxSortModePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
