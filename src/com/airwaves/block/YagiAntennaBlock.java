package com.airwaves.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class YagiAntennaBlock extends Block {
   public static final DirectionProperty FACING;
   private static final VoxelShape NS;
   private static final VoxelShape EW;
   private static final VoxelShape WE;
   private static final VoxelShape SN;

   public YagiAntennaBlock(BlockBehaviour.Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)this.defaultBlockState().setValue(FACING, c.getHorizontalDirection());
   }

   protected BlockState rotate(BlockState s, Rotation r) {
      return (BlockState)s.setValue(FACING, r.rotate((Direction)s.getValue(FACING)));
   }

   protected BlockState mirror(BlockState s, Mirror m) {
      return s.rotate(m.getRotation((Direction)s.getValue(FACING)));
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      VoxelShape var10000;
      switch ((Direction)s.getValue(FACING)) {
         case SOUTH -> var10000 = SN;
         case EAST -> var10000 = WE;
         case WEST -> var10000 = EW;
         default -> var10000 = NS;
      }

      return var10000;
   }

   static {
      FACING = BlockStateProperties.HORIZONTAL_FACING;
      NS = Shapes.or(box((double)6.0F, (double)0.0F, (double)6.0F, (double)10.0F, (double)9.0F, (double)10.0F), new VoxelShape[]{box((double)7.0F, (double)8.0F, (double)0.0F, (double)9.0F, (double)11.0F, (double)16.0F), box((double)0.0F, (double)9.0F, (double)13.0F, (double)16.0F, (double)10.0F, (double)15.0F)});
      EW = Shapes.or(box((double)6.0F, (double)0.0F, (double)6.0F, (double)10.0F, (double)9.0F, (double)10.0F), new VoxelShape[]{box((double)0.0F, (double)8.0F, (double)7.0F, (double)16.0F, (double)11.0F, (double)9.0F), box((double)13.0F, (double)9.0F, (double)0.0F, (double)15.0F, (double)10.0F, (double)16.0F)});
      WE = Shapes.or(box((double)6.0F, (double)0.0F, (double)6.0F, (double)10.0F, (double)9.0F, (double)10.0F), new VoxelShape[]{box((double)0.0F, (double)8.0F, (double)7.0F, (double)16.0F, (double)11.0F, (double)9.0F), box((double)1.0F, (double)9.0F, (double)0.0F, (double)3.0F, (double)10.0F, (double)16.0F)});
      SN = Shapes.or(box((double)6.0F, (double)0.0F, (double)6.0F, (double)10.0F, (double)9.0F, (double)10.0F), new VoxelShape[]{box((double)7.0F, (double)8.0F, (double)0.0F, (double)9.0F, (double)11.0F, (double)16.0F), box((double)0.0F, (double)9.0F, (double)1.0F, (double)16.0F, (double)10.0F, (double)3.0F)});
   }
}
