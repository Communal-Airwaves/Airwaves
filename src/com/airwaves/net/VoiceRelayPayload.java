package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VoiceRelayPayload(UUID talker, byte[] data) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<VoiceRelayPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "voice_relay"));
   public static final StreamCodec<ByteBuf, VoiceRelayPayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, VoiceRelayPayload::talker, ByteBufCodecs.byteArray(1024), VoiceRelayPayload::data, VoiceRelayPayload::new);
   }
}
