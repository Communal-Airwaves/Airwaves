package com.airwaves;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUNDS;
   public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_IN;
   public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL_OUT;
   public static final DeferredHolder<SoundEvent, SoundEvent> KNOB;
   public static final DeferredHolder<SoundEvent, SoundEvent> TUNE;
   public static final DeferredHolder<SoundEvent, SoundEvent> SEEK;
   public static final DeferredHolder<SoundEvent, SoundEvent> POWER_ON;
   public static final DeferredHolder<SoundEvent, SoundEvent> POWER_OFF;
   public static final DeferredHolder<SoundEvent, SoundEvent> CABLE_GRAB;
   public static final DeferredHolder<SoundEvent, SoundEvent> CABLE_ATTACH;
   public static final DeferredHolder<SoundEvent, SoundEvent> CABLE_HEAVY;
   public static final DeferredHolder<SoundEvent, SoundEvent> CABLE_DETACH;
   public static final DeferredHolder<SoundEvent, SoundEvent> CABLE_REJECT;
   public static final DeferredHolder<SoundEvent, SoundEvent> STRUCTURE;
   public static final DeferredHolder<SoundEvent, SoundEvent> SWITCH;
   public static final DeferredHolder<SoundEvent, SoundEvent> PROBE;
   public static final DeferredHolder<SoundEvent, SoundEvent> BOOK;

   private static DeferredHolder<SoundEvent, SoundEvent> s(String n) {
      return SOUNDS.register(n, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("airwaves", n)));
   }

   public static void register(IEventBus bus) {
      SOUNDS.register(bus);
   }

   private ModSounds() {
   }

   static {
      SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, "airwaves");
      CRYSTAL_IN = s("crystal_in");
      CRYSTAL_OUT = s("crystal_out");
      KNOB = s("knob");
      TUNE = s("tune");
      SEEK = s("seek");
      POWER_ON = s("power_on");
      POWER_OFF = s("power_off");
      CABLE_GRAB = s("cable_grab");
      CABLE_ATTACH = s("cable_attach");
      CABLE_HEAVY = s("cable_heavy");
      CABLE_DETACH = s("cable_detach");
      CABLE_REJECT = s("cable_reject");
      STRUCTURE = s("structure");
      SWITCH = s("switch");
      PROBE = s("probe");
      BOOK = s("book");
   }
}
