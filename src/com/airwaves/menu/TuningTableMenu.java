package com.airwaves.menu;

import com.airwaves.ModRegistry;
import com.airwaves.item.Tunable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class TuningTableMenu extends AbstractContainerMenu {
   public static final int INV_X = 47;
   public static final int INV_Y = 160;
   public static final int HOT_Y = 218;
   public static final int SLOT_X = 13;
   public static final int SLOT_Y = 31;
   private final Container tuner;
   private final ContainerLevelAccess access;

   public TuningTableMenu(int id, Inventory inv) {
      this(id, inv, ContainerLevelAccess.NULL);
   }

   public TuningTableMenu(int id, Inventory inv, ContainerLevelAccess access) {
      super((MenuType)ModRegistry.TUNING_MENU.get(), id);
      this.tuner = new SimpleContainer(1);
      this.access = access;
      this.addSlot(new Slot(this.tuner, 0, 13, 31) {
         public boolean mayPlace(ItemStack s) {
            return s.getItem() instanceof Tunable;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });

      for(int r = 0; r < 3; ++r) {
         for(int c = 0; c < 9; ++c) {
            this.addSlot(new Slot(inv, c + r * 9 + 9, 47 + c * 18, 160 + r * 18));
         }
      }

      for(int c = 0; c < 9; ++c) {
         this.addSlot(new Slot(inv, c, 47 + c * 18, 218));
      }

   }

   public ItemStack quickMoveStack(Player p, int idx) {
      ItemStack ret = ItemStack.EMPTY;
      Slot s = (Slot)this.slots.get(idx);
      if (s != null && s.hasItem()) {
         ItemStack st = s.getItem();
         ret = st.copy();
         if (idx == 0) {
            if (!this.moveItemStackTo(st, 1, 37, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(st, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (st.isEmpty()) {
            s.setByPlayer(ItemStack.EMPTY);
         } else {
            s.setChanged();
         }
      }

      return ret;
   }

   public boolean stillValid(Player p) {
      return stillValid(this.access, p, (Block)ModRegistry.TUNING_TABLE.get());
   }

   public void removed(Player p) {
      super.removed(p);
      this.access.execute((l, pos) -> this.clearContainer(p, this.tuner));
   }
}
