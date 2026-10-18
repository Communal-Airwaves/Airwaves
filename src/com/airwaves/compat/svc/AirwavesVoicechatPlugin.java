package com.airwaves.compat.svc;

import com.airwaves.logic.RadioNetwork;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

@ForgeVoicechatPlugin
public class AirwavesVoicechatPlugin implements VoicechatPlugin {
   private static VoicechatApi api;
   private static final Map<UUID, OpusDecoder> DECODERS = new ConcurrentHashMap();
   private static final Map<UUID, Long> LAST = new ConcurrentHashMap();

   public String getPluginId() {
      return "airwaves";
   }

   public void initialize(VoicechatApi a) {
      api = a;
   }

   public void registerEvents(EventRegistration reg) {
      reg.registerEvent(MicrophonePacketEvent.class, AirwavesVoicechatPlugin::onMic);
   }

   private static void onMic(MicrophonePacketEvent e) {
      VoicechatConnection con = e.getSenderConnection();
      if (con != null && api != null) {
         Object var3 = con.getPlayer().getPlayer();
         if (var3 instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)var3;
            UUID id = sp.getUUID();
            if (SvcState.usesSvc(id)) {
               byte[] opus = ((MicrophonePacket)e.getPacket()).getOpusEncodedData();
               if (opus != null && opus.length != 0) {
                  long now = System.currentTimeMillis();
                  Long last = (Long)LAST.put(id, now);
                  OpusDecoder dec = (OpusDecoder)DECODERS.computeIfAbsent(id, (k) -> api.createDecoder());
                  if (last != null && now - last > 500L) {
                     dec.resetState();
                  }

                  short[] pcm48 = dec.decode(opus);
                  if (pcm48 != null && pcm48.length >= 3) {
                     byte[] frame = toAirwaves(pcm48);
                     sp.getServer().execute(() -> {
                        if (!sp.isRemoved() && !sp.isSpectator()) {
                           RadioNetwork.onVoice(sp, frame);
                        }

                     });
                  }
               }
            }
         }
      }
   }

   private static byte[] toAirwaves(short[] pcm48) {
      int n = pcm48.length / 3;
      byte[] out = new byte[n];

      for(int i = 0; i < n; ++i) {
         int s = (pcm48[i * 3] + pcm48[i * 3 + 1] + pcm48[i * 3 + 2]) / 3;
         out[i] = muLaw((short)Math.max(-32768, Math.min(32767, s)));
      }

      return out;
   }

   private static byte muLaw(short pcm) {
      int s = pcm;
      int sign = pcm >> 8 & 128;
      if (sign != 0) {
         s = -pcm;
      }

      if (s > 32635) {
         s = 32635;
      }

      s += 132;
      int exp = 7;

      for(int mask = 16384; (s & mask) == 0 && exp > 0; mask >>= 1) {
         --exp;
      }

      int mant = s >> exp + 3 & 15;
      return (byte)(~(sign | exp << 4 | mant));
   }

   public static void forget(UUID id) {
      OpusDecoder d = (OpusDecoder)DECODERS.remove(id);
      if (d != null) {
         d.close();
      }

      LAST.remove(id);
   }
}
