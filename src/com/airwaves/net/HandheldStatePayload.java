package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HandheldStatePayload(boolean on, boolean carrier, float quality, boolean fm, String label) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<HandheldStatePayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "handheld_state"));
   public static final StreamCodec<ByteBuf, HandheldStatePayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, HandheldStatePayload::on, ByteBufCodecs.BOOL, HandheldStatePayload::carrier, ByteBufCodecs.FLOAT, HandheldStatePayload::quality, ByteBufCodecs.BOOL, HandheldStatePayload::fm, ByteBufCodecs.stringUtf8(64), HandheldStatePayload::label, HandheldStatePayload::new);
   }
}
