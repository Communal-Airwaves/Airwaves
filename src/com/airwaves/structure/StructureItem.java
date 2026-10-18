package com.airwaves.structure;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.Tuning;
import com.airwaves.item.Tunable;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StructureItem extends BlockItem {
   protected final StructureBlock sb;

   public StructureItem(StructureBlock b, Item.Properties p) {
      super(b, p);
      this.sb = b;
   }

   public static Direction.Axis axisFor(Player pl) {
      return pl == null ? Axis.X : pl.getDirection().getClockWise().getAxis();
   }

   public InteractionResult place(BlockPlaceContext ctx) {
      Level lv = ctx.getLevel();
      BlockPos at = ctx.getClickedPos();
      Player pl = ctx.getPlayer();
      Direction.Axis ax = axisFor(pl);
      StructureDef d = this.sb.def;

      for(int i = 0; i < d.size(); ++i) {
         BlockPos q = d.partPos(at, i, ax);
         if (!lv.isInWorldBounds(q) || !lv.getBlockState(q).canBeReplaced()) {
            if (lv.isClientSide && pl != null) {
               pl.displayClientMessage(Component.literal("✖").withStyle(ChatFormatting.RED), true);
            }

            return InteractionResult.FAIL;
         }
      }

      if (!lv.isClientSide) {
         BlockState base = (BlockState)this.sb.defaultBlockState().setValue(StructureBlock.AXIS, ax);

         for(int i = 0; i < d.size(); ++i) {
            lv.setBlock(d.partPos(at, i, ax), (BlockState)base.setValue(this.sb.part, i), 2);
         }

         for(int i = 0; i < d.size(); ++i) {
            BlockPos q = d.partPos(at, i, ax);
            lv.blockUpdated(q, this.sb);
         }

         BlockPos core = d.partPos(at, 0, ax);
         BlockEntity be = lv.getBlockEntity(core);
         ItemStack st = ctx.getItemInHand();
         if (be != null) {
            be.applyComponentsFromItemStack(st);
            be.setChanged();
         }

         this.onPlaced(lv, core, st);
         lv.playSound((Player)null, at, (SoundEvent)ModSounds.STRUCTURE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         if (pl == null || !pl.getAbilities().instabuild) {
            st.shrink(1);
         }
      }

      return InteractionResult.sidedSuccess(lv.isClientSide);
   }

   protected void onPlaced(Level lv, BlockPos core, ItemStack stack) {
   }

   public static class Tuned extends StructureItem implements Tunable {
      public Tuned(StructureBlock b, Item.Properties p) {
         super(b, p);
      }

      public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
         tip.add(Component.literal(((Tuning)stack.getOrDefault((DataComponentType)ModRegistry.TUNING.get(), Tuning.DEFAULT)).display()).withStyle(ChatFormatting.AQUA));
      }
   }
}
