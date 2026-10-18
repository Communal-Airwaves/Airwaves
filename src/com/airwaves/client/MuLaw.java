package com.airwaves.client;

public final class MuLaw {
   private MuLaw() {
   }

   public static byte enc(short pcm) {
      int s = pcm;
      int sign = pcm >> 8 & 128;
      if (sign != 0) {
         s = -pcm;
      }

      if (s > 32635) {
         s = 32635;
      }

      s += 132;
      int exp = 7;

      for(int mask = 16384; (s & mask) == 0 && exp > 0; mask >>= 1) {
         --exp;
      }

      int mant = s >> exp + 3 & 15;
      return (byte)(~(sign | exp << 4 | mant));
   }

   public static short dec(byte u) {
      int v = ~u & 255;
      int sign = v & 128;
      int exp = v >> 4 & 7;
      int mant = v & 15;
      int s = (mant << 3) + 132 << exp;
      s -= 132;
      return (short)(sign != 0 ? -s : s);
   }
}
