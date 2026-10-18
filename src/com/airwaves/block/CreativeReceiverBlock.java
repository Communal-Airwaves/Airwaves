package com.airwaves.block;

import com.airwaves.Tier;
import com.airwaves.be.CreativeReceiverBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CreativeReceiverBlock extends ReceiverBlock {
   public CreativeReceiverBlock(BlockBehaviour.Properties p) {
      super(Tier.GIANT, p);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(CRYSTAL, 2)).setValue(ACTIVE, true));
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new CreativeReceiverBE(p, s);
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      return InteractionResult.PASS;
   }
}
