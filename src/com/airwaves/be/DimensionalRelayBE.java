package com.airwaves.be;

import com.airwaves.Config;
import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.block.DimensionalRelayBlock;
import com.airwaves.compat.SableCompat;
import com.airwaves.logic.AntennaUtil;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.power.PowerSupply;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DimensionalRelayBE extends KineticBlockEntity {
   public static final double STRESS = (double)16.0F;
   public final PowerSupply power = new PowerSupply(() -> (double)16.0F, () -> 120);
   public boolean active;
   public int sending;
   public int bringing;
   public List<String> linked = new ArrayList();
   private boolean forced;

   public DimensionalRelayBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.DIM_RELAY_BE.get(), p, s);
   }

   public BlockPos radioPos() {
      return SableCompat.globalPos(this.level, this.worldPosition);
   }

   public double antennaTop() {
      return SableCompat.global(this.level, this.worldPosition).y + (double)2.0F;
   }

   public double range(boolean fm) {
      double f = this.power.source() == PowerSupply.Source.ELECTRIC ? this.power.electricFactor() : (!Config.rotation() ? (double)1.0F : (double)0.5F + (double)0.5F * Math.min((double)2.0F, (double)(Math.abs(this.getSpeed()) / (float)(Integer)Config.FULL_RPM.get())));
      return AntennaUtil.range(new AntennaUtil.Info(16, 2), fm) * 1.2 * f;
   }

   public double rxGain() {
      return AntennaUtil.rxGain(new AntennaUtil.Info(16, 2)) * 1.2;
   }

   public boolean rotationOk() {
      return Math.abs(this.getSpeed()) >= (float)(Integer)Config.MIN_RPM.get() && !this.isOverStressed();
   }

   public void refresh() {
      if (this.level != null) {
         if (Config.rotation()) {
            this.power.update(this.rotationOk(), this.level.getGameTime());
         }

         boolean was = this.active;
         this.active = (!Config.rotation() || this.power.running()) && !this.level.hasNeighborSignal(this.worldPosition);
         if (!this.active) {
            this.power.refund();
         }

         BlockState st = this.getBlockState();
         if (st.hasProperty(DimensionalRelayBlock.ACTIVE) && (Boolean)st.getValue(DimensionalRelayBlock.ACTIVE) != this.active) {
            this.level.setBlock(this.worldPosition, (BlockState)st.setValue(DimensionalRelayBlock.ACTIVE, this.active), 3);
            this.level.playSound((Player)null, this.worldPosition, this.active ? (SoundEvent)ModSounds.POWER_ON.get() : (SoundEvent)ModSounds.POWER_OFF.get(), SoundSource.BLOCKS, 0.7F, 0.6F);
         }

         this.keepLoaded((Boolean)Config.DIM_RELAY_CHUNK.get());
         if (was != this.active || this.level.getGameTime() % 40L == 0L) {
            this.sendData();
         }

      }
   }

   public void keepLoaded(boolean on) {
      Level var3 = this.level;
      if (var3 instanceof ServerLevel sl) {
         if (this.forced != on) {
            ChunkPos c = new ChunkPos(this.worldPosition);
            sl.setChunkForced(c.x, c.z, on);
            this.forced = on;
            this.setChanged();
            return;
         }
      }

   }

   public void initialize() {
      super.initialize();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.register(this);
      }

   }

   public void invalidate() {
      super.invalidate();
      if (this.level != null && !this.level.isClientSide) {
         RadioNetwork.unregister(this);
      }

   }

   public boolean addToGoggleTooltip(List<Component> tip, boolean sneaking) {
      super.addToGoggleTooltip(tip, sneaking);
      tip.add(Component.literal("    Dimensional Relay").withStyle(ChatFormatting.WHITE));
      if (!this.active) {
         boolean red = this.level != null && this.level.hasNeighborSignal(this.worldPosition);
         tip.add(Component.literal(red ? "    off (redstone)" : "    needs power").withStyle(ChatFormatting.GOLD));
      } else if (this.linked.isEmpty()) {
         tip.add(Component.literal("    no partner: build another one").withStyle(ChatFormatting.GOLD));
         tip.add(Component.literal("    in the Nether or the End").withStyle(ChatFormatting.GOLD));
      } else {
         String var10001 = String.join(", ", this.linked);
         tip.add(Component.literal("    linked to " + var10001).withStyle(ChatFormatting.LIGHT_PURPLE));
         tip.add(Component.literal("    sending " + this.sending + " station" + (this.sending == 1 ? "" : "s") + ", bringing in " + this.bringing).withStyle(ChatFormatting.GREEN));
      }

      if (Config.rotation()) {
         this.power.tooltip(tip, this.level == null || !this.level.hasNeighborSignal(this.worldPosition), this.isOverStressed(), Math.abs(this.getSpeed()));
      }

      return true;
   }

   protected void write(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.write(tag, r, clientPacket);
      this.power.save(tag);
      tag.putBoolean("forced", this.forced);
      if (clientPacket) {
         tag.putBoolean("active", this.active);
         tag.putInt("sending", this.sending);
         tag.putInt("bringing", this.bringing);
         ListTag l = new ListTag();

         for(String s : this.linked) {
            l.add(StringTag.valueOf(s));
         }

         tag.put("linked", l);
      }

   }

   protected void read(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.read(tag, r, clientPacket);
      this.power.load(tag);
      this.forced = tag.getBoolean("forced");
      if (clientPacket) {
         this.active = tag.getBoolean("active");
         this.sending = tag.getInt("sending");
         this.bringing = tag.getInt("bringing");
         this.linked = new ArrayList();

         for(Tag t : tag.getList("linked", 8)) {
            this.linked.add(t.getAsString());
         }
      }

   }
}
