package com.airwaves.menu;

import com.airwaves.ModRegistry;
import com.airwaves.be.TapePlayerBE;
import com.airwaves.item.TapeItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class TapePlayerMenu extends AbstractContainerMenu {
   public static final int SLOT_X = 22;
   public static final int SLOT_Y = 122;
   public static final int INV_X = 20;
   public static final int INV_Y = 152;
   public static final int HOT_Y = 210;
   private final Container tape;
   private final ContainerData data;
   private final ContainerLevelAccess access;
   private final TapePlayerBE be;

   public TapePlayerMenu(int id, Inventory inv) {
      this(id, inv, new SimpleContainer(1), new SimpleContainerData(4), ContainerLevelAccess.NULL, (TapePlayerBE)null);
   }

   public TapePlayerMenu(int id, Inventory inv, Container tape, ContainerData data, ContainerLevelAccess access, TapePlayerBE be) {
      super((MenuType)ModRegistry.TAPE_MENU.get(), id);
      this.tape = tape;
      this.data = data;
      this.access = access;
      this.be = be;
      this.addSlot(new Slot(tape, 0, 22, 122) {
         public boolean mayPlace(ItemStack s) {
            return s.getItem() instanceof TapeItem;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });

      for(int r = 0; r < 3; ++r) {
         for(int c = 0; c < 9; ++c) {
            this.addSlot(new Slot(inv, c + r * 9 + 9, 20 + c * 18, 152 + r * 18));
         }
      }

      for(int c = 0; c < 9; ++c) {
         this.addSlot(new Slot(inv, c, 20 + c * 18, 210));
      }

      this.addDataSlots(data);
   }

   public int mode() {
      return this.data.get(0);
   }

   public int position() {
      return this.data.get(1);
   }

   public int length() {
      return this.data.get(2);
   }

   public boolean looping() {
      return this.data.get(3) != 0;
   }

   public ItemStack tape() {
      return ((Slot)this.slots.get(0)).getItem();
   }

   public boolean clickMenuButton(Player p, int id) {
      if (this.be != null && id >= 0 && id <= 3) {
         this.be.button(id);
         return true;
      } else {
         return false;
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
      return stillValid(this.access, p, (Block)ModRegistry.TAPE_PLAYER.get());
   }
}
