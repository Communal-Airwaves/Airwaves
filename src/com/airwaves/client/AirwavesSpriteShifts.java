package com.airwaves.client;

import com.airwaves.ModRegistry;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class AirwavesSpriteShifts {
   public static final CTSpriteShiftEntry RADIO_CASING;

   private AirwavesSpriteShifts() {
   }

   public static void init(IEventBus modBus) {
      modBus.addListener(EventPriority.LOWEST, RegisterEvent.class, (e) -> {
         if (e.getRegistryKey().equals(Registries.BLOCK)) {
            CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(RADIO_CASING)).accept(ModRegistry.RADIO_CASING.get());
         }
      });
   }

   static {
      RADIO_CASING = CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, ResourceLocation.fromNamespaceAndPath("airwaves", "block/radio_casing"), ResourceLocation.fromNamespaceAndPath("airwaves", "block/radio_casing_connected"));
   }
}
