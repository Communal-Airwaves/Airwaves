package com.airwaves.logic;

import com.airwaves.be.MicrophoneBE;
import com.airwaves.be.TapePlayerBE;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

public final class SoundCapture {
   public static boolean suppress;
   private static final Map<ResourceKey<Level>, Map<Long, Map<Long, Heard>>> HEARD = new HashMap();
   public static final int JUKEBOX_RANGE = 6;

   private SoundCapture() {
   }

   @SubscribeEvent
   public static void atPosition(PlayLevelSoundEvent.AtPosition e) {
      handle(e.getLevel(), e.getPosition(), e);
   }

   @SubscribeEvent
   public static void atEntity(PlayLevelSoundEvent.AtEntity e) {
      handle(e.getEntity().level(), e.getEntity().position(), e);
   }

   private static void handle(Level lv, Vec3 at, PlayLevelSoundEvent e) {
      if (!suppress && !e.isCanceled() && lv instanceof ServerLevel sl) {
         Holder<SoundEvent> h = e.getSound();
         if (h != null) {
            SoundSource src = e.getSource();
            if (src != SoundSource.MUSIC && src != SoundSource.AMBIENT && src != SoundSource.VOICE) {
               RadioNetwork.onWorldSound(sl, at, ((SoundEvent)h.value()).getLocation(), e.getNewVolume(), e.getNewPitch());
            }
         }
      }
   }

   public static void pollJukeboxes(ServerLevel sl, Collection<MicrophoneBE> mics) {
      Map<Long, Map<Long, Heard>> dim = (Map)HEARD.computeIfAbsent(sl.dimension(), (k) -> new HashMap());
      Set<Long> alive = new HashSet();

      for(MicrophoneBE m : mics) {
         if (!m.isRemoved()) {
            long mk;
            TapePlayerBE var10000;
            label94: {
               mk = m.getBlockPos().asLong();
               alive.add(mk);
               if (m instanceof TapePlayerBE) {
                  TapePlayerBE tp = (TapePlayerBE)m;
                  if (tp.isRecording()) {
                     var10000 = tp;
                     break label94;
                  }
               }

               var10000 = null;
            }

            TapePlayerBE tape = var10000;
            boolean live = !(m instanceof TapePlayerBE) && m.feeds != null && m.feeds.active && !m.feeds.isRemoved();
            long root = live ? m.feeds.getBlockPos().asLong() : 0L;
            Map<Long, Heard> now = new HashMap();
            if (tape != null || live) {
               BlockPos c = m.getBlockPos();

               for(BlockPos p : BlockPos.betweenClosed(c.offset(-6, -6, -6), c.offset(6, 6, 6))) {
                  if (sl.isLoaded(p)) {
                     BlockEntity var17 = sl.getBlockEntity(p);
                     if (var17 instanceof JukeboxBlockEntity) {
                        JukeboxBlockEntity jb = (JukeboxBlockEntity)var17;
                        if (jb.getSongPlayer().isPlaying()) {
                           JukeboxSong song = jb.getSongPlayer().getSong();
                           if (song != null) {
                              ResourceLocation sid = ((SoundEvent)song.soundEvent().value()).getLocation();
                              Heard was = (Heard)((Map)dim.getOrDefault(mk, Map.of())).get(p.asLong());
                              now.put(p.asLong(), new Heard(sid, root, was != null && was.sound().equals(sid) && was.root() == root ? was.since() : sl.getGameTime()));
                           }
                        }
                     }
                  }
               }
            }

            Map<Long, Heard> before = (Map)dim.computeIfAbsent(mk, (k) -> new HashMap());

            for(Map.Entry<Long, Heard> e : before.entrySet()) {
               Heard n = (Heard)now.get(e.getKey());
               if (n == null || !n.sound().equals(((Heard)e.getValue()).sound())) {
                  send(sl, m, tape, (Heard)e.getValue(), (Long)e.getKey(), (byte)2);
               }
            }

            for(Map.Entry<Long, Heard> e : now.entrySet()) {
               Heard b = (Heard)before.get(e.getKey());
               boolean fresh = b == null || !b.sound().equals(((Heard)e.getValue()).sound());
               if (tape != null) {
                  if (fresh) {
                     tape.capture(((Heard)e.getValue()).sound(), 1.0F, 1.0F, (byte)1);
                  }
               } else if (((Heard)e.getValue()).root() != 0L) {
                  RadioNetwork.broadcastSound(sl, ((Heard)e.getValue()).root(), ((Heard)e.getValue()).sound(), 1.0F, 1.0F, (byte)1, (Long)e.getKey(), (int)Math.min(2147483647L, (sl.getGameTime() - ((Heard)e.getValue()).since()) * 50L));
               }
            }

            before.clear();
            before.putAll(now);
         }
      }

      Iterator<Map.Entry<Long, Map<Long, Heard>>> it = dim.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Long, Map<Long, Heard>> e = (Map.Entry)it.next();
         if (!alive.contains(e.getKey())) {
            for(Map.Entry<Long, Heard> h : ((Map)e.getValue()).entrySet()) {
               if (((Heard)h.getValue()).root() != 0L) {
                  RadioNetwork.broadcastSound(sl, ((Heard)h.getValue()).root(), ((Heard)h.getValue()).sound(), 1.0F, 1.0F, (byte)2, (Long)h.getKey());
               }
            }

            it.remove();
         }
      }

   }

   private static void send(ServerLevel sl, MicrophoneBE m, TapePlayerBE tape, Heard h, long jukebox, byte kind) {
      if (tape != null) {
         tape.capture(h.sound(), 1.0F, 1.0F, kind);
      } else {
         if (h.root() != 0L) {
            RadioNetwork.broadcastSound(sl, h.root(), h.sound(), 1.0F, 1.0F, kind, jukebox);
         }

      }
   }

   public static void clear() {
      HEARD.clear();
   }

   private static record Heard(ResourceLocation sound, long root, long since) {
   }
}
