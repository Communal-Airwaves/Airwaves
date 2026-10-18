package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VoiceModePayload(boolean simpleVoiceChat) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<VoiceModePayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "voice_mode"));
   public static final StreamCodec<ByteBuf, VoiceModePayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, VoiceModePayload::simpleVoiceChat, VoiceModePayload::new);
   }
}
