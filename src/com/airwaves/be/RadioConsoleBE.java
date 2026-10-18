package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.block.RadioConsoleBlock;
import com.airwaves.logic.RadioNetwork;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadioConsoleBE extends TunableBE {
   public static final int RADIUS = 8;
   public int tx;
   public int txOn;
   public int rx;
   public int rxOn;
   public int relays;
   public int radios;
   public double bestRange;
   private int lastCmp;

   public RadioConsoleBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.RADIO_CONSOLE_BE.get(), p, s);
   }

   public List<BlockEntity> linked() {
      List<BlockEntity> out = new ArrayList();
      if (this.level == null) {
         return out;
      } else {
         for(BlockEntity be : RadioNetwork.tunedNear(this.level, this.worldPosition, 8)) {
            if (be instanceof Tuned && !(be instanceof RadioConsoleBE)) {
               out.add(be);
            }
         }

         return out;
      }
   }

   public void refresh() {
      if (this.level != null && !this.level.isClientSide) {
         boolean hold = this.level.hasNeighborSignal(this.worldPosition);
         int t = 0;
         int tOn = 0;
         int r = 0;
         int rOn = 0;
         int rel = 0;
         int rad = 0;
         double best = (double)0.0F;

         for(BlockEntity be : this.linked()) {
            Tuned d = (Tuned)be;
            if (!d.getTuning().matches(this.tuning)) {
               d.setTuning(this.tuning);
               d.sync();
            }

            if (be instanceof TransmitterBE) {
               TransmitterBE x = (TransmitterBE)be;
               ++t;
               if (hold) {
                  x.consoleHold = this.level.getGameTime();
               }

               if (x.active) {
                  ++tOn;
                  best = Math.max(best, x.range);
               }
            } else if (be instanceof ReceiverBE) {
               ReceiverBE x = (ReceiverBE)be;
               ++r;
               if (x.sinkOn()) {
                  ++rOn;
               }
            } else if (be instanceof RelayBE) {
               ++rel;
            } else if (be instanceof RadioBE) {
               ++rad;
            }
         }

         boolean changed = t != this.tx || tOn != this.txOn || r != this.rx || rOn != this.rxOn || rel != this.relays || rad != this.radios || Math.abs(best - this.bestRange) > (double)1.0F;
         this.tx = t;
         this.txOn = tOn;
         this.rx = r;
         this.rxOn = rOn;
         this.relays = rel;
         this.radios = rad;
         this.bestRange = best;
         BlockState st = this.getBlockState();
         boolean lit = tOn > 0 || this.carrier;
         if (st.getBlock() instanceof RadioConsoleBlock && (Boolean)st.getValue(RadioConsoleBlock.ACTIVE) != lit) {
            this.level.setBlock(this.worldPosition, (BlockState)st.setValue(RadioConsoleBlock.ACTIVE, lit), 3);
         }

         if (changed) {
            this.sync();
         }

      }
   }

   public void broadcastTuning() {
      for(BlockEntity be : this.linked()) {
         Tuned d = (Tuned)be;
         d.setTuning(this.tuning);
         d.sync();
      }

   }

   protected void afterSignal() {
      int c = this.comparator();
      if (c != this.lastCmp && this.level != null) {
         this.lastCmp = c;
         this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
      }

   }

   public int comparator() {
      return this.carrier ? 1 + Math.round(this.quality * 14.0F) : 0;
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      super.addToGoggleTooltip(tip, sneaking);
      if (this.tx + this.rx + this.relays + this.radios == 0) {
         tip.add(Component.literal("    no devices within 8 blocks").withStyle(ChatFormatting.GOLD));
         return true;
      } else {
         if (this.tx > 0) {
            int var10001 = this.tx;
            tip.add(Component.literal("    " + var10001 + " transmitter" + (this.tx > 1 ? "s" : "") + ", " + this.txOn + " on air").withStyle(this.txOn > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
         }

         if (this.txOn > 0) {
            String var3 = this.bestRange >= (double)1000.0F ? String.format("%.1f km", this.bestRange / (double)1000.0F) : Math.round(this.bestRange) + " m";
            tip.add(Component.literal("    reach " + var3).withStyle(ChatFormatting.GRAY));
         }

         if (this.rx > 0) {
            tip.add(Component.literal("    " + this.rx + " receiver" + (this.rx > 1 ? "s" : "") + ", " + this.rxOn + " listening").withStyle(ChatFormatting.GRAY));
         }

         if (this.relays > 0) {
            tip.add(Component.literal("    " + this.relays + " relay tower" + (this.relays > 1 ? "s" : "")).withStyle(ChatFormatting.GRAY));
         }

         if (this.radios > 0) {
            tip.add(Component.literal("    " + this.radios + " radio" + (this.radios > 1 ? "s" : "")).withStyle(ChatFormatting.GRAY));
         }

         if (this.level != null && this.level.hasNeighborSignal(this.worldPosition)) {
            tip.add(Component.literal("    master switch: standby").withStyle(ChatFormatting.GOLD));
         }

         return true;
      }
   }

   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.saveAdditional(tag, r);
      tag.putIntArray("dev", new int[]{this.tx, this.txOn, this.rx, this.rxOn, this.relays, this.radios});
      tag.putDouble("best", this.bestRange);
   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.loadAdditional(tag, r);
      int[] d = tag.getIntArray("dev");
      if (d.length == 6) {
         this.tx = d[0];
         this.txOn = d[1];
         this.rx = d[2];
         this.rxOn = d[3];
         this.relays = d[4];
         this.radios = d[5];
      }

      this.bestRange = tag.getDouble("best");
   }
}
