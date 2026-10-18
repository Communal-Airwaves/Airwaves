package com.airwaves.cable;

import net.minecraft.core.BlockPos;

public record NodeRef(BlockPos pos, int slot) {
   public NodeRef(BlockPos pos, int slot) {
      pos = pos.immutable();
      this.pos = pos;
      this.slot = slot;
   }

   public long key() {
      return this.pos.asLong() * 31L + (long)this.slot;
   }
}
