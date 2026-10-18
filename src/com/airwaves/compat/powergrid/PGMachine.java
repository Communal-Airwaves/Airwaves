package com.airwaves.compat.powergrid;

import com.airwaves.power.ElectricLoad;
import com.airwaves.power.PowerSupply;
import com.airwaves.power.Terminals;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.base.ITerminalPlacement;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;

public final class PGMachine {
   private static final Map<BlockState, TerminalBoundingBox[]> BOXES = new ConcurrentHashMap();

   private PGMachine() {
   }

   public static ITerminalPlacement terminal(BlockState st, int index) {
      return index >= 0 && index <= 1 && Terminals.has(st) ? ((TerminalBoundingBox[])BOXES.computeIfAbsent(st, (k) -> new TerminalBoundingBox[]{(new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, Terminals.box(k, 0))).withOrigin(Terminals.point(k, 0).scale((double)16.0F)).withColor(16726843), (new TerminalBoundingBox(IDecoratedTerminal.NEGATIVE, Terminals.box(k, 1))).withOrigin(Terminals.point(k, 1).scale((double)16.0F)).withColor(3899647)}))[index] : null;
   }

   public static final class Load {
      public final ElectricLoad logic = new ElectricLoad();
      private ElectricWire wire;

      public void build(IElectricEntity.CircuitBuilder b) {
         b.setTerminalCount(2);
         this.wire = b.connect(100000.0F, b.terminalNode(0), b.terminalNode(1));
      }

      public void tick(SmartBlockEntity be) {
         if (this.wire != null && be.getLevel() != null && !be.getLevel().isClientSide) {
            double v = this.wire.potentialDifference();
            double i = this.wire.current();
            double joules = i * i * this.wire.getResistance() / (double)20.0F;
            this.logic.update(PowerSupply.at(be.getLevel(), be.getBlockPos()), v, joules, be.getLevel().getGameTime());
            float want = (float)this.logic.resistance();
            float had = (float)this.wire.getResistance();
            if (Math.abs(want - had) > Math.max(0.01F, had * 0.03F)) {
               this.wire.setResistance((double)want);
            }

         }
      }
   }
}
