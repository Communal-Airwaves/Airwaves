package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TunePayload(boolean fm, double freq) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<TunePayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "tune"));
   public static final StreamCodec<ByteBuf, TunePayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TunePayload::fm, ByteBufCodecs.DOUBLE, TunePayload::freq, TunePayload::new);
   }
}
