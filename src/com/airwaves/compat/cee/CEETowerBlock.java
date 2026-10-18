package com.airwaves.compat.cee;

import com.airwaves.power.TowerTerminals;
import com.airwaves.structure.KineticStructureBlock;
import com.airwaves.structure.StructureDef;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CEETowerBlock extends KineticStructureBlock implements CEEMachine {
   public CEETowerBlock(StructureDef d, BlockBehaviour.Properties p, BiFunction<BlockPos, BlockState, BlockEntity> be) {
      super(d, p, be);
   }

   private boolean terminal(BlockState s) {
      return TowerTerminals.isTerminal(this, s);
   }

   public Map<Integer, Vec3> getNodePositions(Level level, BlockPos pos, BlockState state) {
      return this.terminal(state) ? CEEMachine.super.getNodePositions(level, pos, state) : Map.of();
   }

   public Vec3 getNodePosition(Level level, BlockPos pos, BlockState state, int id) {
      return this.terminal(state) ? CEEMachine.super.getNodePosition(level, pos, state, id) : null;
   }

   protected void onPlace(BlockState s, Level l, BlockPos p, BlockState old, boolean moving) {
      super.onPlace(s, l, p, old, moving);
      if (this.terminal(s)) {
         CEEMachine.placed(l, p, s, this);
      }

   }

   protected void tick(BlockState s, ServerLevel l, BlockPos p, RandomSource r) {
      if (this.terminal(s)) {
         CEEMachine.ticked(this, l, p, s);
      }

      super.tick(s, l, p, r);
   }

   public BlockState playerWillDestroy(Level l, BlockPos p, BlockState s, Player pl) {
      if (this.terminal(s)) {
         CEEMachine.creativeBreak(l, p, pl);
      }

      return super.playerWillDestroy(l, p, s, pl);
   }

   public InteractionResult onSneakWrenched(BlockState s, UseOnContext c) {
      if (this.terminal(s)) {
         this.removeWiresByPlayer(c.getPlayer(), c.getLevel(), c.getClickedPos());
      }

      return super.onSneakWrenched(s, c);
   }
}
