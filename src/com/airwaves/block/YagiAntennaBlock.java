package com.airwaves.block;

import com.airwaves.ModRegistry;
import com.airwaves.be.YagiAntennaBE;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class YagiAntennaBlock extends Block implements IBE<YagiAntennaBE> {
   public static final TagKey<Block> MOUNTS;
   private static final VoxelShape SHAPE;

   public YagiAntennaBlock(BlockBehaviour.Properties p) {
      super(p);
   }

   public static boolean isMount(BlockState s) {
      return s.is(MOUNTS);
   }

   protected boolean canSurvive(BlockState s, LevelReader lv, BlockPos pos) {
      return isMount(lv.getBlockState(pos.below())) && lv.getBlockState(pos.below(2)).getBlock() instanceof AntennaBlock;
   }

   public @Nullable BlockState getStateForPlacement(BlockPlaceContext c) {
      BlockState s = this.defaultBlockState();
      return this.canSurvive(s, c.getLevel(), c.getClickedPos()) ? s : null;
   }

   protected BlockState updateShape(BlockState s, Direction d, BlockState n, LevelAccessor l, BlockPos p, BlockPos np) {
      return d == Direction.DOWN && !this.canSurvive(s, l, p) ? Blocks.AIR.defaultBlockState() : super.updateShape(s, d, n, l, p, np);
   }

   public void setPlacedBy(Level lv, BlockPos pos, BlockState s, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(lv, pos, s, placer, stack);
      if (placer != null) {
         this.withBlockEntityDo(lv, pos, (be) -> be.setBearing(Math.round(placer.getYRot() + 180.0F) % 360));
      }

   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return SHAPE;
   }

   public Class<YagiAntennaBE> getBlockEntityClass() {
      return YagiAntennaBE.class;
   }

   public BlockEntityType<? extends YagiAntennaBE> getBlockEntityType() {
      return (BlockEntityType)ModRegistry.YAGI_BE.get();
   }

   static {
      MOUNTS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("airwaves", "yagi_mounts"));
      SHAPE = Shapes.or(box((double)7.0F, (double)0.0F, (double)7.5F, (double)9.0F, (double)11.0F, (double)9.5F), box((double)0.0F, (double)7.0F, (double)0.0F, (double)16.0F, (double)12.0F, (double)16.0F));
   }
}
