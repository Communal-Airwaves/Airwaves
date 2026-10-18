package com.airwaves.api.event;

import com.airwaves.api.RadioStation;
import com.airwaves.logic.Emitter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class RadioNetworkTickEvent extends Event {
   private final ServerLevel level;
   private final List<Emitter> raw;
   private List<RadioStation> stations;

   public RadioNetworkTickEvent(ServerLevel level, List<Emitter> emitters) {
      this.level = level;
      this.raw = emitters;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public List<RadioStation> stations() {
      if (this.stations == null) {
         List<RadioStation> l = new ArrayList();

         for(Emitter e : this.raw) {
            l.add(RadioStation.of(e));
         }

         this.stations = Collections.unmodifiableList(l);
      }

      return this.stations;
   }
}
