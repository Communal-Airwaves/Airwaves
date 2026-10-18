package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.block.RadioBlock;
import com.airwaves.logic.ClientSinks;
import com.airwaves.logic.RadioNetwork;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadioBE extends TunableBE implements AudioSink {
   public RadioBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.RADIO_BE.get(), p, s);
   }

   public void onLoad() {
      super.onLoad();
      if (this.level != null && this.level.isClientSide) {
         ClientSinks.SINKS.add(this);
      }

   }

   public void setRemoved() {
      super.setRemoved();
      ClientSinks.removed(this, this);
   }

   public double rxGain() {
      return 0.8;
   }

   public double antennaTop() {
      return super.antennaTop() + 1.3;
   }

   public boolean sinkOn() {
      return (Boolean)this.getBlockState().getValue(RadioBlock.POWERED);
   }

   public BlockPos sinkPos() {
      return this.worldPosition;
   }

   public boolean carrier() {
      return this.carrier;
   }

   public float quality() {
      return this.quality;
   }

   public boolean fm() {
      return this.tuning.fm();
   }

   public void deliver(ServerLevel lv, UUID talker, byte[] data, float gain) {
      RadioNetwork.send(lv, this.worldPosition, talker, data, this.quality, this.tuning.fm(), gain);
   }
}
