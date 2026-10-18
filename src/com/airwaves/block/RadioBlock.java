package com.airwaves.block;

import com.airwaves.ModSounds;
import com.airwaves.be.RadioBE;
import com.airwaves.logic.Emitter;
import com.airwaves.logic.RadioNetwork;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class RadioBlock extends DeviceBlock {
   public static final MapCodec<RadioBlock> CODEC = simpleCodec(RadioBlock::new);
   public static final BooleanProperty POWERED;

   public RadioBlock(BlockBehaviour.Properties p) {
      super(p, (double)2.0F, (double)0.0F, (double)3.0F, (double)15.0F, (double)15.0F, (double)12.0F);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(POWERED, true));
   }

   protected MapCodec<RadioBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      super.createBlockStateDefinition(b);
      b.add(new Property[]{POWERED});
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new RadioBE(p, s);
   }

   protected InteractionResult useWithoutItem(BlockState s, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      if (!handEmpty(pl)) {
         return InteractionResult.PASS;
      } else if (lv.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity var7 = lv.getBlockEntity(pos);
         if (!(var7 instanceof RadioBE)) {
            return InteractionResult.CONSUME;
         } else {
            RadioBE be = (RadioBE)var7;
            if (pl.isShiftKeyDown() && lv instanceof ServerLevel) {
               ServerLevel sl = (ServerLevel)lv;
               Emitter e = RadioNetwork.seek(sl, pos, be.antennaTop(), be.tuning, be.rxGain());
               if (e != null) {
                  be.tuning = e.tuning();
                  be.sync();
                  if (!(Boolean)s.getValue(POWERED)) {
                     lv.setBlock(pos, (BlockState)s.setValue(POWERED, true), 3);
                  }
               }

               lv.playSound((Player)null, pos, (SoundEvent)ModSounds.SEEK.get(), SoundSource.BLOCKS, 0.4F, e != null ? 1.6F : 0.7F);
               String var10001 = be.tuning.display();
               pl.displayClientMessage(Component.literal(var10001 + (e != null && !e.name().isEmpty() ? "  " + e.name() : "")), true);
               return InteractionResult.CONSUME;
            } else {
               boolean on = !(Boolean)s.getValue(POWERED);
               lv.setBlock(pos, (BlockState)s.setValue(POWERED, on), 3);
               lv.playSound((Player)null, pos, (SoundEvent)ModSounds.SWITCH.get(), SoundSource.BLOCKS, 0.6F, on ? 1.2F : 0.9F);
               if (on) {
                  pl.displayClientMessage(Component.literal(be.tuning.display()), true);
               }

               return InteractionResult.CONSUME;
            }
         }
      }
   }

   static {
      POWERED = BlockStateProperties.POWERED;
   }
}
