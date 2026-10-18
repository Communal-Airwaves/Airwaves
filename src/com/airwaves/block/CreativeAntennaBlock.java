package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.be.CreativeAntennaBE;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CreativeAntennaBlock extends Block implements IBE<CreativeAntennaBE> {
   private static final VoxelShape SHAPE = Shapes.or(Block.box((double)3.0F, (double)0.0F, (double)3.0F, (double)13.0F, (double)4.0F, (double)13.0F), Block.box((double)6.0F, (double)4.0F, (double)6.0F, (double)10.0F, (double)16.0F, (double)10.0F));

   public CreativeAntennaBlock(BlockBehaviour.Properties p) {
      super(p);
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return SHAPE;
   }

   public Class<CreativeAntennaBE> getBlockEntityClass() {
      return CreativeAntennaBE.class;
   }

   public BlockEntityType<? extends CreativeAntennaBE> getBlockEntityType() {
      return (BlockEntityType)ModRegistry.CREATIVE_ANTENNA_BE.get();
   }
}
