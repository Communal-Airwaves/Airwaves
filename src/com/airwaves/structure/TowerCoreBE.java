package com.airwaves.structure;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.be.TransmitterBE;
import com.airwaves.cable.CableType;
import com.airwaves.logic.AntennaUtil;
import com.airwaves.logic.WeatherUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

public class TowerCoreBE extends TransmitterBE {
   public static final IntegerProperty CRYSTAL = IntegerProperty.create("crystal", 0, 2);
   public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
   public static final int HEIGHT = 27;
   public static final double STRESS = (double)64.0F;

   public TowerCoreBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.TOWER_BE.get(), p, s);
   }

   public int tier() {
      return 4;
   }

   public double stressImpact() {
      return (double)64.0F;
   }

   public int ratedVolts() {
      return 240;
   }

   public boolean accepts(int slot, CableType t) {
      return true;
   }

   public Vec3 anchor(int slot) {
      return StructureNodes.anchor(this, slot);
   }

   public double antennaTop() {
      return (double)(this.radioPos().getY() + 27);
   }

   public void refresh() {
      if (this.level != null) {
         BlockState st = this.getBlockState();
         if (st.hasProperty(CRYSTAL)) {
            this.crystal = (Integer)st.getValue(CRYSTAL);
            this.standby = this.level.hasNeighborSignal(this.worldPosition);
            this.antennaHeight = 27;
            this.problem = this.crystal == 0 ? "no amethyst in the chamber" : (this.standby ? "standby (redstone)" : (!this.turning() ? "needs power (a shaft on the drive shaft at the base edge, or electricity)" : ""));
            this.active = this.problem.isEmpty();
            AntennaUtil.Info a = new AntennaUtil.Info(96, 4);
            this.range = this.active ? AntennaUtil.range(a, this.tuning.fm()) * 1.4 * this.powerFactor() * this.altitudeFactor() : (double)0.0F;
            this.baseQ = Math.min(1.0F, (AntennaUtil.quality(a) + 0.08F) * (this.crystal == 2 ? 1.1F : 1.0F) * (float)Math.sqrt((double)WeatherUtil.factor(this.level, this.radioPos().above(27), this.tuning.fm())));
            if ((Boolean)st.getValue(ACTIVE) != this.active) {
               this.level.playSound((Player)null, this.worldPosition, this.active ? (SoundEvent)ModSounds.POWER_ON.get() : (SoundEvent)ModSounds.POWER_OFF.get(), SoundSource.BLOCKS, 1.0F, 0.8F);
               this.level.setBlock(this.worldPosition, (BlockState)st.setValue(ACTIVE, this.active), 3);
            }

            this.sync();
         }
      }
   }

   public void onAir(ServerLevel lv) {
      this.lastAir = lv.getGameTime();
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      boolean r = super.addToGoggleTooltip(tip, sneaking);

      for(int i = 0; i < tip.size(); ++i) {
         if (((Component)tip.get(i)).getString().trim().equals("Transmitter")) {
            tip.set(i, Component.literal("    Radio Tower").withStyle(ChatFormatting.WHITE));
         }
      }

      return r;
   }
}
