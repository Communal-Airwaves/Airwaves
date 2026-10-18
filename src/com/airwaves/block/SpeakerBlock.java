package com.airwaves.block;

import com.airwaves.be.SpeakerBE;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SpeakerBlock extends DeviceBlock {
   public static final MapCodec<SpeakerBlock> CODEC = simpleCodec(SpeakerBlock::new);

   public SpeakerBlock(BlockBehaviour.Properties p) {
      super(p, (double)2.0F, (double)0.0F, (double)2.0F, (double)14.0F, (double)16.0F, (double)14.0F);
   }

   protected SpeakerBlock(BlockBehaviour.Properties p, double[] box) {
      super(p, box);
   }

   protected MapCodec<SpeakerBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new SpeakerBE(p, s);
   }
}
