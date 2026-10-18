package com.airwaves.block;

import com.airwaves.be.CircuitWorkbenchBE;
import com.airwaves.menu.CircuitWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CircuitWorkbenchBlock extends Block implements EntityBlock {
   public static final DirectionProperty FACING;
   private static final VoxelShape SHAPE;

   public CircuitWorkbenchBlock(BlockBehaviour.Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      b.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext c) {
      return (BlockState)this.defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return SHAPE;
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      if (!lv.isClientSide) {
         pl.openMenu(st.getMenuProvider(lv, pos));
      }

      return InteractionResult.sidedSuccess(lv.isClientSide);
   }

   protected MenuProvider getMenuProvider(BlockState st, Level lv, BlockPos pos) {
      BlockEntity var6 = lv.getBlockEntity(pos);
      CircuitWorkbenchBE var10000;
      if (var6 instanceof CircuitWorkbenchBE b) {
         var10000 = b;
      } else {
         var10000 = null;
      }

      CircuitWorkbenchBE be = var10000;
      return new SimpleMenuProvider((id, inv, p) -> new CircuitWorkbenchMenu(id, inv, ContainerLevelAccess.create(lv, pos), be), Component.translatable("container.airwaves.circuit_workbench"));
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState st) {
      return new CircuitWorkbenchBE(pos, st);
   }

   protected void onRemove(BlockState st, Level lv, BlockPos pos, BlockState now, boolean moved) {
      if (!st.is(now.getBlock())) {
         BlockEntity var7 = lv.getBlockEntity(pos);
         if (var7 instanceof CircuitWorkbenchBE) {
            CircuitWorkbenchBE be = (CircuitWorkbenchBE)var7;
            Containers.dropContents(lv, pos, be.items);
         }
      }

      super.onRemove(st, lv, pos, now, moved);
   }

   static {
      FACING = HorizontalDirectionalBlock.FACING;
      SHAPE = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)15.0F, (double)16.0F);
   }
}
