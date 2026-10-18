package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.Tier;
import com.airwaves.be.TransmitterBE;
import com.airwaves.kinetic.KineticDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class TransmitterBlock extends KineticDeviceBlock<TransmitterBE> {
   public static final IntegerProperty CRYSTAL = IntegerProperty.create("crystal", 0, 2);
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
   public static final BooleanProperty ON_AIR = BooleanProperty.create("on_air");
   private static final double[][] BOX = new double[][]{{(double)0.0F, (double)0.0F, (double)3.0F, (double)16.0F, (double)16.0F, (double)14.0F}, {(double)0.0F, (double)0.0F, (double)1.0F, (double)16.0F, (double)16.0F, (double)15.0F}, {(double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F}, {(double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F}};
   public final Tier tier;
   private static final double[][][] TERMINALS = new double[][][]{{{(double)10.0F, (double)10.0F, (double)3.0F, (double)13.0F, (double)12.0F, (double)4.0F}, {(double)3.0F, (double)10.0F, (double)3.0F, (double)6.0F, (double)12.0F, (double)4.0F}}, {{(double)13.0F, (double)1.0F, (double)1.0F, (double)15.0F, (double)3.0F, (double)3.0F}, {(double)1.0F, (double)1.0F, (double)1.0F, (double)3.0F, (double)3.0F, (double)3.0F}}, {{(double)11.0F, (double)1.0F, (double)0.0F, (double)13.0F, (double)3.0F, (double)1.0F}, {(double)5.0F, (double)1.0F, (double)0.0F, (double)7.0F, (double)3.0F, (double)1.0F}}, {{(double)12.0F, (double)2.0F, (double)0.0F, (double)14.0F, (double)4.0F, (double)1.0F}, {(double)7.0F, (double)2.0F, (double)0.0F, (double)9.0F, (double)4.0F, (double)1.0F}}};

   public double[][] terminals() {
      return TERMINALS[this.tier.level - 1];
   }

   public TransmitterBlock(Tier tier, BlockBehaviour.Properties p) {
      super(p, BOX[tier.level - 1]);
      this.tier = tier;
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(CRYSTAL, 0)).setValue(ACTIVE, false)).setValue(ON_AIR, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      super.createBlockStateDefinition(b);
      b.add(new Property[]{CRYSTAL, ACTIVE, ON_AIR});
   }

   public Class<TransmitterBE> getBlockEntityClass() {
      return TransmitterBE.class;
   }

   public BlockEntityType<? extends TransmitterBE> getBlockEntityType() {
      return (BlockEntityType)ModRegistry.TRANSMITTER_BE.get();
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState st, Level lv, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
      ItemInteractionResult r = this.crystalInsert(stack, st, lv, pos, pl, CRYSTAL);
      return r != null ? r : super.useItemOn(stack, st, lv, pos, pl, h, hit);
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      if (handEmpty(pl) && pl.isShiftKeyDown() && (Integer)st.getValue(CRYSTAL) != 0) {
         if (!lv.isClientSide) {
            this.crystalRemove((BlockState)((BlockState)st.setValue(ACTIVE, false)).setValue(ON_AIR, false), lv, pos, CRYSTAL);
         }

         return InteractionResult.sidedSuccess(lv.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }
}
