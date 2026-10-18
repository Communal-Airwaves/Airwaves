package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.logic.Cables;
import com.airwaves.logic.RadioNetwork;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MicrophoneBE extends NodeBE {
   public TransmitterBE feeds;
   public List<Cables.Seg> path = List.of();
   public long lastPulse;
   public float line = 1.0F;

   public MicrophoneBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.MIC_BE.get(), p, s);
   }

   protected MicrophoneBE(BlockEntityType<?> t, BlockPos p, BlockState s) {
      super(t, p, s);
   }

   public int maxLinks(int slot) {
      return 1;
   }

   public Vec3 anchor(int slot) {
      return CableNode.facing(this, (double)8.0F, (double)0.5F, (double)15.0F);
   }

   public void onLoad() {
      super.onLoad();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.register(this);
      }

   }

   public void setRemoved() {
      super.setRemoved();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.unregister(this);
      }

   }
}
