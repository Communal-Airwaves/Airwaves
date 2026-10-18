package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StaticBurstPayload(float strength) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<StaticBurstPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "static_burst"));
   public static final StreamCodec<ByteBuf, StaticBurstPayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, StaticBurstPayload::strength, StaticBurstPayload::new);
   }
}
