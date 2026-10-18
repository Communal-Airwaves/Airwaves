package com.airwaves.item;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.be.CableNode;
import com.airwaves.be.MicrophoneBE;
import com.airwaves.be.ReceiverBE;
import com.airwaves.be.SpeakerBE;
import com.airwaves.be.TransmitterBE;
import com.airwaves.cable.CableType;
import com.airwaves.cable.NodeRef;
import com.airwaves.compat.ElectricCompat;
import com.airwaves.logic.Cables;
import com.airwaves.structure.StructureBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CableItem extends Item {
   public static final int AUTO_R = 12;
   public final CableType type;

   public CableItem(CableType type, Item.Properties p) {
      super(p);
      this.type = type;
   }

   public static BlockPos nodePos(Level lv, BlockPos clicked) {
      BlockState s = lv.getBlockState(clicked);
      Block var4 = s.getBlock();
      if (var4 instanceof StructureBlock sb) {
         return sb.corePos(s, clicked);
      } else {
         return clicked;
      }
   }

   public static NodeRef refAt(Level lv, BlockPos clicked, Vec3 hit) {
      BlockPos p = nodePos(lv, clicked);
      CableNode n = Cables.node(lv, p);
      return n == null ? null : new NodeRef(p, n.slotAt(hit));
   }

   public static NodeRef start(ItemStack st) {
      BlockPos p = (BlockPos)st.get((DataComponentType)ModRegistry.WIRE_START.get());
      return p == null ? null : new NodeRef(p, (Integer)st.getOrDefault((DataComponentType)ModRegistry.WIRE_SLOT.get(), 0));
   }

   public InteractionResult useOn(UseOnContext ctx) {
      InteractionResult r = ElectricCompat.ceeWire(ctx, this.type);
      return r != InteractionResult.PASS ? r : use(ctx, this.type);
   }

   public static InteractionResult use(UseOnContext ctx, CableType type) {
      Level lv = ctx.getLevel();
      Player pl = ctx.getPlayer();
      NodeRef here = refAt(lv, ctx.getClickedPos(), ctx.getClickLocation());
      if (pl != null && here != null) {
         if (lv.isClientSide) {
            return InteractionResult.SUCCESS;
         } else {
            ItemStack st = ctx.getItemInHand();
            NodeRef start = start(st);
            CableNode node = Cables.node(lv, here.pos());
            if (start == null) {
               if (!pl.isShiftKeyDown()) {
                  if (!node.accepts(here.slot(), type)) {
                     lv.playSound((Player)null, here.pos(), (SoundEvent)ModSounds.CABLE_REJECT.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                     return InteractionResult.CONSUME;
                  } else {
                     st.set((DataComponentType)ModRegistry.WIRE_START.get(), here.pos());
                     st.set((DataComponentType)ModRegistry.WIRE_SLOT.get(), here.slot());
                     lv.playSound((Player)null, here.pos(), (SoundEvent)ModSounds.CABLE_GRAB.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                     return InteractionResult.CONSUME;
                  }
               } else {
                  BlockEntity be = lv.getBlockEntity(here.pos());
                  if (type != CableType.COPPER || !node.links().isEmpty() || !(be instanceof TransmitterBE) && !(be instanceof ReceiverBE)) {
                     List<ItemStack> drops = Cables.unlink(lv, here.pos(), node.connectors() > 1 ? here.slot() : -1);

                     for(ItemStack d : drops) {
                        lv.addFreshEntity(new ItemEntity(lv, pl.getX(), pl.getY() + (double)0.5F, pl.getZ(), d));
                     }

                     if (!drops.isEmpty()) {
                        lv.playSound((Player)null, here.pos(), (SoundEvent)ModSounds.CABLE_DETACH.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
                     }
                  } else {
                     autoWire(lv, here.pos(), be, pl, st, type);
                  }

                  return InteractionResult.CONSUME;
               }
            } else if (!start.equals(here) && !pl.isShiftKeyDown()) {
               if (Cables.link(lv, start, here, type)) {
                  clear(st);
                  lv.playSound((Player)null, here.pos(), type.light ? (SoundEvent)ModSounds.CABLE_ATTACH.get() : (SoundEvent)ModSounds.CABLE_HEAVY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                  if (!pl.getAbilities().instabuild) {
                     st.shrink(1);
                  }
               } else {
                  lv.playSound((Player)null, here.pos(), (SoundEvent)ModSounds.CABLE_REJECT.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
               }

               return InteractionResult.CONSUME;
            } else {
               clear(st);
               lv.playSound((Player)null, here.pos(), (SoundEvent)ModSounds.CABLE_REJECT.get(), SoundSource.BLOCKS, 0.4F, 1.2F);
               return InteractionResult.CONSUME;
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static void clear(ItemStack st) {
      st.remove((DataComponentType)ModRegistry.WIRE_START.get());
      st.remove((DataComponentType)ModRegistry.WIRE_SLOT.get());
   }

   private static void autoWire(Level lv, BlockPos at, BlockEntity hub, Player pl, ItemStack st, CableType type) {
      int n = 0;

      for(BlockPos q : BlockPos.betweenClosed(at.offset(-12, -6, -12), at.offset(12, 6, 12))) {
         BlockEntity be = lv.getBlockEntity(q);
         boolean want = hub instanceof TransmitterBE && be instanceof MicrophoneBE || hub instanceof ReceiverBE && be instanceof SpeakerBE;
         if (want && ((CableNode)be).links().isEmpty()) {
            if (!pl.getAbilities().instabuild && st.isEmpty()) {
               break;
            }

            if (Cables.link(lv, new NodeRef(at, 0), new NodeRef(q.immutable(), 0), type)) {
               ++n;
               if (!pl.getAbilities().instabuild) {
                  st.shrink(1);
               }
            }
         }
      }

      lv.playSound((Player)null, at, n > 0 ? (SoundEvent)ModSounds.CABLE_ATTACH.get() : (SoundEvent)ModSounds.CABLE_REJECT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
   }
}
