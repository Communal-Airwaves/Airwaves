package com.airwaves.item;

import com.airwaves.ModRegistry;
import com.airwaves.logic.TapeData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TapeItem extends Item {
   public TapeItem(Item.Properties p) {
      super(p);
   }

   public static TapeData data(ItemStack s) {
      return (TapeData)s.getOrDefault((DataComponentType)ModRegistry.TAPE.get(), TapeData.EMPTY);
   }

   public void appendHoverText(ItemStack s, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
      TapeData d = data(s);
      if (d.blank()) {
         tip.add(Component.literal("Blank").withStyle(ChatFormatting.GRAY));
      } else {
         tip.add(Component.literal("Length " + TapeData.time(d.length())).withStyle(ChatFormatting.GOLD));
         tip.add(Component.literal(d.cues().size() + " sounds recorded").withStyle(ChatFormatting.DARK_GRAY));
         if (d.hasVoice()) {
            int var10001 = d.voiceSeconds();
            tip.add(Component.literal("Voice: " + TapeData.time(var10001 * 20)).withStyle(ChatFormatting.DARK_GRAY));
         }
      }

   }

   public boolean isFoil(ItemStack s) {
      return false;
   }
}
