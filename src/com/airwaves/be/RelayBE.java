package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.logic.AntennaUtil;
import com.airwaves.structure.StructureBlock;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RelayBE extends KineticTunableBE implements CableNode {
   public static final double STRESS_SMALL = (double)12.0F;
   public static final double STRESS_BIG = (double)24.0F;
   public boolean active;
   public boolean relaying;
   public double range;
   public float antQ;
   private final LinkSet links = new LinkSet();

   public RelayBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.RELAY_BE.get(), p, s);
   }

   public boolean big() {
      return this.getBlockState().getBlock() == ModRegistry.RELAY_TOWER_BIG.get();
   }

   public int height() {
      return this.big() ? 28 : 16;
   }

   public int tier() {
      return this.big() ? 3 : 2;
   }

   public double stressImpact() {
      return this.big() ? (double)24.0F : (double)12.0F;
   }

   protected boolean usesPower() {
      return this.active;
   }

   public int ratedVolts() {
      return this.big() ? 240 : 120;
   }

   public void refresh() {
      if (this.level != null) {
         AntennaUtil.Info info = new AntennaUtil.Info(this.height(), this.tier());
         this.active = this.turning() && !this.level.hasNeighborSignal(this.worldPosition);
         this.range = this.active ? AntennaUtil.range(info, this.tuning.fm()) * (this.big() ? 1.8 : (double)1.5F) * this.powerFactor() * this.altitudeFactor() : (double)0.0F;
         this.antQ = Math.min(1.0F, AntennaUtil.quality(info) + 0.1F);
         this.sync();
      }
   }

   public double rxGain() {
      return AntennaUtil.rxGain(new AntennaUtil.Info(this.height(), this.tier())) * 1.2;
   }

   public double antennaTop() {
      return (double)(this.radioPos().getY() + this.height());
   }

   public LinkSet links() {
      return this.links;
   }

   public BlockPos nodePos() {
      return this.worldPosition;
   }

   public int maxLinks(int slot) {
      return 4;
   }

   public Vec3 anchor(int slot) {
      Block var3 = this.getBlockState().getBlock();
      if (var3 instanceof StructureBlock b) {
         return b.def.connector(this.worldPosition, 0, (Direction.Axis)this.getBlockState().getValue(StructureBlock.AXIS));
      } else {
         return Vec3.atCenterOf(this.worldPosition);
      }
   }

   public void cablesChanged() {
      this.sync();
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      super.addToGoggleTooltip(tip, sneaking);
      tip.add(Component.literal("    " + (this.big() ? "Big Relay Tower" : "Relay Tower")).withStyle(ChatFormatting.WHITE));
      this.radioTooltip(tip, this.active, this.range);
      if (this.active) {
         tip.add(Component.literal(this.relaying ? "    relaying a station" : "    listening, nothing to relay").withStyle(this.relaying ? ChatFormatting.GREEN : ChatFormatting.GRAY));
      }

      return true;
   }

   protected void write(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.write(tag, r, clientPacket);
      this.links.save(tag);
      tag.putBoolean("active", this.active);
      tag.putBoolean("relaying", this.relaying);
      tag.putDouble("range", this.range);
   }

   protected void read(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.read(tag, r, clientPacket);
      this.links.load(tag);
      if (clientPacket) {
         this.active = tag.getBoolean("active");
         this.relaying = tag.getBoolean("relaying");
         this.range = tag.getDouble("range");
      }

   }
}
