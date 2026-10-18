package com.airwaves.client;

import com.airwaves.net.VoiceFramePayload;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VoiceClient {
   public static final float RATE = 16000.0F;
   private static final AudioFormat[] TRY = new AudioFormat[]{new AudioFormat(16000.0F, 16, 1, true, false), new AudioFormat(48000.0F, 16, 1, true, false), new AudioFormat(44100.0F, 16, 1, true, false), new AudioFormat(48000.0F, 16, 2, true, false), new AudioFormat(44100.0F, 16, 2, true, false), new AudioFormat(32000.0F, 16, 1, true, false)};
   public static volatile float level;
   public static volatile float floor = 0.002F;
   public static volatile boolean speaking;
   public static volatile String error;
   public static final float[] HISTORY = new float[150];
   public static final boolean[] HIST_SPEAK = new boolean[150];
   public static volatile int histPos;
   private static final ConcurrentLinkedQueue<byte[]> OUT = new ConcurrentLinkedQueue();
   private static volatile boolean running;
   private static TargetDataLine line;
   private static AudioFormat fmt;

   private VoiceClient() {
   }

   public static List<String> devices() {
      List<String> out = new ArrayList();
      out.add("Default");

      for(Mixer.Info mi : AudioSystem.getMixerInfo()) {
         try {
            Mixer m = AudioSystem.getMixer(mi);

            for(AudioFormat f : TRY) {
               if (m.isLineSupported(new DataLine.Info(TargetDataLine.class, f))) {
                  if (!out.contains(mi.getName())) {
                     out.add(mi.getName());
                  }
                  break;
               }
            }
         } catch (Throwable var10) {
         }
      }

      return out;
   }

   private static TargetDataLine open(String name) throws LineUnavailableException {
      LineUnavailableException last = null;

      for(AudioFormat f : TRY) {
         DataLine.Info info = new DataLine.Info(TargetDataLine.class, f);

         try {
            TargetDataLine l = null;
            if (name != null && !name.isEmpty() && !name.equals("Default")) {
               for(Mixer.Info mi : AudioSystem.getMixerInfo()) {
                  if (name.equals(mi.getName()) && AudioSystem.getMixer(mi).isLineSupported(info)) {
                     l = (TargetDataLine)AudioSystem.getMixer(mi).getLine(info);
                  }
               }
            }

            if (l == null && AudioSystem.isLineSupported(info)) {
               l = (TargetDataLine)AudioSystem.getLine(info);
            }

            if (l != null) {
               l.open(f, (int)(f.getFrameRate() * (float)f.getFrameSize() / 5.0F));
               fmt = f;
               return l;
            }
         } catch (LineUnavailableException e) {
            last = e;
         } catch (IllegalArgumentException var13) {
         }
      }

      throw last != null ? last : new LineUnavailableException("no supported microphone format");
   }

   public static synchronized void start() {
      if (!running) {
         if (VoiceConfig.svcActive()) {
            error = null;
         } else {
            try {
               line = open(VoiceConfig.device);
               line.start();
               error = null;
            } catch (Throwable t) {
               error = "No microphone (" + t.getClass().getSimpleName() + ")";
               line = null;
               return;
            }

            running = true;
            TargetDataLine l = line;
            AudioFormat f = fmt;
            Thread th = new Thread(() -> loop(l, f), "Airwaves-Mic");
            th.setDaemon(true);
            th.start();
         }
      }
   }

   public static synchronized void stop() {
      running = false;
      if (line != null) {
         try {
            line.stop();
            line.close();
         } catch (Throwable var1) {
         }

         line = null;
      }

      OUT.clear();
      level = 0.0F;
      speaking = false;
   }

   public static void restart() {
      stop();
      start();
   }

   public static void flush() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getConnection() != null) {
         byte[] pack = new byte[960];

         int n;
         byte[] f;
         for(n = 0; (f = (byte[])OUT.poll()) != null; n += f.length) {
            if (n + f.length > pack.length) {
               PacketDistributor.sendToServer(new VoiceFramePayload(Arrays.copyOf(pack, n)), new CustomPacketPayload[0]);
               n = 0;
            }

            System.arraycopy(f, 0, pack, n, f.length);
         }

         if (n > 0) {
            PacketDistributor.sendToServer(new VoiceFramePayload(Arrays.copyOf(pack, n)), new CustomPacketPayload[0]);
         }

      } else {
         OUT.clear();
      }
   }

   private static void loop(TargetDataLine l, AudioFormat f) {
      int ch = f.getChannels();
      double step = (double)(f.getSampleRate() / 16000.0F);
      int inFrames = (int)Math.round((double)320.0F * step);
      byte[] buf = new byte[inFrames * 2 * ch];
      short[] s = new short[320];
      int hang = 0;
      float agc = 1.0F;
      ArrayDeque<byte[]> preroll = new ArrayDeque();

      while(running) {
         int n = 0;

         try {
            while(n < buf.length && running) {
               int r = l.read(buf, n, buf.length - n);
               if (r <= 0) {
                  break;
               }

               n += r;
            }
         } catch (Throwable var23) {
            break;
         }

         if (n >= buf.length) {
            double sum = (double)0.0F;

            for(int i = 0; i < 320; ++i) {
               int a = (int)((double)i * step);
               int b = Math.max(a + 1, (int)((double)(i + 1) * step));
               long acc = 0L;
               int c = 0;

               for(int k = a; k < b && k < inFrames; ++k) {
                  for(int cc = 0; cc < ch; ++cc) {
                     int o = (k * ch + cc) * 2;
                     acc += (long)((short)(buf[o] & 255 | buf[o + 1] << 8));
                     ++c;
                  }
               }

               float v = c == 0 ? 0.0F : (float)acc / (float)c * VoiceConfig.input * agc;
               v = Math.max(-32768.0F, Math.min(32767.0F, v));
               s[i] = (short)((int)v);
               sum += (double)v * (double)v;
            }

            float rms = (float)Math.sqrt(sum / (double)320.0F) / 32768.0F;
            level = rms;
            if (!speaking) {
               floor = floor * 0.995F + Math.min(rms, 0.05F) * 0.005F;
            }

            float gate = Math.max(VoiceConfig.threshold, floor * 2.6F + 0.0015F);
            if (rms > gate) {
               hang = 18;
            } else if (hang > 0) {
               --hang;
            }

            boolean open = VoiceConfig.pushToTalk ? ClientInit.PTT_KEY.isDown() : hang > 0;
            speaking = open && !VoiceConfig.muted;
            if (speaking && rms > 5.0E-4F) {
               agc = Math.max(0.6F, Math.min(4.0F, agc + (0.12F / rms - agc) * 0.02F));
            }

            int hp = histPos;
            HISTORY[hp] = rms;
            HIST_SPEAK[hp] = speaking;
            histPos = (hp + 1) % HISTORY.length;
            byte[] enc = new byte[320];

            for(int i = 0; i < 320; ++i) {
               enc[i] = MuLaw.enc(s[i]);
            }

            if (VoiceConfig.monitor && (speaking || VoiceConfig.monitor)) {
               AudioPlayback.playSelf(enc);
            }

            if (!speaking) {
               preroll.addLast(enc);
               if (preroll.size() > 3) {
                  preroll.pollFirst();
               }
            } else {
               while(!preroll.isEmpty()) {
                  OUT.add((byte[])preroll.pollFirst());
               }

               OUT.add(enc);

               while(OUT.size() > 15) {
                  OUT.poll();
               }
            }
         }
      }

      speaking = false;
   }
}
