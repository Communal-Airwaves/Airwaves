package com.airwaves.compat.cee;

import com.airwaves.power.Terminals;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface CEEMachine extends ElectricalDeviceBlock<MachineDevice> {
   default SimulatedDeviceType<MachineDevice> getDevice() {
      return (SimulatedDeviceType)CEECompat.MACHINE.get();
   }

   default Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
      Map<Integer, Vec3> m = new HashMap();
      m.put(0, this.getNodePosition(level, pos, state, 0));
      m.put(1, this.getNodePosition(level, pos, state, 1));
      return m;
   }

   default Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
      return id >= 0 && id <= 1 ? Terminals.point(state, id) : null;
   }

   default MutableComponent getNodeLabel(Level level, BlockPos pos, BlockState state, int id) {
      return id == 0 ? Component.literal("+").withStyle(ChatFormatting.RED) : Component.literal("-").withStyle(ChatFormatting.BLUE);
   }

   default float getNodeSize(Level level, BlockPos pos, BlockState state, int id) {
      return 0.1875F;
   }

   static void placed(Level level, BlockPos pos, BlockState state, Block block) {
      if (!level.getBlockTicks().hasScheduledTick(pos, block)) {
         level.scheduleTick(pos, block, 1);
      }

   }

   static void ticked(CEEMachine m, ServerLevel level, BlockPos pos, BlockState state) {
      List<Integer> nodes = new ArrayList(m.getNodePositions(level, pos, state).keySet());
      InfrastructureSavedData.load(level).registerOrUpdateNodes(pos, nodes);
   }

   static void creativeBreak(Level level, BlockPos pos, Player player) {
      if (level instanceof ServerLevel sl) {
         if (player.isCreative()) {
            InfrastructureSavedData sd = InfrastructureSavedData.load(sl);

            for(InWorldNodeData nodeData : sd.getNodesAt(pos)) {
               for(InWorldNodeConnection connection : sd.getConnections(nodeData)) {
                  sd.removeConnection(connection);
               }
            }
         }
      }

   }
}
