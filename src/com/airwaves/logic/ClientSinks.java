package com.airwaves.logic;

import com.airwaves.be.AudioSink;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientSinks {
   public static final Set<AudioSink> SINKS = ConcurrentHashMap.newKeySet();

   private ClientSinks() {
   }
}
