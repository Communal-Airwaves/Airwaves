package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpectrumRequestPayload() implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<SpectrumRequestPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "spectrum_request"));
   public static final StreamCodec<ByteBuf, SpectrumRequestPayload> CODEC = StreamCodec.unit(new SpectrumRequestPayload());

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
