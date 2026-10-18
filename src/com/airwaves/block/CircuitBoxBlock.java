package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.Tuning;
import com.airwaves.be.CircuitBoxBE;
import com.airwaves.item.BoardItem;
import com.airwaves.item.Tunable;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CircuitBoxBlock extends BaseEntityBlock {
   public static final MapCodec<CircuitBoxBlock> CODEC = simpleCodec(CircuitBoxBlock::new);
   public static final DirectionProperty FACING;
   public static final BooleanProperty LIT;
   public static final BooleanProperty BOARD;
   private static final VoxelShape SHAPE;

   public CircuitBoxBlock(BlockBehaviour.Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(LIT, false)).setValue(BOARD, false));
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(new Property[]{FACING, LIT, BOARD});
   }

   protected RenderShape getRenderShape(BlockState s) {
      return RenderShape.MODEL;
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return SHAPE;
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)this.defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
   }

   public @Nullable BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      return new CircuitBoxBE(p, s);
   }

   public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level lv, BlockState s, BlockEntityType<T> t) {
      return lv.isClientSide ? null : createTickerHelper(t, (BlockEntityType)ModRegistry.CIRCUIT_BOX_BE.get(), (l, p, st, be) -> be.serverTick());
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState st, Level lv, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
      BlockEntity var9 = lv.getBlockEntity(pos);
      if (var9 instanceof CircuitBoxBE be) {
         if (stack.getItem() instanceof BoardItem && be.board().isEmpty()) {
            if (!lv.isClientSide) {
               be.setBoard(stack.copyWithCount(1));
               if (!pl.getAbilities().instabuild) {
                  stack.shrink(1);
               }

               lv.playSound((Player)null, pos, (SoundEvent)ModSounds.SWITCH.get(), SoundSource.BLOCKS, 0.6F, 1.4F);
            }

            return ItemInteractionResult.sidedSuccess(lv.isClientSide);
         } else {
            Item var10 = stack.getItem();
            if (var10 instanceof Tunable) {
               Tunable tn = (Tunable)var10;
               if (tn.canTune(stack) && !pl.isSecondaryUseActive()) {
                  Tuning t = (Tuning)stack.getOrDefault((DataComponentType)ModRegistry.TUNING.get(), Tuning.DEFAULT);
                  if (!t.matches(be.getTuning())) {
                     if (!lv.isClientSide) {
                        be.setTuning(t);
                        be.sync();
                        lv.playSound((Player)null, pos, (SoundEvent)ModSounds.TUNE.get(), SoundSource.BLOCKS, 0.4F, 1.8F);
                     }

                     return ItemInteractionResult.sidedSuccess(lv.isClientSide);
                  }
               }
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      if (pl.isSecondaryUseActive()) {
         BlockEntity var7 = lv.getBlockEntity(pos);
         if (var7 instanceof CircuitBoxBE) {
            CircuitBoxBE be = (CircuitBoxBE)var7;
            if (!be.board().isEmpty()) {
               if (!lv.isClientSide) {
                  ItemStack b = be.board();
                  be.setBoard(ItemStack.EMPTY);
                  if (!pl.getInventory().add(b)) {
                     Block.popResource(lv, pos.above(), b);
                  }

                  lv.playSound((Player)null, pos, (SoundEvent)ModSounds.SWITCH.get(), SoundSource.BLOCKS, 0.6F, 0.9F);
               }

               return InteractionResult.sidedSuccess(lv.isClientSide);
            }
         }
      }

      return InteractionResult.PASS;
   }

   protected boolean isSignalSource(BlockState s) {
      return true;
   }

   protected int getSignal(BlockState s, BlockGetter l, BlockPos p, Direction side) {
      int var10000;
      if (side == s.getValue(FACING)) {
         BlockEntity var6 = l.getBlockEntity(p);
         if (var6 instanceof CircuitBoxBE) {
            CircuitBoxBE be = (CircuitBoxBE)var6;
            var10000 = be.output();
            return var10000;
         }
      }

      var10000 = 0;
      return var10000;
   }

   protected int getDirectSignal(BlockState s, BlockGetter l, BlockPos p, Direction side) {
      return this.getSignal(s, l, p, side);
   }

   public boolean canConnectRedstone(BlockState s, BlockGetter l, BlockPos p, @Nullable Direction side) {
      return side != null && side.getAxis() == ((Direction)s.getValue(FACING)).getAxis();
   }

   public static void updateFront(Level lv, BlockPos pos, BlockState s) {
      Direction out = ((Direction)s.getValue(FACING)).getOpposite();
      BlockPos front = pos.relative(out);
      lv.neighborChanged(front, s.getBlock(), pos);
      lv.updateNeighborsAtExceptFromFacing(front, s.getBlock(), out.getOpposite());
   }

   protected void onRemove(BlockState s, Level lv, BlockPos pos, BlockState ns, boolean moved) {
      if (!s.is(ns.getBlock())) {
         BlockEntity var7 = lv.getBlockEntity(pos);
         if (var7 instanceof CircuitBoxBE) {
            CircuitBoxBE be = (CircuitBoxBE)var7;
            if (!be.board().isEmpty()) {
               Block.popResource(lv, pos, be.board());
            }
         }
      }

      super.onRemove(s, lv, pos, ns, moved);
      if (!s.is(ns.getBlock()) && !moved) {
         updateFront(lv, pos, s);
      }

   }

   static {
      FACING = BlockStateProperties.HORIZONTAL_FACING;
      LIT = BlockStateProperties.LIT;
      BOARD = BooleanProperty.create("board");
      SHAPE = Block.box((double)1.0F, (double)0.0F, (double)1.0F, (double)15.0F, (double)8.0F, (double)15.0F);
   }
}
