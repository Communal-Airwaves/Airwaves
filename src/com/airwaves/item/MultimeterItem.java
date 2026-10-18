package com.airwaves.item;

import com.airwaves.Config;
import com.airwaves.ModSounds;
import com.airwaves.be.CableNode;
import com.airwaves.be.JammerBE;
import com.airwaves.be.KineticTunableBE;
import com.airwaves.be.RelayBE;
import com.airwaves.be.TransmitterBE;
import com.airwaves.be.TunableBE;
import com.airwaves.be.Tuned;
import com.airwaves.cable.Link;
import com.airwaves.power.PowerSupply;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MultimeterItem extends Item {
   public MultimeterItem(Item.Properties p) {
      super(p);
   }

   public InteractionResult useOn(UseOnContext ctx) {
      Level lv = ctx.getLevel();
      if (!lv.isClientSide && ctx.getPlayer() != null) {
         BlockPos pos = CableItem.nodePos(lv, ctx.getClickedPos());
         BlockEntity be = lv.getBlockEntity(pos);
         if (be == null) {
            return InteractionResult.PASS;
         } else {
            MutableComponent out = Component.empty();
            if (be instanceof KineticBlockEntity) {
               KineticBlockEntity k = (KineticBlockEntity)be;
               int var10001 = Math.round(Math.abs(k.getSpeed()));
               out.append(Component.literal("⟳ " + var10001 + " RPM" + (k.isOverStressed() ? " overstressed" : "")).withStyle(k.isOverStressed() ? ChatFormatting.RED : ChatFormatting.YELLOW));
            }

            PowerSupply var10000;
            if (be instanceof KineticTunableBE) {
               KineticTunableBE kt = (KineticTunableBE)be;
               var10000 = kt.power;
            } else if (be instanceof JammerBE) {
               JammerBE j = (JammerBE)be;
               var10000 = j.power;
            } else {
               var10000 = null;
            }

            PowerSupply ps = var10000;
            if (ps != null && Config.electricEnabled()) {
               String var18 = ps.summary();
               out.append(Component.literal("   ⚡ " + var18 + (ps.wired() ? ", bank " + Math.round(ps.storedJoules()) + " J" : "")).withStyle(ps.source() == PowerSupply.Source.ELECTRIC ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            }

            if (be instanceof CableNode) {
               CableNode n = (CableNode)be;
               if (!n.links().isEmpty()) {
                  double loss = (double)0.0F;

                  for(Link l : n.links().list()) {
                     loss = Math.max(loss, l.type().loss);
                  }

                  int var19 = n.links().size();
                  out.append(Component.literal("   ↔ " + var19 + " cable" + (n.links().size() == 1 ? "" : "s")).withStyle(ChatFormatting.GOLD));
               }
            }

            if (be instanceof Tuned) {
               Tuned t = (Tuned)be;
               out.append(Component.literal("   " + t.getTuning().display()).withStyle(ChatFormatting.AQUA));
               if (be instanceof TransmitterBE) {
                  TransmitterBE tx = (TransmitterBE)be;
                  int var20 = (int)tx.range;
                  out.append(Component.literal("   ▲ " + var20 + " m").withStyle(tx.active ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
               } else if (be instanceof RelayBE) {
                  RelayBE r = (RelayBE)be;
                  int var21 = (int)r.range;
                  out.append(Component.literal("   ▲ " + var21 + " m").withStyle(r.relaying ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
               } else if (be instanceof TunableBE) {
                  TunableBE tb = (TunableBE)be;
                  int var22 = Math.round(tb.quality * 100.0F);
                  out.append(Component.literal("   ▮ " + var22 + "%").withStyle(tb.carrier ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
               }
            }

            if (out.getSiblings().isEmpty()) {
               return InteractionResult.PASS;
            } else {
               ctx.getPlayer().displayClientMessage(out, true);
               lv.playSound((Player)null, pos, (SoundEvent)ModSounds.PROBE.get(), SoundSource.PLAYERS, 0.3F, 2.0F);
               return InteractionResult.CONSUME;
            }
         }
      } else {
         return InteractionResult.SUCCESS;
      }
   }
}
