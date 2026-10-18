package com.airwaves.power;

import com.airwaves.kinetic.KineticDeviceBlock;
import com.airwaves.structure.StructureBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Terminals {
   public static final int POSITIVE = 0;
   public static final int NEGATIVE = 1;
   public static final double[][] DEFAULT = new double[][]{{(double)11.0F, (double)1.0F, (double)0.0F, (double)14.0F, (double)4.0F, (double)1.0F}, {(double)2.0F, (double)1.0F, (double)0.0F, (double)5.0F, (double)4.0F, (double)1.0F}};

   private Terminals() {
   }

   public static double[][] spec(BlockState st) {
      Block var2 = st.getBlock();
      double[][] var10000;
      if (var2 instanceof KineticDeviceBlock<?> k) {
         var10000 = k.terminals();
      } else {
         var10000 = DEFAULT;
      }

      return var10000;
   }

   public static Direction facing(BlockState st) {
      return st.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING) ? (Direction)st.getValue(HorizontalKineticBlock.HORIZONTAL_FACING) : Direction.NORTH;
   }

   public static boolean has(BlockState st) {
      return TowerTerminals.isTerminal(st) || st.getBlock() instanceof KineticDeviceBlock && st.hasProperty(HorizontalKineticBlock.HORIZONTAL_FACING);
   }

   public static AABB box(BlockState st, int i) {
      if (st.getBlock() instanceof StructureBlock) {
         return TowerTerminals.box(st, i);
      } else {
         double[] b = spec(st)[i];
         Direction f = facing(st);
         return new AABB(rotate(new Vec3(b[0] / (double)16.0F, b[1] / (double)16.0F, b[2] / (double)16.0F), f), rotate(new Vec3(b[3] / (double)16.0F, b[4] / (double)16.0F, b[5] / (double)16.0F), f));
      }
   }

   public static Vec3 point(BlockState st, int i) {
      if (st.getBlock() instanceof StructureBlock) {
         return TowerTerminals.point(st, i);
      } else {
         double[] b = spec(st)[i];
         return rotate(new Vec3((b[0] + b[3]) / (double)32.0F, (b[1] + b[4]) / (double)32.0F, (b[2] - (double)1.0F) / (double)16.0F), facing(st));
      }
   }

   public static int at(BlockState st, Vec3 local) {
      for(int i = 0; i < 2; ++i) {
         if (box(st, i).inflate(0.03).contains(local)) {
            return i;
         }
      }

      return -1;
   }

   private static Vec3 rotate(Vec3 v, Direction f) {
      Vec3 var10000;
      switch (f) {
         case EAST -> var10000 = new Vec3((double)1.0F - v.z, v.y, v.x);
         case SOUTH -> var10000 = new Vec3((double)1.0F - v.x, v.y, (double)1.0F - v.z);
         case WEST -> var10000 = new Vec3(v.z, v.y, (double)1.0F - v.x);
         default -> var10000 = v;
      }

      return var10000;
   }
}
