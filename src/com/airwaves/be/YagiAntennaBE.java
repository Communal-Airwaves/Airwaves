package com.airwaves.be;

import com.airwaves.logic.AntennaUtil;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class YagiAntennaBE extends SmartBlockEntity {
   public ScrollValueBehaviour bearing;
   public float shown = Float.NaN;

   public YagiAntennaBE(BlockEntityType<?> t, BlockPos p, BlockState s) {
      super(t, p, s);
   }

   public void addBehaviours(List<BlockEntityBehaviour> list) {
      this.bearing = (new ScrollValueBehaviour(Component.literal("Bearing"), this, new TopBox())).between(0, 359).withFormatter((v) -> v + "° " + AntennaUtil.compass((float)v));
      list.add(this.bearing);
   }

   public int bearing() {
      return this.bearing == null ? 0 : this.bearing.getValue();
   }

   public void setBearing(int deg) {
      if (this.bearing != null) {
         this.bearing.setValue(Math.floorMod(deg, 360));
      }

   }

   private static class TopBox extends CenteredSideValueBoxTransform {
      static final float TOP = 12.0F;

      TopBox() {
         super((state, d) -> d == Direction.UP);
      }

      public Vec3 getLocalOffset(LevelAccessor l, BlockPos p, BlockState s) {
         return new Vec3((double)0.5F, 0.753125, (double)0.5F);
      }
   }
}
