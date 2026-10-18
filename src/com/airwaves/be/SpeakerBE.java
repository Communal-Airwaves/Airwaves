package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.logic.ClientSinks;
import com.airwaves.logic.RadioNetwork;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SpeakerBE extends NodeBE implements AudioSink {
   public boolean on;
   public boolean carrier;
   public boolean fm;
   public float quality;

   public SpeakerBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.SPEAKER_BE.get(), p, s);
   }

   public int maxLinks(int slot) {
      return 2;
   }

   public Vec3 anchor(int slot) {
      return CableNode.facing(this, (double)8.0F, (double)2.0F, (double)14.0F);
   }

   public void onLoad() {
      super.onLoad();
      if (this.level != null) {
         if (this.level.isClientSide) {
            ClientSinks.SINKS.add(this);
         } else {
            RadioNetwork.register(this);
         }

      }
   }

   public void setRemoved() {
      super.setRemoved();
      ClientSinks.SINKS.remove(this);
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.unregister(this);
      }

   }

   public void setState(boolean on, boolean carrier, float q, boolean fm) {
      boolean ch = on != this.on || carrier != this.carrier || fm != this.fm || Math.round(q * 50.0F) != Math.round(this.quality * 50.0F);
      this.on = on;
      this.carrier = carrier;
      this.quality = q;
      this.fm = fm;
      if (ch) {
         this.sync();
      }

   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      super.addToGoggleTooltip(tip, sneaking);
      tip.add(Component.literal(!this.on ? "    not driven by a receiver" : (this.carrier ? "    playing, " + Math.round(this.quality * 100.0F) + "% signal" : "    receiver has no station")).withStyle(this.on && this.carrier ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
      return true;
   }

   public BlockPos sinkPos() {
      return this.worldPosition;
   }

   public boolean sinkOn() {
      return this.on;
   }

   public boolean carrier() {
      return this.carrier;
   }

   public float quality() {
      return this.quality;
   }

   public boolean fm() {
      return this.fm;
   }

   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.saveAdditional(tag, r);
      tag.putBoolean("on", this.on);
      tag.putBoolean("carrier", this.carrier);
      tag.putBoolean("fm", this.fm);
      tag.putFloat("q", this.quality);
   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.loadAdditional(tag, r);
      this.on = tag.getBoolean("on");
      this.carrier = tag.getBoolean("carrier");
      this.fm = tag.getBoolean("fm");
      this.quality = tag.getFloat("q");
   }
}
