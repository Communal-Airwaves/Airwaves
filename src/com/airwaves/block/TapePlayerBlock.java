package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.be.TapePlayerBE;
import com.airwaves.item.TapeItem;
import com.airwaves.menu.TapePlayerMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TapePlayerBlock extends DeviceBlock {
   public static final MapCodec<TapePlayerBlock> CODEC = simpleCodec(TapePlayerBlock::new);

   public TapePlayerBlock(BlockBehaviour.Properties p) {
      super(p, (double)1.0F, (double)0.0F, (double)3.0F, (double)15.0F, (double)16.0F, (double)15.0F);
   }

   protected MapCodec<TapePlayerBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new TapePlayerBE(p, s);
   }

   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level lv, BlockState s, BlockEntityType<T> t) {
      return lv.isClientSide ? null : createTickerHelper(t, (BlockEntityType)ModRegistry.TAPE_PLAYER_BE.get(), (l, p, st, be) -> be.serverTick());
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState st, Level lv, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
      if (stack.getItem() instanceof TapeItem) {
         BlockEntity var9 = lv.getBlockEntity(pos);
         if (var9 instanceof TapePlayerBE) {
            TapePlayerBE be = (TapePlayerBE)var9;
            if (be.stack().isEmpty()) {
               if (!lv.isClientSide) {
                  be.inv.setItem(0, stack.split(1));
                  lv.playSound((Player)null, pos, (SoundEvent)SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 0.6F, 1.6F);
               }

               return ItemInteractionResult.sidedSuccess(lv.isClientSide);
            }
         }
      }

      return super.useItemOn(stack, st, lv, pos, pl, h, hit);
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      if (!lv.isClientSide) {
         pl.openMenu(st.getMenuProvider(lv, pos));
      }

      return InteractionResult.sidedSuccess(lv.isClientSide);
   }

   protected MenuProvider getMenuProvider(BlockState st, Level lv, BlockPos pos) {
      BlockEntity var5 = lv.getBlockEntity(pos);
      if (var5 instanceof TapePlayerBE be) {
         return new SimpleMenuProvider((id, inv, p) -> new TapePlayerMenu(id, inv, be.inv, be.data, ContainerLevelAccess.create(lv, pos), be), Component.translatable("container.airwaves.tape_player"));
      } else {
         return null;
      }
   }

   protected void neighborChanged(BlockState s, Level lv, BlockPos p, Block b, BlockPos from, boolean moved) {
      super.neighborChanged(s, lv, p, b, from, moved);
      if (!lv.isClientSide) {
         BlockEntity var8 = lv.getBlockEntity(p);
         if (var8 instanceof TapePlayerBE) {
            TapePlayerBE be = (TapePlayerBE)var8;
            be.onRedstone(lv.hasNeighborSignal(p));
         }
      }

   }

   protected void onRemove(BlockState s, Level lv, BlockPos p, BlockState ns, boolean moved) {
      if (!s.is(ns.getBlock())) {
         BlockEntity var7 = lv.getBlockEntity(p);
         if (var7 instanceof TapePlayerBE) {
            TapePlayerBE be = (TapePlayerBE)var7;
            Containers.dropContents(lv, p, be.inv);
         }
      }

      super.onRemove(s, lv, p, ns, moved);
   }

   protected boolean hasAnalogOutputSignal(BlockState s) {
      return true;
   }

   protected int getAnalogOutputSignal(BlockState s, Level lv, BlockPos p) {
      BlockEntity var5 = lv.getBlockEntity(p);
      byte var10000;
      if (var5 instanceof TapePlayerBE be) {
         if (be.mode == 1) {
            var10000 = 15;
            return var10000;
         }
      }

      var10000 = 0;
      return var10000;
   }
}
