package com.airwaves.power;

import com.airwaves.structure.StructureBlock;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TowerTerminals {
   private static final double[][] RELAY = new double[][]{{(double)4.0F, (double)6.0F, (double)14.0F, (double)6.0F, (double)9.0F, (double)16.0F}, {(double)10.0F, (double)6.0F, (double)14.0F, (double)12.0F, (double)9.0F, (double)16.0F}};
   private static final double[][] RADIO = new double[][]{{(double)4.0F, (double)8.0F, (double)7.0F, (double)6.0F, (double)11.0F, (double)9.0F}, {(double)10.0F, (double)8.0F, (double)7.0F, (double)12.0F, (double)11.0F, (double)9.0F}};

   private TowerTerminals() {
   }

   public static int part(StructureBlock b) {
      byte var10000;
      switch (b.def.name) {
         case "relay_tower_small":
         case "radio_tower":
            var10000 = 7;
            break;
         case "relay_tower_big":
            var10000 = 13;
            break;
         default:
            var10000 = -1;
      }

      return var10000;
   }

   public static boolean isTerminal(StructureBlock b, BlockState st) {
      int p = part(b);
      return p >= 0 && b.partOf(st) == p;
   }

   public static boolean isTerminal(BlockState st) {
      Block var2 = st.getBlock();
      boolean var10000;
      if (var2 instanceof StructureBlock b) {
         if (isTerminal(b, st)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   private static double[][] spec(StructureBlock b) {
      return b.def.name.equals("radio_tower") ? RADIO : RELAY;
   }

   private static boolean up(StructureBlock b) {
      return b.def.name.equals("radio_tower");
   }

   public static AABB box(BlockState st, int i) {
      StructureBlock b = (StructureBlock)st.getBlock();
      double[] s = spec(b)[i];
      Vec3 a = turn(new Vec3(s[0] / (double)16.0F, s[1] / (double)16.0F, s[2] / (double)16.0F), st);
      Vec3 c = turn(new Vec3(s[3] / (double)16.0F, s[4] / (double)16.0F, s[5] / (double)16.0F), st);
      return new AABB(Math.min(a.x, c.x), Math.min(a.y, c.y), Math.min(a.z, c.z), Math.max(a.x, c.x), Math.max(a.y, c.y), Math.max(a.z, c.z));
   }

   public static Vec3 point(BlockState st, int i) {
      StructureBlock b = (StructureBlock)st.getBlock();
      double[] s = spec(b)[i];
      Vec3 p = up(b) ? new Vec3((s[0] + s[3]) / (double)32.0F, s[4] / (double)16.0F, (s[2] + s[5]) / (double)32.0F) : new Vec3((s[0] + s[3]) / (double)32.0F, (s[1] + s[4]) / (double)32.0F, s[5] / (double)16.0F);
      return turn(p, st);
   }

   private static Vec3 turn(Vec3 v, BlockState st) {
      return st.getValue(StructureBlock.AXIS) == Axis.Z ? new Vec3((double)1.0F - v.z, v.y, v.x) : v;
   }
}
