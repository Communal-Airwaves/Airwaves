package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.be.JammerBE;
import com.airwaves.kinetic.KineticDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class JammerBlock extends KineticDeviceBlock<JammerBE> {
   public static final BooleanProperty POWERED;
   private static final double[][] TERMINALS;

   public double[][] terminals() {
      return TERMINALS;
   }

   public JammerBlock(BlockBehaviour.Properties p) {
      super(p, new double[]{(double)0.0F, (double)0.0F, (double)2.0F, (double)16.0F, (double)13.0F, (double)14.0F});
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(POWERED, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      super.createBlockStateDefinition(b);
      b.add(new Property[]{POWERED});
   }

   public Class<JammerBE> getBlockEntityClass() {
      return JammerBE.class;
   }

   public BlockEntityType<? extends JammerBE> getBlockEntityType() {
      return (BlockEntityType)ModRegistry.JAMMER_BE.get();
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)super.getStateForPlacement(c).setValue(POWERED, c.getLevel().hasNeighborSignal(c.getClickedPos()));
   }

   protected void neighborChanged(BlockState s, Level l, BlockPos p, Block b, BlockPos from, boolean moved) {
      if (!l.isClientSide) {
         boolean pw = l.hasNeighborSignal(p);
         if (pw != (Boolean)s.getValue(POWERED)) {
            l.setBlock(p, (BlockState)s.setValue(POWERED, pw), 3);
         }
      }

   }

   static {
      POWERED = BlockStateProperties.POWERED;
      TERMINALS = new double[][]{{(double)11.0F, (double)1.0F, (double)2.0F, (double)13.0F, (double)3.0F, (double)3.0F}, {(double)3.0F, (double)1.0F, (double)2.0F, (double)5.0F, (double)3.0F, (double)3.0F}};
   }
}
