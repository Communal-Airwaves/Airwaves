package com.airwaves.compat.powergrid;

import com.airwaves.ModRegistry;
import com.airwaves.be.DimensionalRelayBE;
import com.airwaves.be.JammerBE;
import com.airwaves.be.TransmitterBE;
import com.mojang.datafixers.types.Type;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockState;

public final class PGCompat {
   public static Supplier<BlockEntityType<PGTowerTerminalBE>> TOWER_TERMINAL_BE;

   public static void register() {
      TOWER_TERMINAL_BE = ModRegistry.BES.register("tower_terminal", () -> Builder.of(PGTowerTerminalBE::new, new Block[]{(Block)ModRegistry.RELAY_TOWER_SMALL.get(), (Block)ModRegistry.RELAY_TOWER_BIG.get(), (Block)ModRegistry.RADIO_TOWER.get()}).build((Type)null));
   }

   public static TransmitterBE transmitterBE(BlockPos p, BlockState s) {
      return new PGTransmitterBE(p, s);
   }

   public static JammerBE jammerBE(BlockPos p, BlockState s) {
      return new PGJammerBE(p, s);
   }

   public static DimensionalRelayBE dimensionalRelayBE(BlockPos p, BlockState s) {
      return new PGDimensionalRelayBE(p, s);
   }

   public static BlockEntity towerTerminalBE(BlockPos p, BlockState s) {
      return new PGTowerTerminalBE(p, s);
   }

   private PGCompat() {
   }
}
