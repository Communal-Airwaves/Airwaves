package com.airwaves.power;

import com.airwaves.Config;
import com.airwaves.api.MachinePower;
import com.airwaves.be.CreativeTransmitterBE;
import com.airwaves.be.DimensionalRelayBE;
import com.airwaves.be.JammerBE;
import com.airwaves.be.KineticTunableBE;
import com.airwaves.structure.StructureBlock;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PowerSupply implements MachinePower {
   private static final float MIN_FRACTION = 0.25F;
   private static final double BANK_SECONDS = (double)2.0F;
   private final DoubleSupplier stress;
   private final IntSupplier volts;
   private double stored;
   private double lastUsed;
   private Source source;
   private float fraction;
   private long lastFed;
   private boolean wired;

   public PowerSupply(DoubleSupplier stress, IntSupplier volts) {
      this.source = PowerSupply.Source.NONE;
      this.lastFed = -1000L;
      this.stress = stress;
      this.volts = volts;
   }

   public static int watts(double stress) {
      return Math.max(1, (int)Math.round(stress * (Double)Config.WATTS_PER_STRESS.get()));
   }

   public int watts() {
      return watts(this.stress.getAsDouble());
   }

   public int ratedVolts() {
      return this.volts.getAsInt();
   }

   private double bank() {
      return (double)this.watts() * (double)2.0F;
   }

   public Source source() {
      return this.source;
   }

   public boolean running() {
      return this.source != PowerSupply.Source.NONE;
   }

   public boolean onRotation() {
      return this.source == PowerSupply.Source.ROTATION;
   }

   public boolean onElectricity() {
      return this.source == PowerSupply.Source.ELECTRIC;
   }

   public float fraction() {
      return this.fraction;
   }

   public boolean wired() {
      return this.wired;
   }

   public double storedJoules() {
      return this.stored;
   }

   public double wantWatts() {
      double room = this.bank() - this.stored;
      return room <= 1.0E-6 ? (double)0.0F : Math.min((double)this.watts() * (double)1.5F, room * (double)20.0F);
   }

   public void feed(double joules, long gameTime) {
      if (joules > (double)0.0F) {
         this.stored = Math.min(this.bank(), this.stored + joules);
      }

      this.lastFed = gameTime;
   }

   public boolean update(boolean rotationOk, long gameTime) {
      Source was = this.source;
      float wasF = this.fraction;
      boolean wasW = this.wired;
      this.wired = gameTime - this.lastFed < 40L;
      this.lastUsed = (double)0.0F;
      if (rotationOk) {
         this.source = PowerSupply.Source.ROTATION;
         this.fraction = 1.0F;
      } else if (Config.electricEnabled() && this.stored > (double)0.0F) {
         double need = (double)this.watts();
         double use = Math.min(this.stored, need);
         float f = (float)(use / need);
         if (f >= 0.25F) {
            this.stored -= use;
            this.lastUsed = use;
            this.source = PowerSupply.Source.ELECTRIC;
            this.fraction = f;
         } else {
            this.source = PowerSupply.Source.NONE;
            this.fraction = f;
         }
      } else {
         this.source = PowerSupply.Source.NONE;
         this.fraction = 0.0F;
      }

      return was != this.source || wasW != this.wired || Math.abs(wasF - this.fraction) > 0.04F;
   }

   public void refund() {
      if (this.lastUsed > (double)0.0F) {
         this.stored = Math.min(this.bank(), this.stored + this.lastUsed);
         this.lastUsed = (double)0.0F;
      }

   }

   public double electricFactor() {
      return (double)0.5F + (double)0.5F * (double)Math.min(1.0F, this.fraction);
   }

   public void tooltip(List<Component> tip, boolean wantsPower, boolean overstressed, float rpm) {
      boolean elec = Config.electricEnabled();
      switch (this.source.ordinal()) {
         case 0:
            if (!wantsPower) {
               return;
            }

            if (overstressed) {
               tip.add(Component.literal("    overstressed").withStyle(ChatFormatting.RED));
            } else {
               tip.add(Component.literal(elec ? "    needs " + String.valueOf(Config.MIN_RPM.get()) + " RPM or " + this.watts() + " W at " + this.ratedVolts() + " V" : "    needs " + String.valueOf(Config.MIN_RPM.get()) + " RPM").withStyle(ChatFormatting.GOLD));
            }

            if (elec && this.wired && this.fraction > 0.0F) {
               tip.add(Component.literal("    grid too weak: needs " + this.ratedVolts() + " V").withStyle(ChatFormatting.GRAY));
            }
            break;
         case 1:
            tip.add(Component.literal("    ⟳ rotation, " + Math.round(rpm) + " RPM").withStyle(ChatFormatting.YELLOW));
            break;
         case 2:
            boolean full = this.fraction >= 0.99F;
            int var10001 = this.watts();
            tip.add(Component.literal("    ⚡ electric, " + var10001 + " W" + (full ? "" : " (weak: " + Math.round(this.fraction * 100.0F) + "%)")).withStyle(full ? ChatFormatting.AQUA : ChatFormatting.GOLD));
            if (!full) {
               tip.add(Component.literal("    the grid needs " + this.ratedVolts() + " V").withStyle(ChatFormatting.GRAY));
            }
      }

   }

   public String summary() {
      String var10000;
      switch (this.source.ordinal()) {
         case 0 -> var10000 = "unpowered";
         case 1 -> var10000 = "rotation";
         case 2 -> var10000 = "electric " + this.watts() + " W" + (this.fraction < 0.99F ? " (" + Math.round(this.fraction * 100.0F) + "%)" : "");
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public void save(CompoundTag tag) {
      tag.putDouble("bankJ", this.stored);
      tag.putByte("psrc", (byte)this.source.ordinal());
      tag.putFloat("pfrac", this.fraction);
      tag.putBoolean("pwired", this.wired);
   }

   public void load(CompoundTag tag) {
      this.stored = tag.getDouble("bankJ");
      int s = tag.getByte("psrc");
      this.source = s >= 0 && s < PowerSupply.Source.values().length ? PowerSupply.Source.values()[s] : PowerSupply.Source.NONE;
      this.fraction = tag.getFloat("pfrac");
      this.wired = tag.getBoolean("pwired");
   }

   public static PowerSupply at(Level lv, BlockPos pos) {
      BlockState st = lv.getBlockState(pos);
      Block var5 = st.getBlock();
      BlockPos var10000;
      if (var5 instanceof StructureBlock sb) {
         var10000 = sb.corePos(st, pos);
      } else {
         var10000 = pos;
      }

      BlockPos p = var10000;
      BlockEntity be = lv.getBlockEntity(p);
      if (be instanceof KineticTunableBE k) {
         if (!(be instanceof CreativeTransmitterBE)) {
            return k.power;
         }
      }

      if (be instanceof JammerBE j) {
         return j.power;
      } else if (be instanceof DimensionalRelayBE d) {
         return d.power;
      } else {
         return null;
      }
   }

   public static enum Source {
      NONE,
      ROTATION,
      ELECTRIC;

      // $FF: synthetic method
      private static Source[] $values() {
         return new Source[]{NONE, ROTATION, ELECTRIC};
      }
   }
}
