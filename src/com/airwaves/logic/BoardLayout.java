package com.airwaves.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BoardLayout(String cells) {
   public static final Codec<BoardLayout> CODEC = RecordCodecBuilder.create((i) -> i.group(Codec.STRING.fieldOf("cells").forGetter(BoardLayout::cells)).apply(i, BoardLayout::new));
   public static final StreamCodec<ByteBuf, BoardLayout> STREAM;

   public static BoardLayout of(int[] entries) {
      StringBuilder b = new StringBuilder(40);

      for(int i = 0; i < 40; ++i) {
         int e = i < entries.length ? entries[i] : -1;
         b.append(e == -2 ? '-' : (e >= 0 && e < BoardDesign.Part.values().length ? BoardDesign.Part.values()[e].letter : '.'));
      }

      return new BoardLayout(b.toString());
   }

   public int[] parts() {
      int[] out = new int[40];

      for(int i = 0; i < 40; ++i) {
         char c = i < this.cells.length() ? this.cells.charAt(i) : 46;
         int p = -1;

         for(BoardDesign.Part part : BoardDesign.Part.values()) {
            if (part.letter == c) {
               p = part.ordinal();
            }
         }

         out[i] = p;
      }

      return out;
   }

   public boolean[] traces() {
      boolean[] out = new boolean[40];

      for(int i = 0; i < 40; ++i) {
         out[i] = i < this.cells.length() && this.cells.charAt(i) == '-';
      }

      return out;
   }

   public BoardAnalysis.Result analyse() {
      return BoardAnalysis.analyse(this.parts(), this.traces());
   }

   static {
      STREAM = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, BoardLayout::cells, BoardLayout::new);
   }
}
