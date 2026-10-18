package com.airwaves.client;

import com.airwaves.net.CablePulsePayload;
import java.util.HashMap;
import java.util.Map;

public final class CablePulses {
   private static final Map<String, Long> LIVE = new HashMap();

   private CablePulses() {
   }

   static String key(long a, int as, long b, int bs) {
      return a + ":" + as + ">" + b + ":" + bs;
   }

   public static synchronized void add(CablePulsePayload p) {
      long now = System.currentTimeMillis();

      for(int i = 0; i < p.from().length; ++i) {
         LIVE.put(key(p.from()[i], p.fromSlot()[i], p.to()[i], p.toSlot()[i]), now);
      }

      if (LIVE.size() > 4096) {
         LIVE.clear();
      }

   }

   public static synchronized int dir(long a, int as, long b, int bs) {
      long now = System.currentTimeMillis();
      Long f = (Long)LIVE.get(key(a, as, b, bs));
      if (f != null && now - f < 700L) {
         return 1;
      } else {
         Long r = (Long)LIVE.get(key(b, bs, a, as));
         return r != null && now - r < 700L ? -1 : 0;
      }
   }
}
