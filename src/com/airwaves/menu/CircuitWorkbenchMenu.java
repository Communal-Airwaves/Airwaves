package com.airwaves.menu;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.be.CircuitWorkbenchBE;
import com.airwaves.logic.BoardAnalysis;
import com.airwaves.logic.BoardDesign;
import com.airwaves.logic.BoardLayout;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CircuitWorkbenchMenu extends AbstractContainerMenu {
   public static final int GRID = 40;
   public static final int BOARD = 40;
   public static final int RESULT = 41;
   public static final int INV0 = 42;
   public static final int BX = 24;
   public static final int BY = 20;
   public static final int BOARD_X = 196;
   public static final int BOARD_Y = 125;
   public static final int OUT_X = 244;
   public static final int OUT_Y = 125;
   public static final int INV_X = 8;
   public static final int INV_Y = 158;
   public static final int DRAW = 0;
   public static final int ERASE = 100;
   public static final int AUTO = 200;
   public static final int CLEAR = 300;
   public static final int CHOOSE = 400;
   private static final int BITS = 10;
   private final SimpleContainer bench;
   private final CircuitWorkbenchBE be;
   private final ContainerListener listener;
   private final SimpleContainer result;
   private final ContainerLevelAccess access;
   private final DataSlot[] traceSlots;
   private final DataSlot choice;

   public CircuitWorkbenchMenu(int id, Inventory inv) {
      this(id, inv, ContainerLevelAccess.NULL, (CircuitWorkbenchBE)null);
   }

   public CircuitWorkbenchMenu(int id, Inventory inv, final ContainerLevelAccess access, CircuitWorkbenchBE be) {
      super((MenuType)ModRegistry.WORKBENCH_MENU.get(), id);
      this.listener = (cx) -> this.slotsChanged(cx);
      this.result = new SimpleContainer(1);
      this.traceSlots = new DataSlot[]{DataSlot.standalone(), DataSlot.standalone(), DataSlot.standalone(), DataSlot.standalone()};
      this.choice = DataSlot.standalone();
      this.access = access;
      this.be = be;
      this.bench = be != null ? be.items : new SimpleContainer(41);
      this.bench.addListener(this.listener);

      for(int r = 0; r < 5; ++r) {
         for(int c = 0; c < 8; ++c) {
            this.addSlot(new Slot(this.bench, r * 8 + c, 24 + c * 18, 20 + r * 18) {
               public boolean mayPlace(ItemStack s) {
                  return BoardDesign.Part.of(s) != null;
               }

               public int getMaxStackSize() {
                  return 1;
               }

               public int getMaxStackSize(ItemStack s) {
                  return 1;
               }
            });
         }
      }

      this.addSlot(new Slot(this.bench, 40, 196, 125) {
         public boolean mayPlace(ItemStack s) {
            return s.is((Item)ModRegistry.CIRCUIT_BOARD.get());
         }
      });
      this.addSlot(new Slot(this.result, 0, 244, 125) {
         public boolean mayPlace(ItemStack s) {
            return false;
         }

         public void onTake(Player p, ItemStack s) {
            CircuitWorkbenchMenu.this.consumeInputs();
            access.execute((l, pos) -> l.playSound((Player)null, pos, (SoundEvent)ModSounds.SWITCH.get(), SoundSource.BLOCKS, 0.6F, 1.6F));
            super.onTake(p, s);
         }
      });

      for(int r = 0; r < 3; ++r) {
         for(int c = 0; c < 9; ++c) {
            this.addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 158 + r * 18));
         }
      }

      for(int c = 0; c < 9; ++c) {
         this.addSlot(new Slot(inv, c, 8 + c * 18, 216));
      }

      for(DataSlot d : this.traceSlots) {
         this.addDataSlot(d);
      }

      this.choice.set(be != null ? be.choice() : -1);
      this.addDataSlot(this.choice);
      if (be != null) {
         this.setMask(be.traces());
         this.updateResult();
      }

   }

   public ItemStack[] cells() {
      ItemStack[] a = new ItemStack[40];

      for(int i = 0; i < 40; ++i) {
         a[i] = this.getSlot(i).getItem();
      }

      return a;
   }

   public int[] parts() {
      int[] p = new int[40];

      for(int i = 0; i < 40; ++i) {
         BoardDesign.Part pt = BoardDesign.Part.of(this.getSlot(i).getItem());
         p[i] = pt == null ? -1 : pt.ordinal();
      }

      return p;
   }

   private long mask() {
      long m = 0L;

      for(int k = 0; k < this.traceSlots.length; ++k) {
         m |= (long)(this.traceSlots[k].get() & 1023) << k * 10;
      }

      return m;
   }

   private void setMask(long m) {
      for(int k = 0; k < this.traceSlots.length; ++k) {
         this.traceSlots[k].set((int)(m >> k * 10) & 1023);
      }

      if (this.be != null) {
         this.be.setTraces(m);
      }

   }

   private void setChoice(int c) {
      this.choice.set(c);
      if (this.be != null) {
         this.be.setChoice(c);
      }

   }

   public boolean[] traces() {
      long m = this.mask();
      boolean[] t = new boolean[40];

      for(int i = 0; i < 40; ++i) {
         t[i] = (m >> i & 1L) != 0L;
      }

      return t;
   }

   public boolean trace(int i) {
      return (this.mask() >> i & 1L) != 0L;
   }

   private void setTrace(int i, boolean on) {
      long m = this.mask();
      this.setMask(on ? m | 1L << i : m & ~(1L << i));
   }

   public BoardAnalysis.Result check() {
      return BoardAnalysis.analyse(this.parts(), this.traces());
   }

   public boolean hasBlank() {
      return this.getSlot(40).getItem().is((Item)ModRegistry.CIRCUIT_BOARD.get());
   }

   public BoardDesign chosen(BoardAnalysis.Result r) {
      if (r.works() && !r.designs().isEmpty()) {
         int c = this.choice.get();
         return c >= 0 && c < BoardDesign.values().length && r.designs().contains(BoardDesign.values()[c]) ? BoardDesign.values()[c] : (BoardDesign)r.designs().get(0);
      } else {
         return null;
      }
   }

   private BoardLayout layout() {
      int[] p = this.parts();
      boolean[] t = this.traces();
      int[] e = new int[40];

      for(int i = 0; i < 40; ++i) {
         e[i] = p[i] >= 0 ? p[i] : (t[i] ? -2 : -1);
      }

      return BoardLayout.of(e);
   }

   private void consumeInputs() {
      for(int i = 0; i < 40; ++i) {
         this.bench.removeItem(i, 1);
      }

      this.bench.removeItem(40, 1);
      this.setMask(0L);
      this.updateResult();
   }

   private void updateResult() {
      BoardAnalysis.Result r = this.check();
      BoardDesign d = this.chosen(r);
      ItemStack out = ItemStack.EMPTY;
      if (d != null && this.hasBlank()) {
         out = new ItemStack(d.item());
         out.set((DataComponentType)ModRegistry.BOARD_LAYOUT.get(), this.layout());
      }

      if (!ItemStack.matches(out, this.result.getItem(0))) {
         this.result.setItem(0, out);
      }

   }

   public void slotsChanged(Container c) {
      if (c == this.bench) {
         this.updateResult();
      }

      super.slotsChanged(c);
   }

   public boolean clickMenuButton(Player p, int id) {
      if (id >= 0 && id < 40) {
         this.setTrace(id - 0, true);
      } else if (id >= 100 && id < 140) {
         this.setTrace(id - 100, false);
      } else if (id == 300) {
         this.setMask(0L);
      } else if (id >= 200 && id < 200 + BoardDesign.values().length) {
         this.autoPlace(p, BoardDesign.values()[id - 200]);
      } else {
         if (id < 400 || id >= 400 + BoardDesign.values().length) {
            return false;
         }

         this.setChoice(id - 400);
      }

      this.updateResult();
      return true;
   }

   private void autoPlace(Player p, BoardDesign d) {
      if (!p.level().isClientSide) {
         int[] lay = d.example();
         Inventory inv = p.getInventory();

         for(int i = 0; i < 40; ++i) {
            if (lay[i] >= 0 && this.getSlot(i).getItem().isEmpty()) {
               BoardDesign.Part part = BoardDesign.Part.values()[lay[i]];
               int from = find(inv, part.item());
               if (from >= 0) {
                  this.getSlot(i).set(inv.removeItem(from, 1));
               }
            }
         }

         if (this.getSlot(40).getItem().isEmpty()) {
            int from = find(inv, (Item)ModRegistry.CIRCUIT_BOARD.get());
            if (from >= 0) {
               this.getSlot(40).set(inv.removeItem(from, 1));
            }
         }

         for(int i = 0; i < 40; ++i) {
            if (lay[i] == -2 && this.getSlot(i).getItem().isEmpty()) {
               this.setTrace(i, true);
            }
         }

         this.setChoice(d.ordinal());
         this.access.execute((l, pos) -> l.playSound((Player)null, pos, (SoundEvent)ModSounds.SWITCH.get(), SoundSource.BLOCKS, 0.4F, 1.2F));
      }
   }

   private static int find(Inventory inv, Item item) {
      for(int i = 0; i < inv.items.size(); ++i) {
         if (((ItemStack)inv.items.get(i)).is(item)) {
            return i;
         }
      }

      return -1;
   }

   public List<BoardDesign> qualifies() {
      return this.check().designs();
   }

   public ItemStack quickMoveStack(Player p, int idx) {
      Slot s = (Slot)this.slots.get(idx);
      if (s != null && s.hasItem()) {
         ItemStack st = s.getItem();
         ItemStack ret = st.copy();
         if (idx == 41) {
            if (!this.moveItemStackTo(st, 42, 78, true)) {
               return ItemStack.EMPTY;
            } else {
               s.onQuickCraft(st, ret);
               s.onTake(p, ret);
               return ItemStack.EMPTY;
            }
         } else {
            if (idx < 42) {
               if (!this.moveItemStackTo(st, 42, 78, true)) {
                  return ItemStack.EMPTY;
               }
            } else if (st.is((Item)ModRegistry.CIRCUIT_BOARD.get())) {
               if (!this.moveItemStackTo(st, 40, 41, false)) {
                  return ItemStack.EMPTY;
               }
            } else {
               if (BoardDesign.Part.of(st) == null) {
                  return ItemStack.EMPTY;
               }

               int free = -1;

               for(int i = 0; i < 40 && free < 0; ++i) {
                  if (!this.getSlot(i).hasItem() && !this.trace(i)) {
                     free = i;
                  }
               }

               if (free < 0) {
                  return ItemStack.EMPTY;
               }

               this.getSlot(free).set(st.split(1));
            }

            if (st.isEmpty()) {
               s.setByPlayer(ItemStack.EMPTY);
            } else {
               s.setChanged();
            }

            if (st.getCount() == ret.getCount()) {
               return ItemStack.EMPTY;
            } else {
               s.onTake(p, st);
               return ItemStack.EMPTY;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.index >= 42;
   }

   public boolean canDragTo(Slot slot) {
      return slot.index != 41;
   }

   public boolean stillValid(Player p) {
      return stillValid(this.access, p, (Block)ModRegistry.CIRCUIT_WORKBENCH.get());
   }

   public void removed(Player p) {
      super.removed(p);
      this.bench.removeListener(this.listener);
      if (this.be == null) {
         this.access.execute((l, pos) -> this.clearContainer(p, this.bench));
      }

   }
}
