package com.airwaves.power;

import com.airwaves.Config;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ElectricLoad {
   public static final double OPEN = (double)100000.0F;
   private double resistance = (double)100000.0F;
   public float volts;
   public float watts;
   public Status status;

   public ElectricLoad() {
      this.status = ElectricLoad.Status.IDLE;
   }

   public double resistance() {
      return this.resistance;
   }

   public void update(PowerSupply s, double v, double joules, long gameTime) {
      this.volts = (float)Math.abs(v);
      this.watts = (float)(joules * (double)20.0F);
      double r = (double)100000.0F;
      if (!Config.electricEnabled()) {
         this.status = ElectricLoad.Status.DISABLED;
      } else if (s == null) {
         this.status = ElectricLoad.Status.NO_MACHINE;
      } else {
         if (joules > (double)0.0F) {
            s.feed(joules, gameTime);
         } else {
            s.feed((double)0.0F, gameTime);
         }

         double want = s.wantWatts();
         int rated = s.ratedVolts();
         if ((double)this.volts > (double)rated * 2.2) {
            this.status = ElectricLoad.Status.OVERVOLTAGE;
         } else if (want <= (double)0.0F) {
            this.status = ElectricLoad.Status.IDLE;
         } else {
            double vv = (double)Math.max(this.volts, (float)rated);
            r = Mth.clamp(vv * vv / want, 0.05, (double)100000.0F);
            this.status = ElectricLoad.Status.DRAWING;
         }
      }

      this.resistance = r;
   }

   public void tooltip(List<Component> tip, PowerSupply s) {
      if (!(this.volts < 0.05F) || this.status == ElectricLoad.Status.OVERVOLTAGE) {
         String var10001 = String.format("%.1f", this.volts);
         tip.add(Component.literal("    ⚡ " + var10001 + " V, " + String.format("%.1f", this.watts) + " W").withStyle(ChatFormatting.AQUA));
         if (this.status == ElectricLoad.Status.OVERVOLTAGE && s != null) {
            double var3 = (double)s.ratedVolts();
            tip.add(Component.literal("    too much voltage: max " + Math.round(var3 * 2.2) + " V").withStyle(ChatFormatting.RED));
         }

      }
   }

   public static enum Status {
      DRAWING,
      NO_MACHINE,
      OVERVOLTAGE,
      IDLE,
      DISABLED;

      // $FF: synthetic method
      private static Status[] $values() {
         return new Status[]{DRAWING, NO_MACHINE, OVERVOLTAGE, IDLE, DISABLED};
      }
   }
}
