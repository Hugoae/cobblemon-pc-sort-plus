package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** Server snapshot of the player's profile, plus whether undo is available. */
public record ProfilePayload(String profileJson, boolean canUndo) implements CustomPacketPayload {
    public static final Type<ProfilePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "profile")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfilePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ProfilePayload::profileJson,
            ByteBufCodecs.BOOL,
            ProfilePayload::canUndo,
            ProfilePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
