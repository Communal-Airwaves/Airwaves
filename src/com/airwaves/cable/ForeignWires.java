package com.airwaves.cable;

import com.airwaves.Config;
import com.airwaves.item.CableItem;
import com.airwaves.power.Terminals;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ForeignWires {
   private ForeignWires() {
   }

   public static CableType asCable(ItemStack st) {
      if (!st.isEmpty() && !(st.getItem() instanceof CableItem)) {
         ResourceLocation id = BuiltInRegistries.ITEM.getKey(st.getItem());
         String ns = id.getNamespace();
         String p = id.getPath();
         boolean pg = ns.equals("powergrid") && Config.powerGridLoaded();
         boolean cee = ns.equals("electroenergetics") && Config.ceeLoaded();
         if (!pg && !cee) {
            return null;
         } else if (!p.contains("wire") && !p.contains("cord")) {
            return null;
         } else {
            return !p.contains("strand") && !p.contains("empty") && !p.contains("duplex") && !p.contains("string_light") ? CableType.COPPER : null;
         }
      } else {
         return null;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void rightClick(PlayerInteractEvent.RightClickBlock e) {
      CableType type = asCable(e.getItemStack());
      if (type != null && e.getEntity() != null) {
         Level lv = e.getLevel();
         if (CableItem.refAt(lv, e.getPos(), e.getHitVec().getLocation()) != null) {
            BlockState st = lv.getBlockState(e.getPos());
            Vec3 local = e.getHitVec().getLocation().subtract((double)e.getPos().getX(), (double)e.getPos().getY(), (double)e.getPos().getZ());
            if (!Terminals.has(st) || Terminals.at(st, local) < 0) {
               InteractionResult r = CableItem.use(new UseOnContext(e.getEntity(), e.getHand(), e.getHitVec()), type);
               if (r != InteractionResult.PASS) {
                  e.setCanceled(true);
                  e.setCancellationResult(r);
               }

            }
         }
      }
   }
}
