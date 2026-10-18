package com.airwaves.structure;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.Tuning;
import com.airwaves.be.CableNode;
import com.airwaves.be.Tuned;
import com.airwaves.compat.ElectricCompat;
import com.airwaves.item.CableItem;
import com.airwaves.item.Tunable;
import com.airwaves.logic.Cables;
import com.airwaves.power.TowerTerminals;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntityTicker;
import java.util.ArrayList;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StructureBlock extends BaseEntityBlock {
   public static final EnumProperty<Direction.Axis> AXIS;
   private static final ThreadLocal<Object[]> PENDING;
   public final StructureDef def;
   public final IntegerProperty part;
   private final Property<?>[] extra;
   private final BiFunction<BlockPos, BlockState, BlockEntity> beFactory;
   private ItemStack dropOverride;
   private MapCodec<StructureBlock> codec;

   public static StructureBlock create(String name, BlockBehaviour.Properties p, BiFunction<BlockPos, BlockState, BlockEntity> be, Property<?>... extra) {
      StructureDef d = StructureDef.get(name);
      PENDING.set(new Object[]{IntegerProperty.create("part", 0, d.size() - 1), extra});

      StructureBlock var5;
      try {
         var5 = new StructureBlock(d, p, be);
      } finally {
         PENDING.remove();
      }

      return var5;
   }

   public static StructureBlock createKinetic(String name, BlockBehaviour.Properties p, BiFunction<BlockPos, BlockState, BlockEntity> be, Property<?>... extra) {
      StructureDef d = StructureDef.get(name);
      PENDING.set(new Object[]{IntegerProperty.create("part", 0, d.size() - 1), extra});

      KineticStructureBlock var5;
      try {
         var5 = ElectricCompat.tower(d, p, be);
      } finally {
         PENDING.remove();
      }

      return var5;
   }

   protected StructureBlock(StructureDef def, BlockBehaviour.Properties p, BiFunction<BlockPos, BlockState, BlockEntity> be) {
      super(p);
      Object[] pend = PENDING.get();
      this.def = def;
      this.part = (IntegerProperty)pend[0];
      this.extra = (Property[])pend[1];
      this.beFactory = be;
      BlockState d = (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(this.part, 0)).setValue(AXIS, Axis.X);

      for(Property<?> p2 : this.extra) {
         if (p2 instanceof BooleanProperty bp) {
            d = (BlockState)d.setValue(bp, false);
         } else if (p2 instanceof IntegerProperty ip) {
            d = (BlockState)d.setValue(ip, (Integer)ip.getPossibleValues().iterator().next());
         }
      }

      this.registerDefaultState(d);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      if (this.codec == null) {
         this.codec = MapCodec.unit(this);
      }

      return this.codec;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
      Object[] pend = PENDING.get();
      b.add(new Property[]{(IntegerProperty)pend[0], AXIS});

      for(Property<?> p : (Property[])pend[1]) {
         b.add(new Property[]{p});
      }

   }

   public int partOf(BlockState s) {
      return (Integer)s.getValue(this.part);
   }

   public BlockPos corePos(BlockState s, BlockPos pos) {
      return this.def.corePos(pos, (Integer)s.getValue(this.part), (Direction.Axis)s.getValue(AXIS));
   }

   public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
      if ((Integer)s.getValue(this.part) == 0) {
         return this.beFactory != null ? (BlockEntity)this.beFactory.apply(p, s) : null;
      } else {
         if (TowerTerminals.isTerminal(this, s)) {
            BiFunction<BlockPos, BlockState, BlockEntity> f = ElectricCompat.towerTerminalBE();
            if (f != null) {
               return (BlockEntity)f.apply(p, s);
            }
         }

         return null;
      }
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level lv, BlockState s, BlockEntityType<T> t) {
      if ((Integer)s.getValue(this.part) != 0 && TowerTerminals.isTerminal(this, s) && ElectricCompat.towerTerminalBE() != null) {
         return new SmartBlockEntityTicker();
      } else {
         return !lv.isClientSide && (Integer)s.getValue(this.part) == 0 ? (l, p, st, be) -> {
            if (be instanceof Ticking tk) {
               tk.serverTick();
            }

         } : null;
      }
   }

   protected RenderShape getRenderShape(BlockState s) {
      return RenderShape.MODEL;
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return this.def.outline((Integer)s.getValue(this.part), (Direction.Axis)s.getValue(AXIS));
   }

   protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return this.def.collision((Integer)s.getValue(this.part), (Direction.Axis)s.getValue(AXIS));
   }

   protected boolean propagatesSkylightDown(BlockState s, BlockGetter l, BlockPos p) {
      return true;
   }

   protected float getShadeBrightness(BlockState s, BlockGetter l, BlockPos p) {
      return 1.0F;
   }

   protected ItemStack drop(Level lv, BlockPos core) {
      ItemStack st = new ItemStack(this.asItem());
      BlockEntity var5 = lv.getBlockEntity(core);
      if (var5 instanceof Tuned t) {
         st.set((DataComponentType)ModRegistry.TUNING.get(), t.getTuning());
         if (t.getStationName() != null) {
            st.set(DataComponents.CUSTOM_NAME, t.getStationName());
         }
      }

      return st;
   }

   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState st, Level lv, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
      if (stack.getItem() instanceof CableItem) {
         return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      } else {
         BlockPos core = this.corePos(st, pos);
         Item var11 = stack.getItem();
         if (var11 instanceof Tunable) {
            Tunable tn = (Tunable)var11;
            if (tn.canTune(stack)) {
               BlockEntity var14 = lv.getBlockEntity(core);
               if (var14 instanceof Tuned) {
                  Tuned be = (Tuned)var14;
                  if (!pl.isSecondaryUseActive()) {
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
            }
         }

         BlockState cs = lv.getBlockState(core);
         if (cs.hasProperty(TowerCoreBE.CRYSTAL) && pos.distManhattan(core) <= 2) {
            int v = stack.is(Items.AMETHYST_SHARD) ? 1 : (stack.is(Items.AMETHYST_BLOCK) ? 2 : 0);
            if (v > 0) {
               if ((Integer)cs.getValue(TowerCoreBE.CRYSTAL) != 0) {
                  return ItemInteractionResult.CONSUME;
               }

               if (!lv.isClientSide) {
                  lv.setBlock(core, (BlockState)cs.setValue(TowerCoreBE.CRYSTAL, v), 3);
                  if (!pl.getAbilities().instabuild) {
                     stack.shrink(1);
                  }

                  lv.playSound((Player)null, core, (SoundEvent)ModSounds.CRYSTAL_IN.get(), SoundSource.BLOCKS, 1.0F, v == 2 ? 0.7F : 1.0F);
               }

               return ItemInteractionResult.sidedSuccess(lv.isClientSide);
            }
         }

         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   protected InteractionResult useWithoutItem(BlockState st, Level lv, BlockPos pos, Player pl, BlockHitResult hit) {
      BlockPos core = this.corePos(st, pos);
      BlockState cs = lv.getBlockState(core);
      if (pl.isSecondaryUseActive() && cs.hasProperty(TowerCoreBE.CRYSTAL) && (Integer)cs.getValue(TowerCoreBE.CRYSTAL) > 0 && pos.distManhattan(core) <= 2) {
         if (!lv.isClientSide) {
            int v = (Integer)cs.getValue(TowerCoreBE.CRYSTAL);
            lv.setBlock(core, (BlockState)cs.setValue(TowerCoreBE.CRYSTAL, 0), 3);
            Block.popResource(lv, core.above(), new ItemStack(v == 2 ? Items.AMETHYST_BLOCK : Items.AMETHYST_SHARD));
            lv.playSound((Player)null, core, (SoundEvent)ModSounds.CRYSTAL_OUT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         return InteractionResult.sidedSuccess(lv.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public ItemStack getCloneItemStack(LevelReader l, BlockPos p, BlockState s) {
      return new ItemStack(this.asItem());
   }

   public BlockState playerWillDestroy(Level lv, BlockPos pos, BlockState st, Player pl) {
      if (!lv.isClientSide) {
         BlockPos core = this.corePos(st, pos);
         if (!pl.getAbilities().instabuild) {
            Block.popResource(lv, pos, this.drop(lv, core));
         }
      }

      return super.playerWillDestroy(lv, pos, st, pl);
   }

   protected void onRemove(BlockState s, Level lv, BlockPos pos, BlockState ns, boolean moved) {
      if (!ns.is(this) && !lv.isClientSide) {
         BlockPos core = this.corePos(s, pos);
         Direction.Axis ax = (Direction.Axis)s.getValue(AXIS);
         if ((Integer)s.getValue(this.part) == 0) {
            BlockEntity var9 = lv.getBlockEntity(pos);
            if (var9 instanceof CableNode) {
               CableNode n = (CableNode)var9;
               Cables.broken(lv, pos, new ArrayList(n.links().list()));
            }
         }

         for(int i = 0; i < this.def.size(); ++i) {
            BlockPos q = this.def.partFromCore(core, i, ax);
            if (!q.equals(pos)) {
               BlockState qs = lv.getBlockState(q);
               if (qs.is(this) && (Integer)qs.getValue(this.part) == i) {
                  lv.setBlock(q, Blocks.AIR.defaultBlockState(), 35);
               }
            }
         }
      }

      super.onRemove(s, lv, pos, ns, moved);
   }

   public <V extends Comparable<V>> void setAll(Level lv, BlockPos core, Property<V> prop, V v) {
      BlockState cs = lv.getBlockState(core);
      if (cs.is(this) && cs.getValue(prop) != v) {
         Direction.Axis ax = (Direction.Axis)cs.getValue(AXIS);

         for(int i = 0; i < this.def.size(); ++i) {
            BlockPos q = this.def.partFromCore(core, i, ax);
            BlockState qs = lv.getBlockState(q);
            if (qs.is(this)) {
               lv.setBlock(q, (BlockState)qs.setValue(prop, v), 2);
            }
         }

      }
   }

   public static <V extends Comparable<V>> void setCore(Level lv, BlockPos core, Property<V> prop, V v) {
      BlockState s = lv.getBlockState(core);
      if (s.hasProperty(prop) && s.getValue(prop) != v) {
         lv.setBlock(core, (BlockState)s.setValue(prop, v), 3);
      }

   }

   static {
      AXIS = BlockStateProperties.HORIZONTAL_AXIS;
      PENDING = new ThreadLocal();
   }

   public interface Ticking {
      void serverTick();
   }
}
