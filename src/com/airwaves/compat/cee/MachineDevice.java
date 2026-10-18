package com.airwaves.compat.cee;

import com.airwaves.power.ElectricLoad;
import com.airwaves.power.PowerSupply;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.SimpleElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MachineDevice extends SimpleElectricalDevice {
   private static final Map<BlockPos, ElectricLoad> LOADS = new HashMap();
   private final ElectricLoad load = new ElectricLoad();

   public MachineDevice(Level level, BlockPos pos, DevicesSavedData sd, SimulatedDeviceType<?> type) {
      super(level, pos, sd, type);
      LOADS.put(pos.immutable(), this.load);
   }

   public static ElectricLoad at(BlockPos pos) {
      return (ElectricLoad)LOADS.get(pos);
   }

   public void preTick(BridgeCollector bridges) {
      bridges.builder(this.pos).resistor(0, 1, this.load.resistance());
   }

   public void postTick(SimulationResults results) {
      double v = results.getVoltageAt(this.pos, 0, 1);
      double joules = v * v / this.load.resistance() / (double)20.0F;
      PowerSupply s = null;
      if (this.level.isLoaded(this.pos)) {
         s = PowerSupply.at(this.level, this.pos);
      }

      this.load.update(s, v, joules, this.level.getGameTime());
   }

   public void onDestroy() {
      LOADS.remove(this.pos);
      super.onDestroy();
   }

   public boolean shouldRemove(BlockState oldState, BlockState newState) {
      return oldState.getBlock() != newState.getBlock();
   }
}
