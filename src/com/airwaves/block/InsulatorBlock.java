package com.airwaves.block;

import com.airwaves.be.InsulatorBE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class InsulatorBlock extends DeviceBlock {
   public static final MapCodec<InsulatorBlock> CODEC = simpleCodec(InsulatorBlock::new);

   public InsulatorBlock(BlockBehaviour.Properties p) {
      super(p, (double)2.5F, (double)0.0F, (double)4.5F, (double)13.5F, (double)11.0F, (double)11.5F);
   }

   protected MapCodec<InsulatorBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new InsulatorBE(p, s);
   }
}
