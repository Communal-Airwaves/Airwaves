package com.airwaves.be;

import com.airwaves.Tuning;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface Tuned {
   Tuning getTuning();

   void setTuning(Tuning var1);

   Component getStationName();

   void sync();

   static Component chamberLine(int crystal) {
      return Component.literal("    Chamber: " + (crystal == 2 ? "amethyst block" : (crystal == 1 ? "amethyst shard" : "empty"))).withStyle(crystal > 0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_GRAY);
   }
}
