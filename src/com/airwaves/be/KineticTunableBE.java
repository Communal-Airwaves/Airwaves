package com.airwaves.be;

import com.airwaves.Config;
import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import com.airwaves.compat.SableCompat;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.power.PowerSupply;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KineticTunableBE extends KineticBlockEntity implements Tuned {
   public Tuning tuning;
   public float quality;
   public boolean carrier;
   public long bestRoot;
   public Component stationName;
   public final PowerSupply power;

   protected KineticTunableBE(BlockEntityType<?> t, BlockPos p, BlockState s) {
      super(t, p, s);
      this.tuning = Tuning.DEFAULT;
      this.power = new PowerSupply(this::stressImpact, this::ratedVolts);
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

   public String stationLabel() {
      return this.stationName == null ? "" : this.stationName.getString();
   }

   public void sync() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.sendData();
      }

   }

   public void initialize() {
      super.initialize();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.register(this);
      }

   }

   public void invalidate() {
      super.invalidate();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.unregister(this);
      }

   }

   public void refresh() {
   }

   public void networkRefresh() {
      this.power.update(this.rotationOk(), this.level == null ? 0L : this.level.getGameTime());
      this.refresh();
      if (!this.usesPower()) {
         this.power.refund();
      }

   }

   public double stressImpact() {
      return (double)0.0F;
   }

   public int ratedVolts() {
      return 120;
   }

   protected boolean usesPower() {
      return true;
   }

   public double rxGain() {
      return (double)1.0F;
   }

   public double antennaTop() {
      return SableCompat.global(this.level, this.worldPosition).y + (double)0.5F;
   }

   public BlockPos radioPos() {
      return SableCompat.globalPos(this.level, this.worldPosition);
   }

   public float rpm() {
      return Math.abs(this.getSpeed());
   }

   public boolean rotationOk() {
      return this.rpm() >= (float)(Integer)Config.MIN_RPM.get() && !this.isOverStressed();
   }

   public boolean turning() {
      return !Config.rotation() ? true : this.power.running();
   }

   public double powerFactor() {
      if (!Config.rotation()) {
         return (double)1.0F;
      } else if (this.power.source() == PowerSupply.Source.ELECTRIC) {
         return this.power.electricFactor();
      } else {
         double f = (double)Mth.clamp(this.rpm() / (float)(Integer)Config.FULL_RPM.get(), 0.0F, 2.0F);
         return (double)0.5F + (double)0.5F * f;
      }
   }

   public double altitudeFactor() {
      return (double)1.0F + (Double)Config.ALTITUDE_BONUS.get() * SableCompat.altitude(this.level, this.worldPosition) / (double)100.0F;
   }

   protected void radioTooltip(List<Component> tip, boolean running, double range) {
      String var10001 = this.tuning.display();
      tip.add(Component.literal("    " + var10001 + (this.stationName != null ? "  " + this.stationName.getString() : "")).withStyle(ChatFormatting.AQUA));
      if (running) {
         tip.add(Component.literal("    ▲ " + (int)range + " m").withStyle(ChatFormatting.GREEN));
      }

      if (Config.rotation()) {
         this.power.tooltip(tip, true, this.isOverStressed(), this.rpm());
      }

      if (SableCompat.altitude(this.level, this.worldPosition) > (double)8.0F) {
         Object[] var10002 = new Object[]{this.altitudeFactor()};
         tip.add(Component.literal("    airborne: ×" + String.format("%.2f", var10002) + " range").withStyle(ChatFormatting.GRAY));
      }

   }

   protected void write(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.write(tag, r, clientPacket);
      tag.putBoolean("fm", this.tuning.fm());
      tag.putDouble("freq", this.tuning.freq());
      tag.putFloat("q", this.quality);
      tag.putBoolean("carrier", this.carrier);
      if (this.stationName != null) {
         tag.putString("station", Serializer.toJson(this.stationName, r));
      }

      this.power.save(tag);
   }

   protected void read(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.read(tag, r, clientPacket);
      if (tag.contains("freq")) {
         this.tuning = (new Tuning(tag.getBoolean("fm"), tag.getDouble("freq"))).clamped();
      }

      this.quality = tag.getFloat("q");
      this.carrier = tag.getBoolean("carrier");
      this.stationName = tag.contains("station") ? Serializer.fromJson(tag.getString("station"), r) : null;
      this.power.load(tag);
   }

   protected void applyImplicitComponents(BlockEntity.DataComponentInput in) {
      super.applyImplicitComponents(in);
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
      super.collectImplicitComponents(b);
      b.set((DataComponentType)ModRegistry.TUNING.get(), this.tuning);
      if (this.stationName != null) {
         b.set(DataComponents.CUSTOM_NAME, this.stationName);
      }

   }

   public void removeComponentsFromTag(CompoundTag tag) {
      super.removeComponentsFromTag(tag);
      tag.remove("fm");
      tag.remove("freq");
      tag.remove("station");
   }
}
