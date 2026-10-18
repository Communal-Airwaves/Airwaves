package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.block.ReceiverBlock;
import com.airwaves.compat.SableCompat;
import com.airwaves.logic.Emitter;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeReceiverBE extends ReceiverBE {
   public CreativeReceiverBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.CREATIVE_RX_BE.get(), p, s);
   }

   public void refresh() {
      if (this.level != null) {
         boolean was = this.ok;
         this.ok = true;
         this.crystal = 2;
         this.grounded = false;
         this.antennaHeight = 0;
         if (!was) {
            this.sync();
         }

         BlockState st = this.getBlockState();
         if (st.getBlock() instanceof ReceiverBlock && !(Boolean)st.getValue(ReceiverBlock.ACTIVE)) {
            this.level.setBlock(this.worldPosition, (BlockState)st.setValue(ReceiverBlock.ACTIVE, true), 3);
         }

      }
   }

   public void updateSignal(List<Emitter> emitters, List<BlockPos> jam) {
      BlockPos me = SableCompat.globalPos(this.level, this.worldPosition);
      Emitter pick = null;
      double bd = Double.MAX_VALUE;

      for(Emitter e : emitters) {
         if (e.tuning().matches(this.tuning)) {
            double d = me.distSqr(e.pos());
            if (d < bd) {
               bd = d;
               pick = e;
            }
         }
      }

      boolean c = pick != null;
      boolean changed = c != this.carrier || c && this.quality < 1.0F || c && this.bestRoot != pick.root();
      this.carrier = c;
      this.quality = c ? 1.0F : 0.0F;
      this.bestRoot = c ? pick.root() : 0L;
      if (changed) {
         this.sync();
      }

      this.afterSignal();
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      tip.add(Component.literal("    Creative Receiver").withStyle(ChatFormatting.LIGHT_PURPLE));
      String var10001 = this.tuning.display();
      tip.add(Component.literal("    " + var10001 + (this.stationName != null ? "  " + this.stationName.getString() : "")).withStyle(ChatFormatting.AQUA));
      tip.add(Component.literal(this.carrier ? "    ▮▮▮▮▮  100% signal" : "    no station on this frequency").withStyle(this.carrier ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
      var10001 = this.links().isEmpty() ? "built-in speaker" : this.links().size() + " cable" + (this.links().size() > 1 ? "s" : "");
      tip.add(Component.literal("    " + var10001).withStyle(ChatFormatting.GRAY));
      return true;
   }
}
