package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HandheldAudioPayload(UUID talker, byte[] data, float quality, boolean fm, float gain) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<HandheldAudioPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "handheld_audio"));
   public static final StreamCodec<ByteBuf, HandheldAudioPayload> CODEC;

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   static {
      CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, HandheldAudioPayload::talker, ByteBufCodecs.byteArray(1024), HandheldAudioPayload::data, ByteBufCodecs.FLOAT, HandheldAudioPayload::quality, ByteBufCodecs.BOOL, HandheldAudioPayload::fm, ByteBufCodecs.FLOAT, HandheldAudioPayload::gain, HandheldAudioPayload::new);
   }
}
