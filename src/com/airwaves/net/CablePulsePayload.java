package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CablePulsePayload(long[] from, int[] fromSlot, long[] to, int[] toSlot) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<CablePulsePayload> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("airwaves", "cable_pulse"));
   public static final StreamCodec<ByteBuf, CablePulsePayload> CODEC = StreamCodec.of((buf, p) -> {
      FriendlyByteBuf b = new FriendlyByteBuf(buf);
      b.writeVarInt(p.from.length);

      for(int i = 0; i < p.from.length; ++i) {
         b.writeLong(p.from[i]);
         b.writeVarInt(p.fromSlot[i]);
         b.writeLong(p.to[i]);
         b.writeVarInt(p.toSlot[i]);
      }

   }, (buf) -> {
      FriendlyByteBuf b = new FriendlyByteBuf(buf);
      int n = Math.min(512, b.readVarInt());
      long[] f = new long[n];
      long[] t = new long[n];
      int[] fs = new int[n];
      int[] ts = new int[n];

      for(int i = 0; i < n; ++i) {
         f[i] = b.readLong();
         fs[i] = b.readVarInt();
         t[i] = b.readLong();
         ts[i] = b.readVarInt();
      }

      return new CablePulsePayload(f, fs, t, ts);
   });

   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
