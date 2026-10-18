package com.airwaves.item;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.Tuning;
import com.airwaves.be.Tuned;
import com.airwaves.structure.StructureBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FrequencyCardItem extends Item implements Tunable {
   public FrequencyCardItem(Item.Properties p) {
      super(p);
   }

   public boolean canTune(ItemStack stack) {
      return stack.has((DataComponentType)ModRegistry.TUNING.get());
   }

   public boolean isFoil(ItemStack stack) {
      return stack.has((DataComponentType)ModRegistry.TUNING.get());
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Player pl = ctx.getPlayer();
      if (pl != null && pl.isSecondaryUseActive()) {
         Level lv = ctx.getLevel();
         BlockPos pos = ctx.getClickedPos();
         BlockState st = lv.getBlockState(pos);
         Block var8 = st.getBlock();
         BlockPos var10001;
         if (var8 instanceof StructureBlock) {
            StructureBlock sb = (StructureBlock)var8;
            var10001 = sb.corePos(st, pos);
         } else {
            var10001 = pos;
         }

         BlockEntity be = lv.getBlockEntity(var10001);
         if (be instanceof Tuned) {
            Tuned t = (Tuned)be;
            if (!lv.isClientSide) {
               ItemStack card = ctx.getItemInHand();
               card.set((DataComponentType)ModRegistry.TUNING.get(), t.getTuning());
               if (t.getStationName() != null) {
                  card.set(DataComponents.CUSTOM_NAME, t.getStationName());
               }

               lv.playSound((Player)null, pos, (SoundEvent)ModSounds.TUNE.get(), SoundSource.PLAYERS, 0.5F, 1.4F);
               pl.displayClientMessage(Component.literal("Copied " + t.getTuning().display()).withStyle(ChatFormatting.AQUA), true);
            }

            return InteractionResult.sidedSuccess(lv.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
      Tuning t = (Tuning)stack.get((DataComponentType)ModRegistry.TUNING.get());
      if (t != null) {
         tip.add(Component.literal(t.display()).withStyle(ChatFormatting.AQUA));
      } else {
         tip.add(Component.literal("Blank").withStyle(ChatFormatting.DARK_GRAY));
      }

   }
}
