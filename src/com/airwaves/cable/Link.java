package com.airwaves.cable;

import net.minecraft.core.BlockPos;

public record Link(int slot, BlockPos other, int otherSlot, CableType type) {
   public NodeRef far() {
      return new NodeRef(this.other, this.otherSlot);
   }
}
