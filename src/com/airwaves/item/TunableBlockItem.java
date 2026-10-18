package com.airwaves.item;

import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class TunableBlockItem extends BlockItem implements Tunable {
   public TunableBlockItem(Block b, Item.Properties p) {
      super(b, p);
   }

   public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
      tip.add(Component.literal(((Tuning)stack.getOrDefault((DataComponentType)ModRegistry.TUNING.get(), Tuning.DEFAULT)).display()).withStyle(ChatFormatting.AQUA));
   }
}
