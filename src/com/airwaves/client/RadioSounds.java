package com.airwaves.client;

import com.airwaves.compat.SableCompat;
import com.airwaves.net.RadioSoundPayload;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class RadioSounds {
   private static final RandomSource RNG = RandomSource.create();
   private static final Map<String, SoundInstance> MUSIC = new HashMap();

   private RadioSounds() {
   }

   public static void play(RadioSoundPayload p) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         String var10000 = p.handheld() ? "h" : Long.toString(p.sink().asLong());
         String key = var10000 + ":" + p.key() + ":" + String.valueOf(p.sound());
         if (p.kind() == 2) {
            stop(key);
         } else if (!VoiceConfig.deafened) {
            float q = p.quality();
            if (!(q < 0.04F)) {
               double x = (double)0.0F;
               double y = (double)0.0F;
               double z = (double)0.0F;
               if (!p.handheld()) {
                  Vec3 c = SableCompat.rendered(Vec3.atCenterOf(p.sink()), 1.0F);
                  if (mc.player.getEyePosition().distanceTo(c) > (double)(p.kind() == 0 ? 32 : 72)) {
                     return;
                  }

                  x = c.x;
                  y = c.y;
                  z = c.z;
               }

               if (p.kind() != 0 || !(RNG.nextFloat() > 0.3F + q)) {
                  float vol = Math.max(0.02F, p.volume() * VoiceConfig.radio * (0.35F + 0.65F * q));
                  float pitch = p.pitch() * (p.fm() ? 1.0F : 1.0F + (RNG.nextFloat() - 0.5F) * 0.05F * (1.0F - q));
                  SoundInstance.Attenuation att = p.handheld() ? Attenuation.NONE : Attenuation.LINEAR;
                  SoundSource src = p.kind() == 0 ? SoundSource.BLOCKS : SoundSource.RECORDS;
                  SimpleSoundInstance inst = new SimpleSoundInstance(p.sound(), src, vol, pitch, RNG, false, 0, att, x, y, z, p.handheld());
                  if (p.kind() == 1) {
                     stop(key);
                     MUSIC.put(key, inst);
                  }

                  mc.getSoundManager().play(inst);
                  if (q < 0.55F) {
                     AudioPlayback.burst(0.12F * (1.0F - q));
                  }

               }
            }
         }
      }
   }

   private static void stop(String key) {
      SoundInstance old = (SoundInstance)MUSIC.remove(key);
      if (old != null) {
         Minecraft.getInstance().getSoundManager().stop(old);
      }

   }

   public static void tick(Minecraft mc) {
      if (mc.level == null) {
         stopAll();
      } else {
         Iterator<Map.Entry<String, SoundInstance>> it = MUSIC.entrySet().iterator();

         while(it.hasNext()) {
            if (!mc.getSoundManager().isActive((SoundInstance)((Map.Entry)it.next()).getValue())) {
               it.remove();
            }
         }

      }
   }

   public static void stopAll() {
      Minecraft mc = Minecraft.getInstance();

      for(SoundInstance s : MUSIC.values()) {
         mc.getSoundManager().stop(s);
      }

      MUSIC.clear();
   }
}
