package com.airwaves.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class ShiftTooltips {
   private static final int WIDTH = 38;
   private static final int HL = 15653496;
   private static final int TEXT = 13079883;
   private static final int DIM = 10197915;

   private ShiftTooltips() {
   }

   public static void onTooltip(ItemTooltipEvent e) {
      ItemStack st = e.getItemStack();
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(st.getItem());
      if ("airwaves".equals(id.getNamespace())) {
         String base = st.getDescriptionId() + ".tooltip";
         if (I18n.exists(base + ".summary")) {
            List<Component> tip = e.getToolTip();
            List<Component> add = new ArrayList();
            boolean shift = Screen.hasShiftDown();
            add.add(Component.literal("Hold [").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal("Shift").withStyle(shift ? ChatFormatting.WHITE : ChatFormatting.GRAY)).append(Component.literal("] for Summary").withStyle(ChatFormatting.DARK_GRAY)));
            if (shift) {
               add.add(Component.empty());
               add.addAll(wrap(I18n.get(base + ".summary", new Object[0]), 13079883, 0));

               for(int i = 1; i <= 3 && I18n.exists(base + ".condition" + i); ++i) {
                  add.add(Component.empty());
                  add.add(Component.literal(I18n.get(base + ".condition" + i, new Object[0])).withStyle(Style.EMPTY.withColor(10197915)));
                  if (I18n.exists(base + ".behaviour" + i)) {
                     add.addAll(wrap(I18n.get(base + ".behaviour" + i, new Object[0]), 13079883, 1));
                  }
               }
            }

            tip.addAll(Math.min(1, tip.size()), add);
         }
      }
   }

   static List<Component> wrap(String text, int color, int indent) {
      List<Component> out = new ArrayList();
      String pad = " ".repeat(indent);
      StringBuilder line = new StringBuilder();
      List<String> lines = new ArrayList();

      for(String w : text.split(" ")) {
         int vis = w.replace("_", "").length();
         if (line.length() > 0 && line.toString().replace("_", "").length() + 1 + vis > 38 - indent) {
            lines.add(line.toString());
            line.setLength(0);
         }

         if (line.length() > 0) {
            line.append(' ');
         }

         line.append(w);
      }

      if (line.length() > 0) {
         lines.add(line.toString());
      }

      boolean hl = false;

      for(String l : lines) {
         MutableComponent c = Component.literal(pad);
         StringBuilder seg = new StringBuilder();

         for(char ch : l.toCharArray()) {
            if (ch == '_') {
               if (seg.length() > 0) {
                  c.append(Component.literal(seg.toString()).withStyle(Style.EMPTY.withColor(hl ? 15653496 : color)));
               }

               seg.setLength(0);
               hl = !hl;
            } else {
               seg.append(ch);
            }
         }

         if (seg.length() > 0) {
            c.append(Component.literal(seg.toString()).withStyle(Style.EMPTY.withColor(hl ? 15653496 : color)));
         }

         out.add(c);
      }

      return out;
   }
}
