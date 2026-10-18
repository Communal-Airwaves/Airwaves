package com.airwaves.logic;

import com.airwaves.be.CreativeAntennaBE;
import com.airwaves.be.YagiAntennaBE;
import com.airwaves.block.AntennaBlock;
import com.airwaves.block.CreativeAntennaBlock;
import com.airwaves.block.YagiAntennaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class AntennaUtil {
   public static final int MIN_TX_HEIGHT = 4;
   private static final int[] CAPS = new int[]{20, 40, 64, 96};
   private static final double[] RANGE_MULT = new double[]{(double)1.0F, 1.3, 1.7, 2.3};
   private static final float[] QUALITY_BONUS = new float[]{0.0F, 0.08F, 0.15F, 0.22F};
   public static final String[] TX_NAMES = new String[]{"Low-Power", "Mid-Power", "High-Power", "Max-Power"};
   public static final String[] RX_NAMES = new String[]{"Basic", "Sensitive", "High-Gain", "Max-Gain"};
   public static final String[] TIER_NAMES;
   public static final double FORWARD = 1.9;
   public static final double SIDE = (double)0.0F;
   public static final double BACK = (double)0.0F;
   public static final double BEAM_EDGE = (double)0.5F;

   private AntennaUtil() {
   }

   public static int cap(int tier) {
      return CAPS[Math.max(1, Math.min(4, tier)) - 1];
   }

   public static Info scan(Level l, BlockPos base, int wantTier) {
      if (l.isLoaded(base) && l.getBlockState(base).getBlock() instanceof CreativeAntennaBlock) {
         Info var10000 = new Info;
         int var10002 = cap(wantTier);
         int var10003 = Math.max(1, Math.min(4, wantTier));
         BlockEntity var6 = l.getBlockEntity(base);
         double var10004;
         if (var6 instanceof CreativeAntennaBE) {
            CreativeAntennaBE ca = (CreativeAntennaBE)var6;
            var10004 = (double)ca.range();
         } else {
            var10004 = (double)1000.0F;
         }

         var10000.<init>(var10002, var10003, var10004);
         return var10000;
      } else {
         Info i = scan(l, base);
         if (i.height() <= 0) {
            return AntennaUtil.Info.NONE;
         } else {
            int t = Math.max(1, Math.min(4, wantTier));
            return new Info(Math.min(i.height(), cap(t)), t, (double)-1.0F, i.beam());
         }
      }
   }

   public static Info scan(Level l, BlockPos base) {
      if (l.isLoaded(base)) {
         Block var3 = l.getBlockState(base).getBlock();
         if (var3 instanceof AntennaBlock) {
            AntennaBlock ab = (AntennaBlock)var3;
            int var6 = ab.maxHeight();
            int h = 0;
            BlockPos.MutableBlockPos p = ab.column(base, l.getBlockState(base)).mutable();

            while(h < var6 && l.isLoaded(p) && l.getBlockState(p).getBlock() instanceof AntennaBlock) {
               ++h;
               p.move(Direction.UP);
            }

            return new Info(h, ab.tier(), (double)-1.0F, yagiAbove(l, p, ab.size()));
         }
      }

      return AntennaUtil.Info.NONE;
   }

   public static Float yagiAbove(Level l, BlockPos at, int size) {
      return l.isLoaded(at) && YagiAntennaBlock.isMount(l.getBlockState(at)) ? yagi(l, at.above()) : null;
   }

   public static double range(int h, int tier, boolean fm) {
      if (h > 0 && tier > 0) {
         double r = h <= 20 ? (double)600.0F * Math.pow((double)h / (double)4.0F, 1.13) : (double)600.0F * Math.pow((double)5.0F, 1.13) * Math.pow((double)h / (double)20.0F, (double)0.75F);
         r *= RANGE_MULT[tier - 1];
         return fm ? r : r * 1.3;
      } else {
         return (double)0.0F;
      }
   }

   public static double range(Info a, boolean fm) {
      return range(a.height(), a.tier(), fm);
   }

   public static double range(int h, boolean fm) {
      return range(h, 1, fm);
   }

   public static float quality(Info a) {
      return a.height() <= 0 ? 0.0F : Math.min(1.0F, 0.5F + 0.5F * Math.min(1.0F, (float)a.height() / 20.0F) + QUALITY_BONUS[a.tier() - 1]);
   }

   public static float quality(int h) {
      return quality(new Info(h, 1));
   }

   public static double rxGain(Info a) {
      return a.height() <= 0 ? (double)1.0F : ((double)1.0F + 0.12 * (double)Math.min(a.height(), 30)) * ((double)1.0F + 0.2 * (double)(a.tier() - 1));
   }

   public static Float yagi(Level l, BlockPos at) {
      if (!l.isLoaded(at)) {
         return null;
      } else {
         BlockEntity var3 = l.getBlockEntity(at);
         Float var10000;
         if (var3 instanceof YagiAntennaBE) {
            YagiAntennaBE y = (YagiAntennaBE)var3;
            var10000 = (float)y.bearing();
         } else {
            var10000 = null;
         }

         return var10000;
      }
   }

   public static double bx(float deg) {
      return Math.sin(Math.toRadians((double)deg));
   }

   public static double bz(float deg) {
      return -Math.cos(Math.toRadians((double)deg));
   }

   public static String compass(float deg) {
      String[] n = new String[]{"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};
      return n[Math.floorMod(Math.round(deg / 45.0F), 8)];
   }

   public static double pattern(Float beam, double dx, double dz) {
      double d = Math.sqrt(dx * dx + dz * dz);
      if (d < 1.0E-6) {
         return (double)1.0F;
      } else {
         double cos = (dx * bx(beam) + dz * bz(beam)) / d;
         if (cos <= (double)0.5F) {
            return (double)0.0F;
         } else {
            double k = (cos - (double)0.5F) / (double)0.5F;
            return 1.9 * k * Math.sqrt(k);
         }
      }
   }

   public static String tierName(int tier) {
      return tier <= 0 ? "none" : TIER_NAMES[tier - 1];
   }

   static {
      TIER_NAMES = TX_NAMES;
   }

   public static record Info(int height, int tier, double fixedRange, Float beam) {
      public static final Info NONE = new Info(0, 0);

      public Info(int height, int tier) {
         this(height, tier, (double)-1.0F, (Float)null);
      }

      public Info(int height, int tier, double fixedRange) {
         this(height, tier, fixedRange, (Float)null);
      }

      public boolean creative() {
         return this.fixedRange >= (double)0.0F;
      }
   }
}
