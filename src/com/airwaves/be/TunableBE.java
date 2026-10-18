package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import com.airwaves.compat.SableCompat;
import com.airwaves.logic.Emitter;
import com.airwaves.logic.RadioNetwork;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TunableBE extends BlockEntity implements Tuned, IHaveGoggleInformation {
   public Tuning tuning;
   public float quality;
   public boolean carrier;
   public long bestRoot;
   public Component stationName;

   protected TunableBE(BlockEntityType<?> t, BlockPos p, BlockState s) {
      super(t, p, s);
      this.tuning = Tuning.DEFAULT;
   }

   public Tuning getTuning() {
      return this.tuning;
   }

   public void setTuning(Tuning t) {
      this.tuning = t;
   }

   public Component getStationName() {
      return this.stationName;
   }

   public void onLoad() {
      super.onLoad();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.register(this);
      }

   }

   public void setRemoved() {
      super.setRemoved();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.unregister(this);
      }

   }

   public void sync() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }

   }

   public String stationLabel() {
      return this.stationName == null ? "" : this.stationName.getString();
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      tip.add(Component.literal("    ").append(this.getBlockState().getBlock().getName()).withStyle(ChatFormatting.WHITE));
      String var10001 = this.tuning.display();
      tip.add(Component.literal("    " + var10001 + (this.stationName != null ? "  " + this.stationName.getString() : "")).withStyle(ChatFormatting.AQUA));
      int bars = this.carrier ? 1 + Math.round(this.quality * 4.0F) : 0;
      var10001 = "▮".repeat(bars);
      tip.add(Component.literal("    " + var10001 + "▯".repeat(5 - bars) + "  " + (this.carrier ? Math.round(this.quality * 100.0F) + "% signal" : "no signal")).withStyle(this.carrier ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
      return true;
   }

   public void refresh() {
   }

   public double rxGain() {
      return (double)1.0F;
   }

   public Direction rxBeam() {
      return null;
   }

   public double antennaTop() {
      return SableCompat.global(this.level, this.worldPosition).y + (double)0.5F;
   }

   protected void afterSignal() {
   }

   public void updateSignal(List<Emitter> emitters, List<BlockPos> jam) {
      RadioNetwork.Best b = RadioNetwork.best(this.level, SableCompat.globalPos(this.level, this.worldPosition), this.antennaTop(), this.tuning, this.rxGain(), emitters, jam, this.rxBeam());
      float best = b.q();
      boolean c = best > 0.04F;
      boolean changed = c != this.carrier || Math.round(best * 50.0F) != Math.round(this.quality * 50.0F);
      this.bestRoot = b.root();
      this.quality = best;
      this.carrier = c;
      if (changed) {
         this.sync();
      }

      this.afterSignal();
   }

   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.saveAdditional(tag, r);
      tag.putBoolean("fm", this.tuning.fm());
      tag.putDouble("freq", this.tuning.freq());
      tag.putFloat("q", this.quality);
      tag.putBoolean("carrier", this.carrier);
      if (this.stationName != null) {
         tag.putString("station", Serializer.toJson(this.stationName, r));
      }

   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.loadAdditional(tag, r);
      if (tag.contains("freq")) {
         this.tuning = (new Tuning(tag.getBoolean("fm"), tag.getDouble("freq"))).clamped();
      }

      this.quality = tag.getFloat("q");
      this.carrier = tag.getBoolean("carrier");
      this.stationName = tag.contains("station") ? Serializer.fromJson(tag.getString("station"), r) : null;
   }

   public CompoundTag getUpdateTag(HolderLookup.Provider r) {
      return this.saveWithoutMetadata(r);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   protected void applyImplicitComponents(BlockEntity.DataComponentInput in) {
      Tuning t = (Tuning)in.get((DataComponentType)ModRegistry.TUNING.get());
      if (t != null) {
         this.tuning = t.clamped();
      }

      Component n = (Component)in.get(DataComponents.CUSTOM_NAME);
      if (n != null) {
         this.stationName = n;
      }

   }

   protected void collectImplicitComponents(DataComponentMap.Builder b) {
      b.set((DataComponentType)ModRegistry.TUNING.get(), this.tuning);
      if (this.stationName != null) {
         b.set(DataComponents.CUSTOM_NAME, this.stationName);
      }

   }

   public void removeComponentsFromTag(CompoundTag tag) {
      tag.remove("fm");
      tag.remove("freq");
      tag.remove("station");
   }
}
