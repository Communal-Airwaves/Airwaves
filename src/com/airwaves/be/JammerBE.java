package com.airwaves.be;

import com.airwaves.Config;
import com.airwaves.ModRegistry;
import com.airwaves.block.JammerBlock;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.power.PowerSupply;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class JammerBE extends KineticBlockEntity implements CableNode {
   public static final double STRESS = (double)8.0F;
   private final LinkSet links = new LinkSet();
   private boolean running;
   public final PowerSupply power = new PowerSupply(() -> (double)8.0F, () -> 24);

   public JammerBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.JAMMER_BE.get(), p, s);
   }

   public boolean isActive() {
      return this.running;
   }

   public boolean rotationOk() {
      return Math.abs(this.getSpeed()) >= (float)(Integer)Config.MIN_RPM.get() && !this.isOverStressed();
   }

   public boolean turning() {
      return !Config.rotation() || this.power.running();
   }

   public boolean refresh() {
      boolean on = (Boolean)this.getBlockState().getValue(JammerBlock.POWERED);
      boolean was = this.running;
      boolean changed = Config.rotation() && this.power.update(this.rotationOk(), this.level == null ? 0L : this.level.getGameTime());
      this.running = on && this.turning();
      if (!this.running) {
         this.power.refund();
      }

      if (was != this.running || changed) {
         this.sendData();
      }

      return this.running;
   }

   public LinkSet links() {
      return this.links;
   }

   public BlockPos nodePos() {
      return this.worldPosition;
   }

   public int maxLinks(int slot) {
      return 2;
   }

   public Vec3 anchor(int slot) {
      return CableNode.facing(this, (double)8.0F, (double)6.0F, (double)14.0F);
   }

   public void cablesChanged() {
      this.setChanged();
      this.sendData();
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
      tip.add(Component.literal("    Signal Jammer").withStyle(ChatFormatting.WHITE));
      boolean on = (Boolean)this.getBlockState().getValue(JammerBlock.POWERED);
      tip.add(Component.literal(this.running ? "    jamming 48 blocks around" : (!on ? "    off (needs a redstone signal)" : "    needs power")).withStyle(this.running ? ChatFormatting.RED : ChatFormatting.GOLD));
      if (Config.rotation()) {
         this.power.tooltip(tip, on, this.isOverStressed(), Math.abs(this.getSpeed()));
      }

      return true;
   }

   protected void write(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.write(tag, r, clientPacket);
      this.links.save(tag);
      tag.putBoolean("running", this.running);
      this.power.save(tag);
   }

   protected void read(CompoundTag tag, HolderLookup.Provider r, boolean clientPacket) {
      super.read(tag, r, clientPacket);
      this.links.load(tag);
      this.running = tag.getBoolean("running");
      this.power.load(tag);
   }
}
