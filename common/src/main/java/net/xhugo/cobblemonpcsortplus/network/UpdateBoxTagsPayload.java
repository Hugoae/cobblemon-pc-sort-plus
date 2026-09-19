package net.xhugo.cobblemonpcsortplus.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** The complete tag set replaces the selected box's previous tag set. */
public record UpdateBoxTagsPayload(int boxNumber, String tagsJson) implements CustomPacketPayload {
    public static final Type<UpdateBoxTagsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CobblemonPCSortPlus.MOD_ID, "update_box_tags")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBoxTagsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateBoxTagsPayload::boxNumber,
            ByteBufCodecs.STRING_UTF8,
            UpdateBoxTagsPayload::tagsJson,
            UpdateBoxTagsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
