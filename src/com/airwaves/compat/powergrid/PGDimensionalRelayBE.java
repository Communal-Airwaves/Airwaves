package com.airwaves.compat.powergrid;

import com.airwaves.be.DimensionalRelayBE;
import com.airwaves.power.ElectricLoad;
import com.airwaves.power.PowerSupply;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.base.IElectric;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.base.ITerminalPlacement;

public class PGDimensionalRelayBE extends DimensionalRelayBE implements IElectricEntity, IElectric {
   private ElectricBehaviour electric;
   private PGMachine.Load load;

   public PGDimensionalRelayBE(BlockPos p, BlockState s) {
      super(p, s);
   }

   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      super.addBehaviours(behaviours);
      this.electric = new ElectricBehaviour(this);
      behaviours.add(this.electric);
   }

   private PGMachine.Load load() {
      if (this.load == null) {
         this.load = new PGMachine.Load();
      }

      return this.load;
   }

   public void buildCircuit(IElectricEntity.CircuitBuilder builder) {
      this.load().build(builder);
   }

   public int terminalCount() {
      return 2;
   }

   public ITerminalPlacement terminal(BlockState state, int index) {
      return PGMachine.terminal(state, index);
   }

   public void tick() {
      super.tick();
      if (this.level != null && !this.level.isClientSide) {
         this.load().tick(this);
      }

   }

   public void remove() {
      super.remove();
      if (this.electric != null) {
         this.electric.remove();
      }

   }

   protected void write(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.write(tag, r, clientPacket);
      if (clientPacket) {
         tag.putFloat("ev", this.load().logic.volts);
         tag.putFloat("ew", this.load().logic.watts);
         tag.putInt("es", this.load().logic.status.ordinal());
      }

   }

   protected void read(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.read(tag, r, clientPacket);
      if (clientPacket) {
         this.load().logic.volts = tag.getFloat("ev");
         this.load().logic.watts = tag.getFloat("ew");
         int st = tag.getInt("es");
         if (st >= 0 && st < ElectricLoad.Status.values().length) {
            this.load().logic.status = ElectricLoad.Status.values()[st];
         }
      }

   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      boolean r = super.addToGoggleTooltip(tip, sneaking);
      if (this.level != null) {
         this.load().logic.tooltip(tip, PowerSupply.at(this.level, this.worldPosition));
      }

      return r;
   }
}
