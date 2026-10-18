package com.airwaves.structure;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

final class StructureNodes {
   private StructureNodes() {
   }

   static Vec3 anchor(BlockEntity be, int slot) {
      Block var3 = be.getBlockState().getBlock();
      if (var3 instanceof StructureBlock b) {
         if (b.def.connectors.length != 0) {
            return b.def.connector(be.getBlockPos(), Math.min(slot, b.def.connectors.length - 1), (Direction.Axis)be.getBlockState().getValue(StructureBlock.AXIS));
         }
      }

      return Vec3.atCenterOf(be.getBlockPos());
   }
}
