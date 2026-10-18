package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.block.ReceiverBlock;
import com.airwaves.logic.AntennaUtil;
import com.airwaves.logic.Cables;
import com.airwaves.logic.ClientSinks;
import com.airwaves.logic.RadioNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ReceiverBE extends TunableBE implements AudioSink, CableNode {
   private static final double[][] JACK = new double[][]{{(double)10.0F, (double)5.0F, (double)4.0F}, {(double)4.0F, (double)3.0F, (double)3.0F}, {(double)3.0F, (double)3.0F, (double)1.0F}, {(double)3.0F, (double)3.0F, (double)0.0F}};
   public boolean ok;
   public boolean grounded;
   public int crystal;
   public int antennaHeight;
   public List<SpeakerBE> speakers = new ArrayList();
   public List<Cables.Seg> paths = new ArrayList();
   public long lastPulse;
   public final Map<SpeakerBE, Float> lineQ = new HashMap();
   private AntennaUtil.Info info;
   private int lastSig;
   private final LinkSet links;

   public ReceiverBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.RECEIVER_BE.get(), p, s);
      this.info = AntennaUtil.Info.NONE;
      this.links = new LinkSet();
   }

   protected ReceiverBE(BlockEntityType<?> type, BlockPos p, BlockState s) {
      super(type, p, s);
      this.info = AntennaUtil.Info.NONE;
      this.links = new LinkSet();
   }

   public int tier() {
      Block var2 = this.getBlockState().getBlock();
      int var10000;
      if (var2 instanceof ReceiverBlock b) {
         var10000 = b.tier.level;
      } else {
         var10000 = 1;
      }

      return var10000;
   }

   public LinkSet links() {
      return this.links;
   }

   public BlockPos nodePos() {
      return this.worldPosition;
   }

   public int maxLinks(int slot) {
      return 8;
   }

   public Vec3 anchor(int slot) {
      return CableNode.facing(this, JACK[this.tier() - 1][0], JACK[this.tier() - 1][1], JACK[this.tier() - 1][2]);
   }

   public void cablesChanged() {
      this.sync();
   }

   public void onLoad() {
      super.onLoad();
      if (this.level != null && this.level.isClientSide) {
         ClientSinks.SINKS.add(this);
      }

   }

   public void setRemoved() {
      ClientSinks.removed(this, this);
      super.setRemoved();
   }

   public void refresh() {
      if (this.level != null) {
         BlockState st = this.getBlockState();
         if (st.getBlock() instanceof ReceiverBlock) {
            Direction f = (Direction)st.getValue(ReceiverBlock.FACING);
            BlockPos back = this.worldPosition.relative(f.getOpposite());
            boolean copper = this.level.isLoaded(back) && this.level.getBlockState(back).is(ModRegistry.GROUND_PLANE);
            AntennaUtil.Info onCopper = copper ? AntennaUtil.scan(this.level, back.above(), this.tier()) : AntennaUtil.Info.NONE;
            AntennaUtil.Info onTop = AntennaUtil.scan(this.level, this.worldPosition.above(), this.tier());
            int oldCr = this.crystal;
            int oldAnt = this.antennaHeight;
            boolean oldOk = this.ok;
            boolean oldGnd = this.grounded;
            int t = this.tier();
            if (onCopper.height() > 0 && onCopper.height() >= onTop.height()) {
               this.info = onCopper;
               this.grounded = true;
            } else if (onTop.height() > 0) {
               this.info = onTop;
               this.grounded = false;
            } else {
               this.info = AntennaUtil.Info.NONE;
               this.grounded = false;
            }

            this.antennaHeight = this.info.height();
            this.crystal = (Integer)st.getValue(ReceiverBlock.CRYSTAL);
            this.ok = this.antennaHeight >= 1 && this.crystal > 0;
            if (oldCr != this.crystal || oldAnt != this.antennaHeight || oldOk != this.ok || oldGnd != this.grounded) {
               this.sync();
            }

            if ((Boolean)st.getValue(ReceiverBlock.ACTIVE) != this.ok) {
               this.level.playSound((Player)null, this.worldPosition, this.ok ? (SoundEvent)ModSounds.POWER_ON.get() : (SoundEvent)ModSounds.POWER_OFF.get(), SoundSource.BLOCKS, 0.4F, 1.15F);
               this.level.setBlock(this.worldPosition, (BlockState)st.setValue(ReceiverBlock.ACTIVE, this.ok), 3);
            }

         }
      }
   }

   public double rxGain() {
      double roof = (double)1.0F;
      return AntennaUtil.rxGain(this.info) * (this.grounded ? (double)1.25F : (double)1.0F) * roof * (this.crystal == 2 ? 1.15 : (double)1.0F);
   }

   public Float rxBeam() {
      return this.info.beam();
   }

   public double antennaTop() {
      return super.antennaTop() + (double)0.5F + (double)this.antennaHeight;
   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      super.addToGoggleTooltip(tip, sneaking);
      int cr = this.getBlockState().getBlock() instanceof ReceiverBlock ? (Integer)this.getBlockState().getValue(ReceiverBlock.CRYSTAL) : this.crystal;
      tip.add(Tuned.chamberLine(cr));
      if (cr == 0) {
         tip.add(Component.literal("    no amethyst in the chamber").withStyle(ChatFormatting.GOLD));
      } else if (!this.ok) {
         tip.add(Component.literal("    needs an antenna").withStyle(ChatFormatting.GOLD));
      } else {
         int var10001 = this.antennaHeight;
         tip.add(Component.literal("    antenna " + var10001 + " tall" + (this.grounded ? ", grounded" : "") + (this.links.isEmpty() ? ", built-in speaker" : "")).withStyle(ChatFormatting.GRAY));
      }

      return true;
   }

   public boolean sinkOn() {
      return this.ok;
   }

   public boolean ambientOn() {
      return this.ok && this.links.isEmpty();
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

   public int signal() {
      return this.ok && this.carrier ? 1 + Math.round(this.quality * 14.0F) : 0;
   }

   protected void afterSignal() {
      Level var2 = this.level;
      if (var2 instanceof ServerLevel lv) {
         for(SpeakerBE s : this.speakers) {
            s.setState(this.ok, this.carrier, this.quality, this.tuning.fm());
         }

         int sig = this.signal();
         if (sig != this.lastSig) {
            this.lastSig = sig;
            lv.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         }

      }
   }

   public void deliver(ServerLevel lv, UUID talker, byte[] data, float gain) {
      if (this.speakers.isEmpty()) {
         RadioNetwork.send(lv, this.worldPosition, talker, data, this.quality * 0.92F, this.tuning.fm(), gain * 0.6F);
      } else {
         for(SpeakerBE s : this.speakers) {
            RadioNetwork.send(lv, s.getBlockPos(), talker, data, this.quality * (Float)this.lineQ.getOrDefault(s, 1.0F), this.tuning.fm(), gain);
         }

      }
   }

   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.saveAdditional(tag, r);
      this.links.save(tag);
      tag.putBoolean("ok", this.ok);
      tag.putInt("ant", this.antennaHeight);
      tag.putBoolean("gnd", this.grounded);
      tag.putInt("cr", this.crystal);
   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.loadAdditional(tag, r);
      this.links.load(tag);
      this.ok = tag.getBoolean("ok");
      this.antennaHeight = tag.getInt("ant");
      this.grounded = tag.getBoolean("gnd");
      this.crystal = tag.getInt("cr");
   }
}
