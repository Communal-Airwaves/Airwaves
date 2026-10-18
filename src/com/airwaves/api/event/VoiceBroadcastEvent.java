package com.airwaves.api.event;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class VoiceBroadcastEvent extends Event implements ICancellableEvent {
   private final ServerLevel level;
   private final long stationId;
   private final UUID speaker;
   private final byte[] frame;

   public VoiceBroadcastEvent(ServerLevel level, long stationId, UUID speaker, byte[] frame) {
      this.level = level;
      this.stationId = stationId;
      this.speaker = speaker;
      this.frame = frame;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public long getStationId() {
      return this.stationId;
   }

   public UUID getSpeaker() {
      return this.speaker;
   }

   public byte[] getFrame() {
      return (byte[])this.frame.clone();
   }
}
