package com.airwaves.client;

import com.airwaves.net.StationInfo;
import java.util.List;

public final class ClientData {
   public static volatile List<StationInfo> spectrum = List.of();
   public static volatile long spectrumTime;
   public static volatile boolean hhOn;
   public static volatile boolean hhCarrier;
   public static volatile boolean hhFm;
   public static volatile float hhQuality;
   public static volatile String hhLabel = "";

   private ClientData() {
   }
}
