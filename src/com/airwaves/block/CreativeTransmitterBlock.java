package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.Tier;
import com.airwaves.be.TransmitterBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CreativeTransmitterBlock extends TransmitterBlock {
   public CreativeTransmitterBlock(BlockBehaviour.Properties p) {
      super(Tier.GIANT, p);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(CRYSTAL, 2));
   }

   public BlockEntityType<? extends TransmitterBE> getBlockEntityType() {
      return (BlockEntityType)ModRegistry.CREATIVE_TX_BE.get();
   }

   public boolean hasShaftTowards(LevelReader w, BlockPos p, BlockState s, Direction face) {
      return false;
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      return InteractionResult.PASS;
   }
}
