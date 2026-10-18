package com.airwaves.be;

import net.minecraft.core.BlockPos;

public interface AudioSink {
   BlockPos sinkPos();

   boolean sinkOn();

   boolean carrier();

   float quality();

   boolean fm();

   default boolean ambientOn() {
      return this.sinkOn();
   }
}
