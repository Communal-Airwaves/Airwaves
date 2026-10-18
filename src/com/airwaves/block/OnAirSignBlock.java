package com.airwaves.block;

import com.airwaves.logic.RadioNetwork;
import com.airwaves.logic.ShapeUtil;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OnAirSignBlock extends Block {
   public static final DirectionProperty FACING;
   public static final BooleanProperty LIT;
   public static final int RADIUS = 12;
   private static final Map<Direction, VoxelShape> SHAPES;

   public OnAirSignBlock(BlockBehaviour.Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(LIT, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(new Property[]{FACING, LIT});
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      Direction d = c.getClickedFace().getAxis().isHorizontal() ? c.getClickedFace() : c.getHorizontalDirection().getOpposite();
      return (BlockState)this.defaultBlockState().setValue(FACING, d);
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return (VoxelShape)SHAPES.get(s.getValue(FACING));
   }

   protected BlockState rotate(BlockState s, Rotation r) {
      return (BlockState)s.setValue(FACING, r.rotate((Direction)s.getValue(FACING)));
   }

   protected BlockState mirror(BlockState s, Mirror m) {
      return s.rotate(m.getRotation((Direction)s.getValue(FACING)));
   }

   protected void onPlace(BlockState s, Level lv, BlockPos p, BlockState old, boolean moved) {
      if (!lv.isClientSide) {
         lv.scheduleTick(p, this, 10);
      }

   }

   protected void tick(BlockState s, ServerLevel lv, BlockPos p, RandomSource r) {
      boolean on = onAirNear(lv, p);
      if (on != (Boolean)s.getValue(LIT)) {
         lv.setBlock(p, (BlockState)s.setValue(LIT, on), 3);
      }

      lv.scheduleTick(p, this, 10);
   }

   private static boolean onAirNear(Level lv, BlockPos pos) {
      return RadioNetwork.onAirNear(lv, pos, 12);
   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
      LIT = BlockStateProperties.LIT;
      SHAPES = ShapeUtil.make((double)1.0F, (double)3.0F, (double)13.0F, (double)15.0F, (double)13.0F, (double)16.0F);
   }
}
