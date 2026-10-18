package com.airwaves.block;

import com.airwaves.ModSounds;
import com.airwaves.be.RadioConsoleBE;
import com.airwaves.logic.Emitter;
import com.airwaves.logic.RadioNetwork;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class RadioConsoleBlock extends DeviceBlock {
   public static final MapCodec<RadioConsoleBlock> CODEC = simpleCodec(RadioConsoleBlock::new);
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

   public RadioConsoleBlock(BlockBehaviour.Properties p) {
      super(p, (double)0.0F, (double)0.0F, (double)1.0F, (double)16.0F, (double)16.0F, (double)15.0F);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, false));
   }

   protected MapCodec<RadioConsoleBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      super.createBlockStateDefinition(b);
      b.add(new Property[]{ACTIVE});
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new RadioConsoleBE(p, s);
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState st, Level lv, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
      ItemInteractionResult r = super.useItemOn(stack, st, lv, pos, pl, h, hit);
      if (r.consumesAction() && !lv.isClientSide) {
         BlockEntity var10 = lv.getBlockEntity(pos);
         if (var10 instanceof RadioConsoleBE) {
            RadioConsoleBE be = (RadioConsoleBE)var10;
            be.broadcastTuning();
         }
      }

      return r;
   }

   protected InteractionResult useWithoutItem(BlockState s, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      if (!handEmpty(pl)) {
         return InteractionResult.PASS;
      } else if (lv.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity var8 = lv.getBlockEntity(pos);
         if (var8 instanceof RadioConsoleBE) {
            RadioConsoleBE be = (RadioConsoleBE)var8;
            if (lv instanceof ServerLevel) {
               ServerLevel sl = (ServerLevel)lv;
               if (pl.isShiftKeyDown()) {
                  be.refresh();
                  String var10 = be.tuning.display();
                  pl.displayClientMessage(Component.literal(var10 + "  |  TX " + be.txOn + "/" + be.tx + "  RX " + be.rxOn + "/" + be.rx + (be.relays > 0 ? "  relays " + be.relays : "") + (be.radios > 0 ? "  radios " + be.radios : "")).withStyle(ChatFormatting.AQUA), true);
                  return InteractionResult.CONSUME;
               }

               Emitter e = RadioNetwork.seek(sl, pos, be.antennaTop(), be.tuning, be.rxGain());
               if (e != null) {
                  be.tuning = e.tuning();
                  be.sync();
                  be.broadcastTuning();
               }

               lv.playSound((Player)null, pos, (SoundEvent)ModSounds.SEEK.get(), SoundSource.BLOCKS, 0.4F, e != null ? 1.6F : 0.7F);
               String var10001 = be.tuning.display();
               pl.displayClientMessage(Component.literal(var10001 + (e != null && !e.name().isEmpty() ? "  " + e.name() : (e == null ? "  (no other station)" : ""))), true);
               return InteractionResult.CONSUME;
            }
         }

         return InteractionResult.CONSUME;
      }
   }

   protected boolean hasAnalogOutputSignal(BlockState s) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState s, Level lv, BlockPos pos) {
      BlockEntity var5 = lv.getBlockEntity(pos);
      int var10000;
      if (var5 instanceof RadioConsoleBE be) {
         var10000 = be.comparator();
      } else {
         var10000 = 0;
      }

      return var10000;
   }
}
