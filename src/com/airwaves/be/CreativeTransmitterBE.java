package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.block.TransmitterBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeTransmitterBE extends TransmitterBE {
   public CreativeTransmitterBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.CREATIVE_TX_BE.get(), p, s);
   }

   public void refresh() {
      if (this.level != null) {
         BlockState st = this.getBlockState();
         if (st.getBlock() instanceof TransmitterBlock) {
            boolean held = this.level.getGameTime() - this.consoleHold < 40L;
            this.standby = this.level.hasNeighborSignal(this.worldPosition) || held;
            this.active = !this.standby;
            this.problem = this.standby ? (held ? "standby (radio console)" : "standby (redstone)") : "";
            this.range = this.active ? (double)1.0E8F : (double)0.0F;
            this.baseQ = 1.0F;
            this.crystal = 2;
            this.antennaHeight = 0;
            boolean onAir = this.active && this.level.getGameTime() - this.lastAir < 40L;
            BlockState ns = (BlockState)((BlockState)st.setValue(TransmitterBlock.ACTIVE, this.active)).setValue(TransmitterBlock.ON_AIR, onAir);
            if (ns != st) {
               this.level.setBlock(this.worldPosition, ns, 3);
            }

            this.sync();
         }
      }
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      tip.add(Component.literal("    Creative Transmitter").withStyle(ChatFormatting.LIGHT_PURPLE));
      String var10001 = this.tuning.display();
      tip.add(Component.literal("    " + var10001 + (this.stationName != null ? "  " + this.stationName.getString() : "")).withStyle(ChatFormatting.AQUA));
      tip.add(Component.literal(this.active ? "    on the air, heard everywhere" : "    " + this.problem).withStyle(this.active ? ChatFormatting.GREEN : ChatFormatting.GOLD));
      tip.add(Component.literal("    " + (this.wired ? "microphone cabled" : "built-in mic")).withStyle(ChatFormatting.GRAY));
      return true;
   }
}
