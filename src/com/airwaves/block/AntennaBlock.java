package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AntennaBlock extends MastBlock {
   protected AntennaBlock(BlockBehaviour.Properties p) {
      super(p);
   }

   public abstract int[][] profile();

   public int maxHeight() {
      int h = 0;

      for(int[] s : this.profile()) {
         h += s[1];
      }

      return h;
   }

   public int widthAt(int h) {
      for(int[] s : this.profile()) {
         if (h < s[1]) {
            return s[0];
         }

         h -= s[1];
      }

      return 0;
   }

   public static AntennaBlock forWidth(int w) {
      Object var10000;
      switch (w) {
         case 2 -> var10000 = (Medium)ModRegistry.ANTENNA_MEDIUM.get();
         case 4 -> var10000 = (Big)ModRegistry.ANTENNA_BIG.get();
         case 8 -> var10000 = (Giant)ModRegistry.ANTENNA_GIANT.get();
         default -> var10000 = (Small)ModRegistry.ANTENNA.get();
      }

      return (AntennaBlock)var10000;
   }

   public BlockPos column(BlockPos pos, BlockState st) {
      return this.origin(pos, st).offset(this.size() / 2, 0, this.size() / 2);
   }

   public static Object[] base(BlockGetter lv, BlockPos pos) {
      BlockState st = lv.getBlockState(pos);
      Block var4 = st.getBlock();
      if (!(var4 instanceof AntennaBlock a)) {
         return null;
      } else {
         BlockPos.MutableBlockPos var9 = a.column(pos, st).mutable();
         AntennaBlock base = a;
         int n = 0;

         while(n < 512) {
            Block var8 = lv.getBlockState(var9).getBlock();
            if (!(var8 instanceof AntennaBlock)) {
               break;
            }

            AntennaBlock b = (AntennaBlock)var8;
            base = b;
            ++n;
            var9.move(Direction.DOWN);
         }

         return new Object[]{base, n};
      }
   }

   protected ItemStack layerDrop(Level lv, BlockPos origin, BlockState st) {
      Object[] b = base(lv, origin.offset(this.size() / 2, 0, this.size() / 2));
      return new ItemStack(b == null ? this.asItem() : ((AntennaBlock)b[0]).asItem());
   }

   public static class Small extends AntennaBlock {
      public static final MapCodec<Small> CODEC = simpleCodec(Small::new);

      public Small(BlockBehaviour.Properties p) {
         super(p);
      }

      protected MapCodec<Small> codec() {
         return CODEC;
      }

      public int size() {
         return 1;
      }

      public int tier() {
         return 1;
      }

      public int[][] profile() {
         return new int[][]{{1, 20}};
      }
   }

   public static class Medium extends AntennaBlock {
      public static final MapCodec<Medium> CODEC = simpleCodec(Medium::new);

      public Medium(BlockBehaviour.Properties p) {
         super(p);
      }

      protected MapCodec<Medium> codec() {
         return CODEC;
      }

      public int size() {
         return 2;
      }

      public int tier() {
         return 2;
      }

      public int[][] profile() {
         return new int[][]{{2, 40}};
      }
   }

   public static class Big extends AntennaBlock {
      public static final MapCodec<Big> CODEC = simpleCodec(Big::new);

      public Big(BlockBehaviour.Properties p) {
         super(p);
      }

      protected MapCodec<Big> codec() {
         return CODEC;
      }

      public int size() {
         return 4;
      }

      public int tier() {
         return 3;
      }

      public int[][] profile() {
         return new int[][]{{4, 32}, {2, 32}};
      }
   }

   public static class Giant extends AntennaBlock {
      public static final MapCodec<Giant> CODEC = simpleCodec(Giant::new);

      public Giant(BlockBehaviour.Properties p) {
         super(p);
      }

      protected MapCodec<Giant> codec() {
         return CODEC;
      }

      public int size() {
         return 8;
      }

      public int tier() {
         return 4;
      }

      public int[][] profile() {
         return new int[][]{{8, 32}, {4, 32}, {2, 32}};
      }
   }
}
