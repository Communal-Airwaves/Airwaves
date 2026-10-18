package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SpectrumPayload(List<StationInfo> stations) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<SpectrumPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "spectrum"));
   public static final StreamCodec<ByteBuf, SpectrumPayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(StationInfo.STREAM.apply(ByteBufCodecs.list(64)), SpectrumPayload::stations, SpectrumPayload::new);
   }
}
