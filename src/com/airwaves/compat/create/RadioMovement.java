package com.airwaves.compat.create;

import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import com.airwaves.block.RadioBlock;
import com.airwaves.block.ReceiverBlock;
import com.airwaves.block.TransmitterBlock;
import com.airwaves.logic.MobileRadios;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

public class RadioMovement implements MovementBehaviour {
   private final int kind;

   private RadioMovement(int kind) {
      this.kind = kind;
   }

   public static void register() {
      reg((Block)ModRegistry.RADIO.get(), 1);

      for(DeferredBlock<ReceiverBlock> r : ModRegistry.RECEIVERS) {
         reg((Block)r.get(), 2);
      }

      reg((Block)ModRegistry.CREATIVE_RECEIVER.get(), 2);
      reg((Block)ModRegistry.RADIO_CONSOLE.get(), 3);
      reg((Block)ModRegistry.SPEAKER.get(), 4);

      for(DeferredBlock<TransmitterBlock> t : ModRegistry.TRANSMITTERS) {
         reg((Block)t.get(), 5);
      }

      reg((Block)ModRegistry.CREATIVE_TRANSMITTER.get(), 5);
      reg((Block)ModRegistry.MICROPHONE.get(), 6);
   }

   private static void reg(Block b, int kind) {
      MovementBehaviour.REGISTRY.register(b, new RadioMovement(kind));
   }

   private static UUID id(MovementContext c) {
      if (!c.data.hasUUID("airwaves_id")) {
         c.data.putUUID("airwaves_id", UUID.randomUUID());
      }

      return c.data.getUUID("airwaves_id");
   }

   public boolean mustTickWhileDisabled() {
      return true;
   }

   public @Nullable ItemStack canBeDisabledVia(MovementContext context) {
      return null;
   }

   public void tick(MovementContext c) {
      Level var3 = c.world;
      if (var3 instanceof ServerLevel sl) {
         if (c.position != null && c.contraption != null && c.contraption.entity != null) {
            MobileRadios.Mobile m = MobileRadios.update(id(c), this.kind, sl, c.position, c.contraption.entity.getId());
            CompoundTag be = c.blockEntityData == null ? new CompoundTag() : c.blockEntityData;
            if (be.contains("freq")) {
               m.tuning = (new Tuning(be.getBoolean("fm"), be.getDouble("freq"))).clamped();
            }

            BlockState s = c.state;
            switch (this.kind) {
               case 1:
                  m.on = s.hasProperty(RadioBlock.POWERED) && (Boolean)s.getValue(RadioBlock.POWERED);
                  m.rxGain = 0.8;
                  break;
               case 2:
                  boolean creative = s.is((Block)ModRegistry.CREATIVE_RECEIVER.get());
                  m.on = creative || s.hasProperty(ReceiverBlock.CRYSTAL) && (Integer)s.getValue(ReceiverBlock.CRYSTAL) > 0;
                  m.rxGain = creative ? (double)1000000.0F : 1.2;
                  break;
               case 3:
                  m.on = true;
                  m.rxGain = (double)1.0F;
                  break;
               case 4:
               default:
                  m.on = true;
                  break;
               case 5:
                  boolean creative = s.is((Block)ModRegistry.CREATIVE_TRANSMITTER.get());
                  m.on = creative || be.getBoolean("active");
                  m.range = creative ? (double)1.0E8F : be.getDouble("range");
                  if (m.range <= (double)0.0F && m.on) {
                     m.range = (double)300.0F;
                  }
            }

            return;
         }
      }

   }

   public void stopMoving(MovementContext c) {
      if (c.data.hasUUID("airwaves_id")) {
         MobileRadios.remove(c.data.getUUID("airwaves_id"));
      }

   }
}
