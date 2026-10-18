package com.airwaves.be;

import com.airwaves.cable.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public interface CableNode {
   LinkSet links();

   BlockPos nodePos();

   default int connectors() {
      return 1;
   }

   int maxLinks(int var1);

   default boolean accepts(int slot, CableType t) {
      return t.light;
   }

   default boolean junction(int slot) {
      return false;
   }

   Vec3 anchor(int var1);

   void cablesChanged();

   default int slotAt(Vec3 hit) {
      int best = 0;
      double bd = Double.MAX_VALUE;

      for(int i = 0; i < this.connectors(); ++i) {
         double d = this.anchor(i).distanceToSqr(hit);
         if (d < bd) {
            bd = d;
            best = i;
         }
      }

      return best;
   }

   static Vec3 facing(BlockEntity be, double px, double py, double pz) {
      double x = px / (double)16.0F;
      double y = py / (double)16.0F;
      double z = pz / (double)16.0F;
      BlockState st = be.getBlockState();
      if (st.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
         double ox = x;
         Direction f = (Direction)st.getValue(BlockStateProperties.HORIZONTAL_FACING);
         switch (f) {
            case SOUTH:
               x = (double)1.0F - x;
               z = (double)1.0F - z;
               break;
            case EAST:
               x = (double)1.0F - z;
               z = ox;
               break;
            case WEST:
               x = z;
               z = (double)1.0F - ox;
         }
      }

      BlockPos p = be.getBlockPos();
      return new Vec3((double)p.getX() + x, (double)p.getY() + y, (double)p.getZ() + z);
   }
}
