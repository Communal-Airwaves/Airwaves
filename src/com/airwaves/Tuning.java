package com.airwaves;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Tuning(boolean fm, double freq) {
   public static final Tuning DEFAULT = new Tuning(true, 98.1);
   public static final Codec<Tuning> CODEC = RecordCodecBuilder.create((i) -> i.group(Codec.BOOL.fieldOf("fm").forGetter(Tuning::fm), Codec.DOUBLE.fieldOf("freq").forGetter(Tuning::freq)).apply(i, Tuning::new));
   public static final StreamCodec<ByteBuf, Tuning> STREAM;

   public static double min(boolean fm) {
      return fm ? (double)87.5F : (double)530.0F;
   }

   public static double max(boolean fm) {
      return fm ? (double)108.0F : (double)1700.0F;
   }

   public Tuning clamped() {
      double f = Double.isFinite(this.freq) ? this.freq : (this.fm ? 98.1 : (double)1000.0F);
      f = Math.max(min(this.fm), Math.min(max(this.fm), f));
      return new Tuning(this.fm, (double)Math.round(f * (double)100.0F) / (double)100.0F);
   }

   public boolean matches(Tuning o) {
      return this.fm == o.fm && Math.round(this.freq * (double)100.0F) == Math.round(o.freq * (double)100.0F);
   }

   public String display() {
      return this.fm ? String.format(Locale.ROOT, "%.2f MHz FM", this.freq) : String.format(Locale.ROOT, "%.1f kHz AM", this.freq);
   }

   static {
      STREAM = StreamCodec.composite(ByteBufCodecs.BOOL, Tuning::fm, ByteBufCodecs.DOUBLE, Tuning::freq, Tuning::new);
   }
}
