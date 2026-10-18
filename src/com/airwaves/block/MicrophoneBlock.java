package com.airwaves.block;

import com.airwaves.be.MicrophoneBE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MicrophoneBlock extends DeviceBlock {
   public static final MapCodec<MicrophoneBlock> CODEC = simpleCodec(MicrophoneBlock::new);

   public MicrophoneBlock(BlockBehaviour.Properties p) {
      super(p, (double)4.0F, (double)0.0F, (double)3.0F, (double)12.0F, (double)12.0F, (double)13.0F);
   }

   protected MapCodec<MicrophoneBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new MicrophoneBE(p, s);
   }
}
