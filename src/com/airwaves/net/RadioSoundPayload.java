package com.airwaves.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RadioSoundPayload(BlockPos sink, boolean handheld, ResourceLocation sound, float volume, float pitch, float quality, boolean fm, byte kind, long key, int offsetMs) implements CustomPacketPayload {
   public static final byte ONE_SHOT = 0;
   public static final byte MUSIC_START = 1;
   public static final byte MUSIC_STOP = 2;
   public static final CustomPacketPayload.Type<RadioSoundPayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "radio_sound"));
   public static final StreamCodec<FriendlyByteBuf, RadioSoundPayload> CODEC = CustomPacketPayload.codec(RadioSoundPayload::write, RadioSoundPayload::new);

   private RadioSoundPayload(FriendlyByteBuf b) {
      this(b.readBlockPos(), b.readBoolean(), b.readResourceLocation(), b.readFloat(), b.readFloat(), b.readFloat(), b.readBoolean(), b.readByte(), b.readLong(), b.readVarInt());
   }

   public RadioSoundPayload(BlockPos sink, boolean handheld, ResourceLocation sound, float volume, float pitch, float quality, boolean fm, byte kind, long key) {
      this(sink, handheld, sound, volume, pitch, quality, fm, kind, key, 0);
   }

   private void write(FriendlyByteBuf b) {
      b.writeBlockPos(this.sink);
      b.writeBoolean(this.handheld);
      b.writeResourceLocation(this.sound);
      b.writeFloat(this.volume);
      b.writeFloat(this.pitch);
      b.writeFloat(this.quality);
      b.writeBoolean(this.fm);
      b.writeByte(this.kind);
      b.writeLong(this.key);
      b.writeVarInt(Math.max(0, this.offsetMs));
   }

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
