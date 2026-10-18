package com.airwaves.api.event;

import com.airwaves.api.RadioStation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class CollectStationsEvent extends Event {
   private final ServerLevel level;
   private final List<RadioStation> stations = new ArrayList();

   public CollectStationsEvent(ServerLevel level) {
      this.level = level;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public void addStation(RadioStation station) {
      if (station != null) {
         this.stations.add(station);
      }

   }

   public List<RadioStation> stations() {
      return Collections.unmodifiableList(this.stations);
   }
}
