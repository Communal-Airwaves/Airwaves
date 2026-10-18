package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.placement.PoleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AntennaBlock extends MastBlock implements Fallable {
   public static final int PLACEMENT_HELPER = PlacementHelpers.register(new ExtendHelper());
   private static final int FALL_DELAY = 2;

   protected AntennaBlock(BlockBehaviour.Properties p) {
      super(p);
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (!player.isShiftKeyDown() && player.mayBuild()) {
         IPlacementHelper helper = PlacementHelpers.get(PLACEMENT_HELPER);
         if (helper.matchesItem(stack)) {
            Item var10 = stack.getItem();
            if (var10 instanceof BlockItem) {
               BlockItem bi = (BlockItem)var10;
               return helper.getOffset(player, level, state, pos, hit).placeInWorld(level, bi, player, hand, hit);
            }
         }

         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   private static boolean supported(LevelAccessor lv, BlockPos pos) {
      BlockState below = lv.getBlockState(pos.below());
      return below.getBlock() instanceof AntennaBlock || !below.canBeReplaced();
   }

   protected BlockState updateShape(BlockState s, Direction d, BlockState n, LevelAccessor l, BlockPos p, BlockPos np) {
      BlockState out = super.updateShape(s, d, n, l, p, np);
      if (d == Direction.DOWN && !supported(l, p)) {
         l.scheduleTick(p, this, 2);
      }

      return out;
   }

   protected void tick(BlockState s, ServerLevel lv, BlockPos pos, RandomSource r) {
      if (!supported(lv, pos)) {
         List<BlockPos> column = new ArrayList();
         BlockPos.MutableBlockPos m = pos.mutable();

         while(column.size() < 512 && lv.getBlockState(m).getBlock() instanceof AntennaBlock) {
            column.add(m.immutable());
            m.move(Direction.UP);
         }

         for(BlockPos p : column) {
            BlockState st = lv.getBlockState(p);
            if (st.getBlock() instanceof AntennaBlock) {
               FallingBlockEntity f = FallingBlockEntity.fall(lv, p, st);
               f.setHurtsEntities(1.0F, 6);
            }
         }

      }
   }

   public void onLand(Level lv, BlockPos pos, BlockState state, BlockState replaced, FallingBlockEntity e) {
      lv.destroyBlock(pos, true);
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
      return (AntennaBlock)ModRegistry.ANTENNA.get();
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

   private static class ExtendHelper extends PoleHelper<Boolean> {
      ExtendHelper() {
         super((s) -> s.getBlock() instanceof AntennaBlock, (s) -> Axis.Y, MastBlock.TOP);
      }

      public Predicate<ItemStack> getItemPredicate() {
         return (i) -> {
            Item patt0$temp = i.getItem();
            boolean var10000;
            if (patt0$temp instanceof BlockItem b) {
               if (b.getBlock() instanceof AntennaBlock) {
                  var10000 = true;
                  return var10000;
               }
            }

            var10000 = false;
            return var10000;
         };
      }

      public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
         Block var7 = state.getBlock();
         if (var7 instanceof AntennaBlock a) {
            int var10 = this.attachedPoles(world, pos, Direction.UP);
            int below = this.attachedPoles(world, pos, Direction.DOWN);
            if (var10 + below + 1 >= a.maxHeight()) {
               return PlacementOffset.fail();
            } else {
               BlockPos np = pos.above(var10 + 1);
               return world.isInWorldBounds(np) && world.getBlockState(np).canBeReplaced() ? PlacementOffset.success(np, (s) -> (BlockState)s.setValue(MastBlock.TOP, !MastBlock.continues(world.getBlockState(np.above())))) : PlacementOffset.fail();
            }
         } else {
            return PlacementOffset.fail();
         }
      }
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
         return new int[][]{{1, 96}};
      }
   }
}
