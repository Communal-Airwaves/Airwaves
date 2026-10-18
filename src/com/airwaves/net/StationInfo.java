package com.airwaves.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StationInfo(boolean fm, double freq, float quality, String name) {
   public static final StreamCodec<ByteBuf, StationInfo> STREAM;

   static {
      STREAM = StreamCodec.composite(ByteBufCodecs.BOOL, StationInfo::fm, ByteBufCodecs.DOUBLE, StationInfo::freq, ByteBufCodecs.FLOAT, StationInfo::quality, ByteBufCodecs.stringUtf8(64), StationInfo::name, StationInfo::new);
   }
}
