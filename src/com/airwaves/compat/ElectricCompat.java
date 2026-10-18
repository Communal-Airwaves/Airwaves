package com.airwaves.compat;

import com.airwaves.Config;
import com.airwaves.Tier;
import com.airwaves.be.DimensionalRelayBE;
import com.airwaves.be.JammerBE;
import com.airwaves.be.TransmitterBE;
import com.airwaves.block.DimensionalRelayBlock;
import com.airwaves.block.JammerBlock;
import com.airwaves.block.TransmitterBlock;
import com.airwaves.cable.CableType;
import com.airwaves.compat.cee.CEECompat;
import com.airwaves.compat.cee.CEEWires;
import com.airwaves.compat.powergrid.PGCompat;
import com.airwaves.structure.KineticStructureBlock;
import com.airwaves.structure.StructureDef;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class ElectricCompat {
   private ElectricCompat() {
   }

   public static void register() {
      if (Config.powerGridLoaded()) {
         PGCompat.register();
      }

   }

   public static InteractionResult ceeWire(UseOnContext ctx, CableType type) {
      return !Config.ceeLoaded() ? InteractionResult.PASS : CEEWires.use(ctx, type);
   }

   public static BiFunction<BlockPos, BlockState, BlockEntity> towerTerminalBE() {
      return Config.powerGridLoaded() ? (p, s) -> PGCompat.towerTerminalBE(p, s) : null;
   }

   public static KineticStructureBlock tower(StructureDef d, BlockBehaviour.Properties p, BiFunction<BlockPos, BlockState, BlockEntity> be) {
      return Config.ceeLoaded() ? CEECompat.tower(d, p, be) : new KineticStructureBlock(d, p, be);
   }

   public static TransmitterBlock transmitter(Tier t, BlockBehaviour.Properties p) {
      return Config.ceeLoaded() ? CEECompat.transmitter(t, p) : new TransmitterBlock(t, p);
   }

   public static JammerBlock jammer(BlockBehaviour.Properties p) {
      return Config.ceeLoaded() ? CEECompat.jammer(p) : new JammerBlock(p);
   }

   public static DimensionalRelayBlock dimensionalRelay(BlockBehaviour.Properties p) {
      return Config.ceeLoaded() ? CEECompat.dimensionalRelay(p) : new DimensionalRelayBlock(p);
   }

   public static BlockEntityType.BlockEntitySupplier<TransmitterBE> transmitterBE() {
      return Config.powerGridLoaded() ? (p, s) -> PGCompat.transmitterBE(p, s) : TransmitterBE::new;
   }

   public static BlockEntityType.BlockEntitySupplier<JammerBE> jammerBE() {
      return Config.powerGridLoaded() ? (p, s) -> PGCompat.jammerBE(p, s) : JammerBE::new;
   }

   public static BlockEntityType.BlockEntitySupplier<DimensionalRelayBE> dimensionalRelayBE() {
      return Config.powerGridLoaded() ? (p, s) -> PGCompat.dimensionalRelayBE(p, s) : DimensionalRelayBE::new;
   }
}
