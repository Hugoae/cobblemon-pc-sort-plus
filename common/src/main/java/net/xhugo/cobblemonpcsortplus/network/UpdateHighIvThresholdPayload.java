package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Per-box High IV threshold (perfect stats required, 0–6). */
public record UpdateHighIvThresholdPayload(int boxNumber, int perfectStats) implements CustomPacketPayload {
    public static final Type<UpdateHighIvThresholdPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "update_high_iv_threshold")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHighIvThresholdPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateHighIvThresholdPayload::boxNumber,
            ByteBufCodecs.VAR_INT,
            UpdateHighIvThresholdPayload::perfectStats,
            UpdateHighIvThresholdPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
