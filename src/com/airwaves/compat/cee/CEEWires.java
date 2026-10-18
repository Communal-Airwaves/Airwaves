package com.airwaves.compat.cee;

import com.airwaves.cable.CableType;
import com.airwaves.power.Terminals;
import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEWireTypes;
import com.george_vi.electroenergetics.foundation.device.ElectricalDeviceBlock;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.WireType;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class CEEWires {
   private CEEWires() {
   }

   private static WireType wireFor(CableType t) {
      return (WireType)CEEWireTypes.COPPER.get();
   }

   public static InteractionResult use(UseOnContext ctx, CableType type) {
      Level level = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      Player player = ctx.getPlayer();
      ItemStack held = ctx.getItemInHand();
      BlockState state = level.getBlockState(pos);
      Block var8 = state.getBlock();
      if (!(var8 instanceof ElectricalDeviceBlock<?> db)) {
         return InteractionResult.PASS;
      } else {
         boolean var18 = held.has(CEEDataComponents.SELECTED_NODE);
         if (state.getBlock() instanceof CEEMachine && !var18) {
            Vec3 local = ctx.getClickLocation().subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            if (!Terminals.has(state) || Terminals.at(state, local) < 0) {
               return InteractionResult.PASS;
            }
         }

         if (player != null && player.isShiftKeyDown()) {
            if (!var18) {
               return InteractionResult.PASS;
            } else {
               held.remove(CEEDataComponents.SELECTED_NODE);
               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         } else {
            InWorldNode hovered = InWorldNode.closestNode(level, ctx.getClickLocation(), 1.5F, pos);
            if (hovered == null) {
               hovered = InWorldNode.closestNode(level, pos, state, 1.5F, ctx.getClickLocation());
            }

            if (!var18) {
               if (hovered == null) {
                  return InteractionResult.PASS;
               } else {
                  held.set(CEEDataComponents.SELECTED_NODE, hovered);
                  level.playSound((Player)null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.6F, 1.4F);
                  return InteractionResult.sidedSuccess(level.isClientSide);
               }
            } else if (!(level instanceof ServerLevel)) {
               return InteractionResult.SUCCESS;
            } else {
               ServerLevel sl = (ServerLevel)level;
               InWorldNode original = (InWorldNode)held.get(CEEDataComponents.SELECTED_NODE);
               WireType wire = wireFor(type);
               InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
               if (hovered != null && original != null && !hovered.equals(original) && (!hovered.sourcePos().equals(original.sourcePos()) || db.canSelfConnect(level, pos, state, hovered.id(), original.id())) && !sd.isConnected(hovered, original) && !(Math.sqrt(original.sableSourcePos(level).distSqr(hovered.sableSourcePos(level))) > (double)Math.max(wire.getMaxLength(), (float)type.maxLen))) {
                  BlockState os = level.getBlockState(original.sourcePos());
                  BlockState hs = level.getBlockState(hovered.sourcePos());
                  Block var17 = os.getBlock();
                  if (var17 instanceof ElectricalDeviceBlock) {
                     ElectricalDeviceBlock<?> e1 = (ElectricalDeviceBlock)var17;
                     e1.ensureNodesExist(sl, original.sourcePos(), os);
                  }

                  var17 = hs.getBlock();
                  if (var17 instanceof ElectricalDeviceBlock) {
                     ElectricalDeviceBlock<?> e2 = (ElectricalDeviceBlock)var17;
                     e2.ensureNodesExist(sl, hovered.sourcePos(), hs);
                  }

                  if (sd.getNodes().contains(hovered) && sd.getNodes().contains(original)) {
                     sd.connect(original, hovered, wire);
                     level.playSound((Player)null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                     if (player == null || !player.isCreative()) {
                        held.shrink(1);
                     }

                     held.remove(CEEDataComponents.SELECTED_NODE);
                     return InteractionResult.SUCCESS;
                  } else {
                     return InteractionResult.FAIL;
                  }
               } else {
                  level.playSound((Player)null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 0.4F, 1.6F);
                  return InteractionResult.FAIL;
               }
            }
         }
      }
   }
}
