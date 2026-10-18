package com.airwaves.api;

import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import com.airwaves.client.MuLaw;
import com.airwaves.logic.BoardAnalysis;
import com.airwaves.logic.BoardLayout;
import com.airwaves.logic.Emitter;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.power.PowerSupply;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AirwavesApi {
   public static final String VERSION = "1.0";
   public static final String MODID = "airwaves";
   public static final int SAMPLE_RATE = 16000;
   public static final int FRAME_SAMPLES = 320;

   private AirwavesApi() {
   }

   public static long stationId(BlockPos pos) {
      return pos.asLong();
   }

   public static List<RadioStation> stations(ServerLevel level) {
      List<RadioStation> out = new ArrayList();

      for(Emitter e : RadioNetwork.emitters(level)) {
         out.add(RadioStation.of(e));
      }

      return out;
   }

   public static Reception listen(ServerLevel level, BlockPos pos, double antennaTop, Tuning tuning, double gain) {
      List<Emitter> em = RadioNetwork.emitters(level);
      if (em.isEmpty()) {
         return Reception.NOTHING;
      } else {
         RadioNetwork.Best b = RadioNetwork.best(level, pos, antennaTop, tuning.clamped(), gain, em, RadioNetwork.jammers(level));
         if (b.q() < 0.02F) {
            return new Reception(b.q(), 0L, "");
         } else {
            String name = "";

            for(Emitter e : em) {
               if (e.root() == b.root()) {
                  name = e.name();
                  break;
               }
            }

            return new Reception(b.q(), b.root(), name);
         }
      }
   }

   public static boolean broadcast(ServerLevel level, long stationId, UUID speaker, byte[] frame, float gain) {
      return frame != null && frame.length >= 16 && frame.length <= 960 ? RadioNetwork.broadcast(level, stationId, speaker, frame, Math.max(0.0F, Math.min(1.0F, gain))) : false;
   }

   public static void playAt(ServerLevel level, BlockPos pos, UUID speaker, byte[] frame, float quality, boolean fm, float gain) {
      RadioNetwork.send(level, pos, speaker, frame, quality, fm, gain);
   }

   public static byte[] encode(short[] pcm) {
      byte[] out = new byte[pcm.length];

      for(int i = 0; i < pcm.length; ++i) {
         out[i] = MuLaw.enc(pcm[i]);
      }

      return out;
   }

   public static short[] decode(byte[] frame) {
      short[] out = new short[frame.length];

      for(int i = 0; i < frame.length; ++i) {
         out[i] = MuLaw.dec(frame[i]);
      }

      return out;
   }

   public static MachinePower power(Level level, BlockPos pos) {
      return PowerSupply.at(level, pos);
   }

   public static BoardLayout layout(ItemStack board) {
      BoardLayout l = (BoardLayout)board.get((DataComponentType)ModRegistry.BOARD_LAYOUT.get());
      return l;
   }

   public static BoardAnalysis.Program program(ItemStack board) {
      BoardLayout l = layout(board);
      if (l == null) {
         return null;
      } else {
         BoardAnalysis.Result r = l.analyse();
         return r.works() ? BoardAnalysis.program(r) : null;
      }
   }
}
