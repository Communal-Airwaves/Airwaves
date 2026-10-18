package com.airwaves.item;

import net.minecraft.world.item.ItemStack;

public interface Tunable {
   default boolean canTune(ItemStack stack) {
      return true;
   }
}
