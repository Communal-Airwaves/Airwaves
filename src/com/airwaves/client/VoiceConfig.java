package com.airwaves.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

public final class VoiceConfig {
   public static boolean muted = false;
   public static boolean pushToTalk = false;
   public static boolean monitor = false;
   public static boolean deafened = false;
   public static float input = 1.0F;
   public static float voice = 1.0F;
   public static float radio = 1.0F;
   public static float threshold = 0.004F;
   public static String device = "";
   public static boolean useSvc = true;

   public static boolean svcInstalled() {
      return ModList.get() != null && ModList.get().isLoaded("voicechat");
   }

   public static boolean svcActive() {
      return useSvc && svcInstalled();
   }

   private VoiceConfig() {
   }

   private static Path file() {
      return FMLPaths.CONFIGDIR.get().resolve("airwaves-voice.properties");
   }

   private static float f(Properties p, String k, float d) {
      try {
         return Float.parseFloat(p.getProperty(k, String.valueOf(d)));
      } catch (Exception var4) {
         return d;
      }
   }

   public static void load() {
      try {
         InputStream in = new FileInputStream(file().toFile());

         try {
            Properties p = new Properties();
            p.load(in);
            muted = Boolean.parseBoolean(p.getProperty("muted", "false"));
            pushToTalk = Boolean.parseBoolean(p.getProperty("pushToTalk", "false"));
            input = f(p, "input", 1.0F);
            voice = f(p, "voice", 1.0F);
            radio = f(p, "radio", 1.0F);
            threshold = f(p, "threshold2", 0.004F);
            device = p.getProperty("device", "");
            useSvc = Boolean.parseBoolean(p.getProperty("useSvc", "true"));
         } catch (Throwable var4) {
            try {
               in.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         in.close();
      } catch (Exception var5) {
      }

   }

   public static void save() {
      try {
         OutputStream out = new FileOutputStream(file().toFile());

         try {
            Properties p = new Properties();
            p.setProperty("muted", String.valueOf(muted));
            p.setProperty("pushToTalk", String.valueOf(pushToTalk));
            p.setProperty("input", String.valueOf(input));
            p.setProperty("voice", String.valueOf(voice));
            p.setProperty("radio", String.valueOf(radio));
            p.setProperty("threshold2", String.valueOf(threshold));
            p.setProperty("device", device);
            p.setProperty("useSvc", String.valueOf(useSvc));
            p.store(out, "Create: Airwaves voice settings");
         } catch (Throwable var4) {
            try {
               out.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         out.close();
      } catch (Exception var5) {
      }

   }

   public static void reset() {
      muted = false;
      pushToTalk = false;
      monitor = false;
      deafened = false;
      input = 1.0F;
      voice = 1.0F;
      radio = 1.0F;
      threshold = 0.004F;
   }
}
