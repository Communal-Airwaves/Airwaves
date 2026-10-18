package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.List;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CreativeAntennaBE extends SmartBlockEntity implements IHaveGoggleInformation {
   public static final int MAX = 9999;
   public static final int DEFAULT = 1000;
   public ScrollValueBehaviour range;

   public CreativeAntennaBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.CREATIVE_ANTENNA_BE.get(), p, s);
   }

   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      this.range = new RangeValue(Component.translatable("airwaves.creative_antenna.range"), this, new BaseBox());
      this.range.between(0, 9999);
      this.range.value = 1000;
      this.range.withFormatter((v) -> v + " m");
      behaviours.add(this.range);
   }

   public int range() {
      return this.range == null ? 1000 : this.range.getValue();
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      tip.add(Component.literal("    Creative Antenna").withStyle(ChatFormatting.LIGHT_PURPLE));
      tip.add(Component.literal("    range " + this.range() + " m (hold right-click on the base to change)").withStyle(ChatFormatting.GRAY));
      return true;
   }

   static class RangeValue extends ScrollValueBehaviour {
      RangeValue(Component label, CreativeAntennaBE be, ValueBoxTransform slot) {
         super(label, be, slot);
      }

      public ValueSettingsBoard createBoard(Player player, BlockHitResult hit) {
         return new ValueSettingsBoard(this.label, 99, 10, List.of(Component.translatable("airwaves.creative_antenna.hundreds"), Component.translatable("airwaves.creative_antenna.units")), new ValueSettingsFormatter((vs) -> Component.literal(vs.row() == 0 ? vs.value() * 100 + this.value % 100 + " m" : this.value / 100 * 100 + vs.value() + " m")));
      }

      public void setValueSettings(Player player, ValueSettingsBehaviour.ValueSettings vs, boolean ctrlDown) {
         int v = vs.row() == 0 ? vs.value() * 100 + this.value % 100 : this.value / 100 * 100 + vs.value();
         if (v != this.value) {
            this.setValue(Math.max(0, Math.min(9999, v)));
            this.playFeedbackSound(this);
         }
      }

      public ValueSettingsBehaviour.ValueSettings getValueSettings() {
         return new ValueSettingsBehaviour.ValueSettings(0, this.value / 100);
      }
   }

   static class BaseBox extends ValueBoxTransform.Sided {
      protected Vec3 getSouthLocation() {
         return VecHelper.voxelSpace((double)8.0F, (double)2.0F, (double)13.5F);
      }

      protected boolean isSideActive(BlockState state, Direction d) {
         return d.getAxis().isHorizontal();
      }

      public float getScale() {
         return 0.4F;
      }
   }
}
