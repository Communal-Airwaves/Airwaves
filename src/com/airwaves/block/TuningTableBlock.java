package com.airwaves.block;

import com.airwaves.item.Tunable;
import com.airwaves.menu.TuningTableMenu;
import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TuningTableBlock extends HorizontalDirectionalBlock {
   public static final MapCodec<TuningTableBlock> CODEC = simpleCodec(TuningTableBlock::new);
   public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
   private static final VoxelShape SHAPE = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)15.0F, (double)16.0F);

   public TuningTableBlock(BlockBehaviour.Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(PART, TuningTableBlock.Part.CENTER));
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(new Property[]{FACING, PART});
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return SHAPE;
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      Direction f = c.getHorizontalDirection().getOpposite();
      BlockPos p = c.getClickedPos();
      Level l = c.getLevel();
      return l.getBlockState(p.relative(f.getClockWise())).canBeReplaced(c) && l.getBlockState(p.relative(f.getCounterClockWise())).canBeReplaced(c) ? (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, f)).setValue(PART, TuningTableBlock.Part.CENTER) : null;
   }

   public void setPlacedBy(Level l, BlockPos p, BlockState s, LivingEntity who, ItemStack st) {
      if (!l.isClientSide) {
         Direction f = (Direction)s.getValue(FACING);
         l.setBlock(p.relative(f.getClockWise()), (BlockState)s.setValue(PART, TuningTableBlock.Part.LEFT), 3);
         l.setBlock(p.relative(f.getCounterClockWise()), (BlockState)s.setValue(PART, TuningTableBlock.Part.RIGHT), 3);
      }
   }

   protected BlockState updateShape(BlockState s, Direction d, BlockState n, LevelAccessor l, BlockPos p, BlockPos np) {
      Direction f = (Direction)s.getValue(FACING);
      Part part = (Part)s.getValue(PART);
      boolean nIsCenter = n.is(this) && n.getValue(PART) == TuningTableBlock.Part.CENTER;
      boolean nIsSide = n.is(this) && n.getValue(PART) != TuningTableBlock.Part.CENTER;
      if (part == TuningTableBlock.Part.CENTER) {
         if ((d == f.getClockWise() || d == f.getCounterClockWise()) && !nIsSide) {
            return Blocks.AIR.defaultBlockState();
         }
      } else {
         Direction toCenter = part == TuningTableBlock.Part.LEFT ? f.getCounterClockWise() : f.getClockWise();
         if (d == toCenter && !nIsCenter) {
            return Blocks.AIR.defaultBlockState();
         }
      }

      return s;
   }

   public BlockState playerWillDestroy(Level l, BlockPos p, BlockState s, Player pl) {
      Part part = (Part)s.getValue(PART);
      if (!l.isClientSide && !pl.isCreative() && part != TuningTableBlock.Part.CENTER) {
         Direction f = (Direction)s.getValue(FACING);
         l.destroyBlock(part == TuningTableBlock.Part.LEFT ? p.relative(f.getCounterClockWise()) : p.relative(f.getClockWise()), true, pl);
      }

      return super.playerWillDestroy(l, p, s, pl);
   }

   protected InteractionResult useWithoutItem(BlockState s, Level l, BlockPos p, Player pl, BlockHitResult h) {
      if (!pl.getMainHandItem().isEmpty() && !(pl.getMainHandItem().getItem() instanceof Tunable)) {
         return InteractionResult.PASS;
      } else {
         if (!l.isClientSide) {
            MenuProvider mp = new SimpleMenuProvider((id, inv, pp) -> new TuningTableMenu(id, inv, ContainerLevelAccess.create(l, p)), Component.translatable("container.airwaves.tuning_table"));
            pl.openMenu(mp);
         }

         return InteractionResult.sidedSuccess(l.isClientSide);
      }
   }

   public static enum Part implements StringRepresentable {
      LEFT,
      CENTER,
      RIGHT;

      public String getSerializedName() {
         return this.name().toLowerCase(Locale.ROOT);
      }

      // $FF: synthetic method
      private static Part[] $values() {
         return new Part[]{LEFT, CENTER, RIGHT};
      }
   }
}
