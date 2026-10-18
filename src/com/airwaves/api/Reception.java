package com.airwaves.api;

public record Reception(float quality, long stationId, String name) {
   public static final Reception NOTHING = new Reception(0.0F, 0L, "");
   public static final float THRESHOLD = 0.02F;

   public boolean heard() {
      return this.quality >= 0.02F && this.stationId != 0L;
   }
}
