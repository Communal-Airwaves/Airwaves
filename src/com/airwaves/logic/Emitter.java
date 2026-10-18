package com.airwaves.logic;

import com.airwaves.Tuning;
import net.minecraft.core.BlockPos;

public record Emitter(long root, BlockPos pos, double topY, Tuning tuning, double range, float baseQ, String name, Float beam) {
   public static final double CREATIVE = (double)1.0E8F;

   public Emitter(long root, BlockPos pos, double topY, Tuning tuning, double range, float baseQ, String name) {
      this(root, pos, topY, tuning, range, baseQ, name, (Float)null);
   }

   public double rangeTo(BlockPos at) {
      return this.beam == null ? this.range : this.range * AntennaUtil.pattern(this.beam, (double)(at.getX() - this.pos.getX()), (double)(at.getZ() - this.pos.getZ()));
   }

   public boolean creative() {
      return this.range >= (double)1.0E8F;
   }
}
