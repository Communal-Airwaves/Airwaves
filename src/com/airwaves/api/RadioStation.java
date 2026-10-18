package com.airwaves.api;

import com.airwaves.Tuning;
import com.airwaves.logic.Emitter;
import net.minecraft.core.BlockPos;

public record RadioStation(long id, BlockPos pos, double antennaTop, Tuning tuning, double range, float quality, String name) {
   public static final double CREATIVE_RANGE = (double)1.0E8F;

   public RadioStation(long id, BlockPos pos, double antennaTop, Tuning tuning, double range, float quality, String name) {
      tuning = tuning.clamped();
      quality = Math.max(0.0F, Math.min(1.0F, quality));
      range = Math.max((double)0.0F, range);
      name = name == null ? "" : name;
      this.id = id;
      this.pos = pos;
      this.antennaTop = antennaTop;
      this.tuning = tuning;
      this.range = range;
      this.quality = quality;
      this.name = name;
   }

   public Emitter toEmitter() {
      return new Emitter(this.id, this.pos, this.antennaTop, this.tuning, this.range, this.quality, this.name);
   }

   public static RadioStation of(Emitter e) {
      return new RadioStation(e.root(), e.pos(), e.topY(), e.tuning(), e.range(), e.baseQ(), e.name());
   }
}
