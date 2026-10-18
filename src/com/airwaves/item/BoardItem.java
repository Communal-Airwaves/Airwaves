package com.airwaves.item;

import com.airwaves.ModRegistry;
import com.airwaves.logic.BoardAnalysis;
import com.airwaves.logic.BoardDesign;
import com.airwaves.logic.BoardLayout;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class BoardItem extends Item {
   public final BoardDesign design;

   public BoardItem(BoardDesign design, Item.Properties p) {
      super(p);
      this.design = design;
   }

   public static BoardLayout layout(ItemStack st) {
      BoardLayout l = (BoardLayout)st.get((DataComponentType)ModRegistry.BOARD_LAYOUT.get());
      if (l != null) {
         return l;
      } else {
         Item var3 = st.getItem();
         BoardLayout var10000;
         if (var3 instanceof BoardItem) {
            BoardItem b = (BoardItem)var3;
            var10000 = b.design.exampleLayout();
         } else {
            var10000 = BoardDesign.LOGIC.exampleLayout();
         }

         return var10000;
      }
   }

   public void appendHoverText(ItemStack st, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
      BoardAnalysis.Result r = layout(st).analyse();
      if (r.works()) {
         MutableComponent stages = Component.empty();
         boolean firstOne = true;

         for(Map.Entry<BoardAnalysis.Stage, Integer> e : r.counts().entrySet()) {
            if (!firstOne) {
               stages.append(Component.literal(", "));
            }

            stages.append(Component.literal(String.valueOf(e.getValue()) + " ")).append(Component.translatable(((BoardAnalysis.Stage)e.getKey()).key()));
            firstOne = false;
         }

         if (!firstOne) {
            tip.add(stages.withStyle(ChatFormatting.GRAY));
         }

         tip.add(Component.translatable("tooltip.airwaves.board.program", new Object[]{BoardAnalysis.describe(BoardAnalysis.program(r))}).withStyle(ChatFormatting.DARK_AQUA));
         tip.add(Component.translatable("tooltip.airwaves.board." + this.design.id).withStyle(ChatFormatting.DARK_GRAY));
      }
   }
}
