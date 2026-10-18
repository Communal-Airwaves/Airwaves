package com.airwaves.compat.cee;

import com.airwaves.Tier;
import com.airwaves.block.DimensionalRelayBlock;
import com.airwaves.block.JammerBlock;
import com.airwaves.block.TransmitterBlock;
import com.airwaves.structure.KineticStructureBlock;
import com.airwaves.structure.StructureDef;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.devices.device.SimulatedDeviceType;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CEECompat {
   private static final DeferredRegister<SimulatedDeviceType<?>> DEVICES;
   public static final DeferredHolder<SimulatedDeviceType<?>, SimulatedDeviceType<MachineDevice>> MACHINE;

   private CEECompat() {
   }

   public static void register(IEventBus modBus) {
      DEVICES.register(modBus);
   }

   public static KineticStructureBlock tower(StructureDef d, BlockBehaviour.Properties p, BiFunction<BlockPos, BlockState, BlockEntity> be) {
      return new CEETowerBlock(d, p, be);
   }

   public static TransmitterBlock transmitter(Tier t, BlockBehaviour.Properties p) {
      return new CEEBlocks.Transmitter(t, p);
   }

   public static JammerBlock jammer(BlockBehaviour.Properties p) {
      return new CEEBlocks.Jammer(p);
   }

   public static DimensionalRelayBlock dimensionalRelay(BlockBehaviour.Properties p) {
      return new CEEBlocks.DimRelay(p);
   }

   static {
      DEVICES = DeferredRegister.create(CEERegistries.SIMULATED_DEVICE_TYPE, "airwaves");
      MACHINE = DEVICES.register("machine", () -> new SimulatedDeviceType(ResourceLocation.fromNamespaceAndPath("airwaves", "machine"), (type, level, pos, sd) -> new MachineDevice(level, pos, sd, type)));
   }
}
