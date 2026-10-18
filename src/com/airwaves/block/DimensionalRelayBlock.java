package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.be.DimensionalRelayBE;
import com.airwaves.kinetic.KineticDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class DimensionalRelayBlock extends KineticDeviceBlock<DimensionalRelayBE> {
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
   private static final double[][] TERMINALS = new double[][]{{(double)13.0F, (double)2.0F, (double)0.0F, (double)15.0F, (double)4.0F, (double)1.0F}, {(double)1.0F, (double)2.0F, (double)0.0F, (double)3.0F, (double)4.0F, (double)1.0F}};

   public double[][] terminals() {
      return TERMINALS;
   }

   public DimensionalRelayBlock(BlockBehaviour.Properties p) {
      super(p, new double[]{(double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F});
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      super.createBlockStateDefinition(b);
      b.add(new Property[]{ACTIVE});
   }

   public Class<DimensionalRelayBE> getBlockEntityClass() {
      return DimensionalRelayBE.class;
   }

   public BlockEntityType<? extends DimensionalRelayBE> getBlockEntityType() {
      return (BlockEntityType)ModRegistry.DIM_RELAY_BE.get();
   }

   public void animateTick(BlockState s, Level lv, BlockPos p, RandomSource r) {
      if ((Boolean)s.getValue(ACTIVE)) {
         for(int i = 0; i < 2; ++i) {
            lv.addParticle(ParticleTypes.PORTAL, (double)p.getX() + (double)0.5F + (r.nextDouble() - (double)0.5F) * 0.4, (double)p.getY() + 0.8 + r.nextDouble() * 0.3, (double)p.getZ() + (double)0.5F + (r.nextDouble() - (double)0.5F) * 0.4, (r.nextDouble() - (double)0.5F) * 0.6, r.nextDouble() * 0.4, (r.nextDouble() - (double)0.5F) * 0.6);
         }

         if (r.nextInt(30) == 0) {
            lv.playLocalSound(p, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.08F, 1.6F + r.nextFloat() * 0.2F, false);
         }

      }
   }

   public void onRemove(BlockState s, Level lv, BlockPos p, BlockState ns, boolean moved) {
      if (!s.is(ns.getBlock()) && !lv.isClientSide) {
         BlockEntity var7 = lv.getBlockEntity(p);
         if (var7 instanceof DimensionalRelayBE) {
            DimensionalRelayBE be = (DimensionalRelayBE)var7;
            be.keepLoaded(false);
         }
      }

      super.onRemove(s, lv, p, ns, moved);
   }
}
