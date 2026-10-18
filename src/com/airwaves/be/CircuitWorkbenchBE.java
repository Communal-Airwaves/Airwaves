package com.airwaves.be;

import com.airwaves.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CircuitWorkbenchBE extends BlockEntity {
   public final SimpleContainer items = new SimpleContainer(41) {
      public void setChanged() {
         super.setChanged();
         CircuitWorkbenchBE.this.setChanged();
      }
   };
   private long traces;
   private int choice = -1;

   public CircuitWorkbenchBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.WORKBENCH_BE.get(), p, s);
   }

   public long traces() {
      return this.traces;
   }

   public int choice() {
      return this.choice;
   }

   public void setTraces(long m) {
      if (m != this.traces) {
         this.traces = m;
         this.setChanged();
      }

   }

   public void setChoice(int c) {
      if (c != this.choice) {
         this.choice = c;
         this.setChanged();
      }

   }

   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.saveAdditional(tag, r);
      ContainerHelper.saveAllItems(tag, this.items.getItems(), r);
      tag.putLong("traces", this.traces);
      tag.putInt("choice", this.choice);
   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.loadAdditional(tag, r);
      this.items.getItems().replaceAll((s) -> ItemStack.EMPTY);
      ContainerHelper.loadAllItems(tag, this.items.getItems(), r);
      this.traces = tag.getLong("traces");
      this.choice = tag.contains("choice") ? tag.getInt("choice") : -1;
   }
}
