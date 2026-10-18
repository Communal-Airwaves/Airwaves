package com.airwaves.compat.svc;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SvcState {
   public static final Set<UUID> USING_SVC = ConcurrentHashMap.newKeySet();

   private SvcState() {
   }

   public static boolean usesSvc(UUID id) {
      return USING_SVC.contains(id);
   }

   public static void set(UUID id, boolean svc) {
      if (svc) {
         USING_SVC.add(id);
      } else {
         USING_SVC.remove(id);
      }

   }
}
