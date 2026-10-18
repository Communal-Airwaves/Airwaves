package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VoiceFramePayload(byte[] data) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<VoiceFramePayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "voice_frame"));
   public static final StreamCodec<ByteBuf, VoiceFramePayload> CODEC = StreamCodec.composite(ByteBufCodecs.byteArray(1024), VoiceFramePayload::data, VoiceFramePayload::new);

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
