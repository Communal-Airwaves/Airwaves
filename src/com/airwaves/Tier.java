package com.airwaves;

public enum Tier {
   SMALL(1, "small"),
   MEDIUM(2, "medium"),
   LARGE(3, "large"),
   GIANT(4, "giant");

   public final int level;
   public final String id;

   private Tier(int level, String id) {
      this.level = level;
      this.id = id;
   }

   public static Tier of(int level) {
      return values()[Math.max(1, Math.min(4, level)) - 1];
   }

   // $FF: synthetic method
   private static Tier[] $values() {
      return new Tier[]{SMALL, MEDIUM, LARGE, GIANT};
   }
}
