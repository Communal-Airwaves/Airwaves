package com.airwaves.logic;

import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

public final class TapeAudio {
   private static final Logger LOG = LogUtils.getLogger();
   public static final int MAX_FRAMES = 15000;
   private static final Map<String, List<Frame>> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, List<Frame>>(16, 0.75F, true) {
      protected boolean removeEldestEntry(Map.Entry<String, List<Frame>> e) {
         return this.size() > 12;
      }
   });

   private TapeAudio() {
   }

   private static Path file(MinecraftServer s, String id) {
      return s.getWorldPath(LevelResource.ROOT).resolve("airwaves_tapes").resolve(id.replaceAll("[^a-zA-Z0-9-]", "") + ".bin");
   }

   public static void save(MinecraftServer s, String id, List<Frame> frames) {
      List<Frame> copy = List.copyOf(frames);
      CACHE.put(id, copy);
      Path f = file(s, id);
      Util.ioPool().execute(() -> {
         try {
            Files.createDirectories(f.getParent());
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(f))));

            try {
               out.writeInt(1);
               out.writeInt(copy.size());

               for(Frame fr : copy) {
                  out.writeInt(fr.ms());
                  out.writeShort(fr.data().length);
                  out.write(fr.data());
               }
            } catch (Throwable var7) {
               try {
                  out.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            out.close();
         } catch (IOException e) {
            LOG.warn("Could not save tape {}", id, e);
         }

      });
   }

   public static List<Frame> load(MinecraftServer s, String id) {
      if (id != null && !id.isEmpty()) {
         List<Frame> hit = (List)CACHE.get(id);
         if (hit != null) {
            return hit;
         } else {
            Path f = file(s, id);
            if (!Files.exists(f, new LinkOption[0])) {
               return List.of();
            } else {
               try {
                  DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(f))));

                  List var14;
                  try {
                     in.readInt();
                     int n = Math.min(in.readInt(), 15000);
                     List<Frame> out = new ArrayList(n);

                     for(int i = 0; i < n; ++i) {
                        int ms = in.readInt();
                        byte[] d = new byte[Math.min(in.readUnsignedShort(), 1024)];
                        in.readFully(d);
                        out.add(new Frame(ms, d));
                     }

                     List<Frame> r = List.copyOf(out);
                     CACHE.put(id, r);
                     var14 = r;
                  } catch (Throwable var11) {
                     try {
                        in.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }

                     throw var11;
                  }

                  in.close();
                  return var14;
               } catch (IOException e) {
                  LOG.warn("Could not read tape {}", id, e);
                  return List.of();
               }
            }
         }
      } else {
         return List.of();
      }
   }

   public static void clear() {
      CACHE.clear();
   }

   public static record Frame(int ms, byte[] data) {
   }
}
