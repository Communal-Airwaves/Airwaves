package com.airwaves.block;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MastBlock extends Block {
   public static final BooleanProperty TOP = BooleanProperty.create("top");
   public static final BooleanProperty NARROW = BooleanProperty.create("narrow");
   public static final IntegerProperty SEG = IntegerProperty.create("seg", 0, 3);
   public static final IntegerProperty CX2 = IntegerProperty.create("cx", 0, 1);
   public static final IntegerProperty CZ2 = IntegerProperty.create("cz", 0, 1);
   public static final IntegerProperty CX4 = IntegerProperty.create("cx", 0, 3);
   public static final IntegerProperty CZ4 = IntegerProperty.create("cz", 0, 3);
   public static final IntegerProperty CX8 = IntegerProperty.create("cx", 0, 7);
   public static final IntegerProperty CZ8 = IntegerProperty.create("cz", 0, 7);
   private static final Map<Long, VoxelShape> SHAPES = new HashMap();

   protected MastBlock(BlockBehaviour.Properties p) {
      super(p);
      BlockState d = (BlockState)((BlockState)this.stateDefinition.any()).setValue(TOP, true);
      if (d.hasProperty(NARROW)) {
         d = (BlockState)d.setValue(NARROW, false);
      }

      this.registerDefaultState(d);
   }

   public abstract int size();

   public abstract int tier();

   public abstract int maxHeight();

   public IntegerProperty px() {
      IntegerProperty var10000;
      switch (this.size()) {
         case 2 -> var10000 = CX2;
         case 4 -> var10000 = CX4;
         case 8 -> var10000 = CX8;
         default -> var10000 = null;
      }

      return var10000;
   }

   public IntegerProperty pz() {
      IntegerProperty var10000;
      switch (this.size()) {
         case 2 -> var10000 = CZ2;
         case 4 -> var10000 = CZ4;
         case 8 -> var10000 = CZ8;
         default -> var10000 = null;
      }

      return var10000;
   }

   protected boolean segAlways() {
      return false;
   }

   public int cx(BlockState s) {
      return this.size() > 1 ? (Integer)s.getValue(this.px()) : 0;
   }

   public int cz(BlockState s) {
      return this.size() > 1 ? (Integer)s.getValue(this.pz()) : 0;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(new Property[]{TOP});
      if (this.size() >= 4) {
         b.add(new Property[]{NARROW});
      }

      if (this.px() != null) {
         b.add(new Property[]{this.px(), this.pz()});
      }

      if (this.px() != null || this.segAlways()) {
         b.add(new Property[]{SEG});
      }

   }

   public int mask(BlockState s) {
      int n = this.size();
      if (n == 1) {
         return 15;
      } else {
         int x = this.cx(s);
         int z = this.cz(s);
         int last = n - 1;
         return (z == 0 ? 1 : 0) | (x == last ? 2 : 0) | (z == last ? 4 : 0) | (x == 0 ? 8 : 0);
      }
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      boolean top = (Boolean)s.getValue(TOP) && (!s.hasProperty(NARROW) || !(Boolean)s.getValue(NARROW));
      int m = this.mask(s);
      long key = (long)this.size() << 8 | (long)m << 1 | (long)(top ? 1 : 0);
      return (VoxelShape)SHAPES.computeIfAbsent(key, (k) -> this.build(m, top));
   }

   private VoxelShape build(int m, boolean top) {
      if (this.size() == 1) {
         VoxelShape v = Block.box((double)3.5F, (double)0.0F, (double)3.5F, (double)12.5F, top ? (double)9.0F : (double)16.0F, (double)12.5F);
         return top ? Shapes.or(v, Block.box((double)5.0F, (double)9.0F, (double)5.0F, (double)11.0F, (double)15.0F, (double)11.0F)) : v;
      } else {
         double h = top ? (double)9.0F : (double)16.0F;
         VoxelShape v = Shapes.empty();
         if ((m & 1) != 0) {
            v = Shapes.or(v, Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, h, top ? (double)5.0F : (double)1.5F));
         }

         if ((m & 2) != 0) {
            v = Shapes.or(v, Block.box(top ? (double)11.0F : (double)14.5F, (double)0.0F, (double)0.0F, (double)16.0F, h, (double)16.0F));
         }

         if ((m & 4) != 0) {
            v = Shapes.or(v, Block.box((double)0.0F, (double)0.0F, top ? (double)11.0F : (double)14.5F, (double)16.0F, h, (double)16.0F));
         }

         if ((m & 8) != 0) {
            v = Shapes.or(v, Block.box((double)0.0F, (double)0.0F, (double)0.0F, top ? (double)5.0F : (double)1.5F, h, (double)16.0F));
         }

         return v;
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)this.defaultBlockState().setValue(TOP, !continues(c.getLevel().getBlockState(c.getClickedPos().above())));
   }

   protected BlockState updateShape(BlockState s, Direction d, BlockState n, LevelAccessor l, BlockPos p, BlockPos np) {
      return d == Direction.UP ? (BlockState)s.setValue(TOP, !continues(n)) : s;
   }

   public static boolean continues(BlockState above) {
      return above.getBlock() instanceof MastBlock || above.getBlock() instanceof YagiAntennaBlock;
   }

   protected boolean propagatesSkylightDown(BlockState s, BlockGetter l, BlockPos p) {
      return true;
   }

   public int nextSeg(BlockState below) {
      return below.getBlock() instanceof MastBlock && below.hasProperty(SEG) ? ((Integer)below.getValue(SEG) + 1) % 4 : 0;
   }

   public static void setNarrow(Level lv, BlockPos origin, boolean v) {
      BlockState o = lv.getBlockState(origin);
      Block var5 = o.getBlock();
      if (var5 instanceof MastBlock mb) {
         if (o.hasProperty(NARROW)) {
            for(int dx = 0; dx < mb.size(); ++dx) {
               for(int dz = 0; dz < mb.size(); ++dz) {
                  BlockPos q = origin.offset(dx, 0, dz);
                  BlockState s = lv.getBlockState(q);
                  if (s.is(mb) && (Boolean)s.getValue(NARROW) != v) {
                     lv.setBlock(q, (BlockState)s.setValue(NARROW, v), 2);
                  }
               }
            }

            return;
         }
      }

   }

   protected void onRemove(BlockState s, Level lv, BlockPos p, BlockState ns, boolean moved) {
      if (!ns.is(this) && !lv.isClientSide) {
         BlockState below = lv.getBlockState(p.below());
         Block var8 = below.getBlock();
         if (var8 instanceof MastBlock) {
            MastBlock wide = (MastBlock)var8;
            if (wide.size() > this.size()) {
               setNarrow(lv, wide.origin(p.below(), below), false);
            }
         }
      }

      super.onRemove(s, lv, p, ns, moved);
   }

   public BlockPos origin(BlockPos pos, BlockState st) {
      return pos.offset(-this.cx(st), 0, -this.cz(st));
   }

   protected ItemStack layerDrop(Level lv, BlockPos origin, BlockState st) {
      return new ItemStack(this.asItem());
   }

   protected boolean customDrop() {
      return this.size() > 1;
   }

   public BlockState playerWillDestroy(Level lv, BlockPos pos, BlockState st, Player pl) {
      if (!lv.isClientSide && this.customDrop()) {
         int n = this.size();
         BlockPos o = this.origin(pos, st);
         ItemStack drop = this.layerDrop(lv, o, lv.getBlockState(o).is(this) ? lv.getBlockState(o) : st);

         for(int dx = 0; dx < n; ++dx) {
            for(int dz = 0; dz < n; ++dz) {
               BlockPos q = o.offset(dx, 0, dz);
               if (!q.equals(pos) && lv.getBlockState(q).is(this)) {
                  lv.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
               }
            }
         }

         if (!pl.isCreative()) {
            Block.popResource(lv, pos, drop);
         }
      }

      return super.playerWillDestroy(lv, pos, st, pl);
   }
}
