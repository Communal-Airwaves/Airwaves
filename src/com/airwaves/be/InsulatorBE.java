package com.airwaves.be;

import com.airwaves.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class InsulatorBE extends NodeBE {
   public InsulatorBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.INSULATOR_BE.get(), p, s);
   }

   public int maxLinks(int slot) {
      return 4;
   }

   public boolean junction(int slot) {
      return true;
   }

   public Vec3 anchor(int slot) {
      return CableNode.facing(this, (double)8.0F, 9.6, (double)8.0F);
   }
}
