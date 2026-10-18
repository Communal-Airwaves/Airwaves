package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.block.TransmitterBlock;
import com.airwaves.logic.AntennaUtil;
import com.airwaves.logic.WeatherUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TransmitterBE extends KineticTunableBE implements CableNode {
   private static final double[][] JACK = new double[][]{{(double)5.0F, (double)3.0F, (double)3.0F}, {(double)4.0F, (double)3.0F, (double)2.0F}, {(double)3.0F, (double)2.0F, (double)0.0F}, {(double)4.0F, (double)3.0F, (double)0.0F}};
   public static final double[] STRESS = new double[]{(double)4.0F, (double)8.0F, (double)16.0F, (double)32.0F};
   public boolean active;
   public boolean standby;
   public boolean wired;
   public int antennaHeight;
   public Direction beam;
   public double range;
   public float baseQ;
   protected int crystal;
   protected long lastAir;
   public long consoleHold;
   protected String problem;
   protected final LinkSet links;
   private static final int[] VOLTS = new int[]{12, 24, 120, 120};

   public TransmitterBE(BlockPos p, BlockState s) {
      this((BlockEntityType)ModRegistry.TRANSMITTER_BE.get(), p, s);
   }

   protected TransmitterBE(BlockEntityType<?> type, BlockPos p, BlockState s) {
      super(type, p, s);
      this.consoleHold = -1000L;
      this.problem = "";
      this.links = new LinkSet();
   }

   public int tier() {
      Block var2 = this.getBlockState().getBlock();
      int var10000;
      if (var2 instanceof TransmitterBlock b) {
         var10000 = b.tier.level;
      } else {
         var10000 = 1;
      }

      return var10000;
   }

   public double stressImpact() {
      return STRESS[Math.max(0, Math.min(STRESS.length - 1, this.tier() - 1))];
   }

   protected boolean usesPower() {
      return this.active;
   }

   public int ratedVolts() {
      return VOLTS[Math.max(0, Math.min(VOLTS.length - 1, this.tier() - 1))];
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
      return CableNode.facing(this, JACK[this.tier() - 1][0], JACK[this.tier() - 1][1], JACK[this.tier() - 1][2]);
   }

   public void cablesChanged() {
      this.sync();
   }

   public double antennaTop() {
      return super.antennaTop() + (double)this.antennaHeight;
   }

   public void refresh() {
      if (this.level != null) {
         BlockState st = this.getBlockState();
         if (st.getBlock() instanceof TransmitterBlock) {
            Direction f = (Direction)st.getValue(TransmitterBlock.HORIZONTAL_FACING);
            BlockPos back = this.worldPosition.relative(f.getOpposite());
            this.crystal = (Integer)st.getValue(TransmitterBlock.CRYSTAL);
            boolean ground = this.level.isLoaded(back) && this.level.getBlockState(back).is(ModRegistry.GROUND_PLANE);
            AntennaUtil.Info a = ground ? AntennaUtil.scan(this.level, back.above(), this.tier()) : AntennaUtil.Info.NONE;
            boolean match = a.tier() == this.tier();
            this.antennaHeight = match ? a.height() : 0;
            this.beam = match ? a.beam() : null;
            boolean held = this.level.getGameTime() - this.consoleHold < 40L;
            this.standby = this.level.hasNeighborSignal(this.worldPosition) || held;
            this.problem = this.crystal == 0 ? "no amethyst in the chamber" : (!ground ? "needs a copper block behind it" : (!match ? "needs a " + AntennaUtil.tierName(this.tier()).toLowerCase() + " antenna on the copper" : (this.antennaHeight < 4 ? "antenna too short (" + this.antennaHeight + "/4)" : (this.standby ? (held ? "standby (radio console)" : "standby (redstone)") : (!this.turning() ? "needs power" : "")))));
            this.active = this.problem.isEmpty();
            this.range = this.active ? (a.creative() ? a.fixedRange() : AntennaUtil.range(a, this.tuning.fm()) * this.powerFactor() * this.altitudeFactor()) : (double)0.0F;
            this.baseQ = Math.min(1.0F, AntennaUtil.quality(a) * (this.crystal == 2 ? 1.1F : 1.0F) * (float)Math.sqrt((double)WeatherUtil.factor(this.level, this.radioPos().above(1 + this.antennaHeight), this.tuning.fm())));
            boolean onAir = this.active && this.level.getGameTime() - this.lastAir < 40L;
            if (this.active != (Boolean)st.getValue(TransmitterBlock.ACTIVE)) {
               this.level.playSound((Player)null, this.worldPosition, this.active ? (SoundEvent)ModSounds.POWER_ON.get() : (SoundEvent)ModSounds.POWER_OFF.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
            }

            BlockState ns = (BlockState)((BlockState)st.setValue(TransmitterBlock.ACTIVE, this.active)).setValue(TransmitterBlock.ON_AIR, onAir);
            if (ns != st) {
               this.level.setBlock(this.worldPosition, ns, 3);
            }

            this.sync();
         }
      }
   }

   public void onAir(ServerLevel lv) {
      this.lastAir = lv.getGameTime();
      BlockState st = this.getBlockState();
      if (st.getBlock() instanceof TransmitterBlock && !(Boolean)st.getValue(TransmitterBlock.ON_AIR)) {
         lv.setBlock(this.worldPosition, (BlockState)st.setValue(TransmitterBlock.ON_AIR, true), 3);
      }

   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      super.addToGoggleTooltip(tip, sneaking);
      tip.add(Component.literal("    Transmitter").withStyle(ChatFormatting.WHITE));
      int cr = this.getBlockState().getBlock() instanceof TransmitterBlock ? (Integer)this.getBlockState().getValue(TransmitterBlock.CRYSTAL) : this.crystal;
      tip.add(Tuned.chamberLine(cr));
      this.radioTooltip(tip, this.active, this.range);
      String pr = cr > 0 && this.problem.startsWith("no amethyst") ? "" : (cr == 0 ? "no amethyst in the chamber" : this.problem);
      if (!pr.isEmpty() && !pr.startsWith("needs power") && !pr.startsWith("needs rotation")) {
         tip.add(Component.literal("    " + pr).withStyle(ChatFormatting.GOLD));
      }

      if (this.active) {
         tip.add(Component.literal("    antenna " + this.antennaHeight + " tall, " + (this.wired ? "microphone cabled" : "built-in mic")).withStyle(ChatFormatting.GRAY));
      }

      if (this.active && this.beam != null) {
         tip.add(Component.literal("    Yagi beam to the " + this.beam.getName() + ": x1.9 ahead, nothing to the sides or behind").withStyle(ChatFormatting.AQUA));
      }

      return true;
   }

   protected void write(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.write(tag, r, clientPacket);
      this.links.save(tag);
      tag.putBoolean("active", this.active);
      tag.putDouble("range", this.range);
      tag.putString("problem", this.problem);
      tag.putInt("ant", this.antennaHeight);
      tag.putBoolean("wired", this.wired);
      tag.putInt("beam", this.beam == null ? -1 : this.beam.get2DDataValue());
   }

   protected void read(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.read(tag, r, clientPacket);
      this.links.load(tag);
      if (clientPacket) {
         this.active = tag.getBoolean("active");
         this.range = tag.getDouble("range");
         this.problem = tag.getString("problem");
         this.antennaHeight = tag.getInt("ant");
         this.wired = tag.getBoolean("wired");
         int bm = tag.contains("beam") ? tag.getInt("beam") : -1;
         this.beam = bm < 0 ? null : Direction.from2DDataValue(bm);
      }

   }
}
