package com.airwaves.client;

import com.airwaves.compat.SableCompat;
import com.airwaves.net.RadioSoundPayload;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class RadioSounds {
   private static final RandomSource RNG = RandomSource.create();
   private static final long TIMEOUT_MS = 3500L;
   private static final double MUSIC_RANGE = (double)72.0F;
   private static final Map<String, Track> MUSIC = new HashMap();

   private RadioSounds() {
   }

   public static void play(RadioSoundPayload p) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         String var10000 = p.handheld() ? "h" : Long.toString(p.sink().asLong());
         String key = var10000 + ":" + p.key() + ":" + String.valueOf(p.sound());
         if (p.kind() == 2) {
            stop(key);
         } else if (VoiceConfig.deafened) {
            stop(key);
         } else {
            float q = p.quality();
            double x = (double)0.0F;
            double y = (double)0.0F;
            double z = (double)0.0F;
            Vec3 at = null;
            if (!p.handheld()) {
               at = SableCompat.rendered(Vec3.atCenterOf(p.sink()), 1.0F);
               x = at.x;
               y = at.y;
               z = at.z;
            }

            if (p.kind() == 1) {
               music(mc, p, key, at, q);
            } else if (!(q < 0.04F)) {
               if (at == null || !(mc.player.getEyePosition().distanceTo(at) > (double)32.0F)) {
                  if (!(RNG.nextFloat() > 0.3F + q)) {
                     SimpleSoundInstance inst = new SimpleSoundInstance(p.sound(), SoundSource.BLOCKS, volume(p, q), pitch(p, q), RNG, false, 0, p.handheld() ? Attenuation.NONE : Attenuation.LINEAR, x, y, z, p.handheld());
                     mc.getSoundManager().play(inst);
                     if (q < 0.55F) {
                        AudioPlayback.burst(0.12F * (1.0F - q));
                     }

                  }
               }
            }
         }
      }
   }

   private static void music(Minecraft mc, RadioSoundPayload p, String key, Vec3 at, float q) {
      Track t = (Track)MUSIC.get(key);
      boolean inRange = q >= 0.04F && (at == null || mc.player.getEyePosition().distanceTo(at) <= (double)72.0F);
      if (!inRange) {
         stop(key);
      } else if (t != null) {
         t.seen = System.currentTimeMillis();
      } else {
         t = new Track();
         t.seen = System.currentTimeMillis();
         t.at = at;
         t.inst = new SeekingSound(p.sound(), volume(p, q), pitch(p, q), at, p.handheld(), p.offsetMs());
         MUSIC.put(key, t);
         mc.getSoundManager().play(t.inst);
         if (q < 0.55F) {
            AudioPlayback.burst(0.12F * (1.0F - q));
         }

      }
   }

   private static float volume(RadioSoundPayload p, float q) {
      return Math.max(0.02F, p.volume() * VoiceConfig.radio * (0.35F + 0.65F * q));
   }

   private static float pitch(RadioSoundPayload p, float q) {
      return p.pitch() * (p.fm() ? 1.0F : 1.0F + (RNG.nextFloat() - 0.5F) * 0.05F * (1.0F - q));
   }

   private static void stop(String key) {
      Track old = (Track)MUSIC.remove(key);
      if (old != null) {
         Minecraft.getInstance().getSoundManager().stop(old.inst);
      }

   }

   public static void tick(Minecraft mc) {
      if (mc.level != null && mc.player != null) {
         long now = System.currentTimeMillis();
         Vec3 eye = mc.player.getEyePosition();
         Iterator<Map.Entry<String, Track>> it = MUSIC.entrySet().iterator();

         while(it.hasNext()) {
            Track t = (Track)((Map.Entry)it.next()).getValue();
            if (now - t.seen > 3500L || t.at != null && eye.distanceTo(t.at) > (double)80.0F) {
               mc.getSoundManager().stop(t.inst);
               it.remove();
            }
         }

      } else {
         stopAll();
      }
   }

   public static void stopAt(BlockPos pos) {
      String prefix = pos.asLong() + ":";
      Minecraft mc = Minecraft.getInstance();
      Iterator<Map.Entry<String, Track>> it = MUSIC.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<String, Track> e = (Map.Entry)it.next();
         if (((String)e.getKey()).startsWith(prefix)) {
            mc.getSoundManager().stop(((Track)e.getValue()).inst);
            it.remove();
         }
      }

      AudioPlayback.stopSink(pos);
   }

   public static void stopAll() {
      Minecraft mc = Minecraft.getInstance();

      for(Track t : MUSIC.values()) {
         mc.getSoundManager().stop(t.inst);
      }

      MUSIC.clear();
   }

   private static final class Track {
      SoundInstance inst;
      long seen;
      Vec3 at;
   }

   private static final class SeekingSound extends SimpleSoundInstance {
      private final int offsetMs;

      SeekingSound(ResourceLocation id, float vol, float pitch, Vec3 at, boolean handheld, int offsetMs) {
         super(id, SoundSource.RECORDS, vol, pitch, RadioSounds.RNG, false, 0, handheld ? Attenuation.NONE : Attenuation.LINEAR, at == null ? (double)0.0F : at.x, at == null ? (double)0.0F : at.y, at == null ? (double)0.0F : at.z, handheld);
         this.offsetMs = offsetMs;
      }

      public CompletableFuture<AudioStream> getStream(SoundBufferLibrary lib, Sound sound, boolean looping) {
         CompletableFuture<AudioStream> f = lib.getStream(sound.getPath(), looping);
         return this.offsetMs < 250 ? f : f.thenApply((s) -> {
            skip(s, this.offsetMs);
            return s;
         });
      }

      private static void skip(AudioStream s, int ms) {
         try {
            AudioFormat fmt = s.getFormat();

            ByteBuffer b;
            for(long bytes = (long)((double)ms / (double)1000.0F * (double)fmt.getSampleRate()) * (long)fmt.getChannels() * (long)Math.max(1, fmt.getSampleSizeInBits() / 8); bytes > 0L; bytes -= (long)b.remaining()) {
               b = s.read((int)Math.min(bytes, 32768L));
               if (b == null || b.remaining() == 0) {
                  break;
               }
            }
         } catch (RuntimeException | IOException var6) {
         }

      }
   }
}
