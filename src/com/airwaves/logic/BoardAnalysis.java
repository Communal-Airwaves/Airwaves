package com.airwaves.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.ToIntFunction;

public final class BoardAnalysis {
   private static final int R = 0;
   private static final int C = 1;
   private static final int L = 2;
   private static final int T = 3;
   private static final int U = 4;
   private static final int K = 5;
   private static final int X = 6;

   private static Stage single(int part) {
      if (part >= 0 && part < BoardDesign.Part.values().length) {
         Stage var10000;
         switch (BoardDesign.Part.values()[part]) {
            case MEMORY_CHIP -> var10000 = BoardAnalysis.Stage.MEMORY;
            case COUNTER_CHIP -> var10000 = BoardAnalysis.Stage.COUNTER;
            case PULSE_CHIP -> var10000 = BoardAnalysis.Stage.PULSE;
            case LIGHT_SENSOR -> var10000 = BoardAnalysis.Stage.DAYLIGHT;
            case RAIN_SENSOR -> var10000 = BoardAnalysis.Stage.RAIN;
            case MOTION_SENSOR -> var10000 = BoardAnalysis.Stage.MOTION;
            default -> var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   private BoardAnalysis() {
   }

   private static int[] nb(int i) {
      int c = i % 8;
      int r = i / 8;
      int W = 8;
      return new int[]{c > 0 ? i - 1 : -1, c < W - 1 ? i + 1 : -1, r > 0 ? i - W : -1, r < 4 ? i + W : -1};
   }

   private static Stage pair(int a, int b) {
      int lo = Math.min(a, b);
      int hi = Math.max(a, b);
      if (lo == 1 && hi == 5) {
         return BoardAnalysis.Stage.TANK;
      } else if (lo == 1 && hi == 6) {
         return BoardAnalysis.Stage.OSCILLATOR;
      } else if (lo == 0 && hi == 4) {
         return BoardAnalysis.Stage.TUBE_AMP;
      } else if (lo == 0 && hi == 3) {
         return BoardAnalysis.Stage.TRANSISTOR_AMP;
      } else if (lo == 3 && hi == 3) {
         return BoardAnalysis.Stage.LOGIC;
      } else if (lo == 0 && hi == 1) {
         return BoardAnalysis.Stage.TIMER;
      } else {
         return lo == 0 && hi == 2 ? BoardAnalysis.Stage.INDICATOR : null;
      }
   }

   private static int anchor(Stage s) {
      byte var10000;
      switch (s.ordinal()) {
         case 0:
            var10000 = 5;
            break;
         case 1:
            var10000 = 6;
            break;
         case 2:
            var10000 = 4;
            break;
         case 3:
         case 4:
            var10000 = 3;
            break;
         case 5:
            var10000 = 1;
            break;
         case 6:
            var10000 = 2;
            break;
         default:
            var10000 = -1;
      }

      return var10000;
   }

   public static Result analyse(int[] parts, boolean[] traces) {
      int N = 40;
      int W = 8;
      int H = 5;
      boolean[] cond = new boolean[N];
      int[] dist = new int[N];
      Arrays.fill(dist, -1);
      int nparts = 0;

      for(int i = 0; i < N; ++i) {
         cond[i] = parts[i] >= 0 || traces[i];
         if (parts[i] >= 0) {
            ++nparts;
         }
      }

      ArrayDeque<Integer> q = new ArrayDeque();

      for(int r = 0; r < H; ++r) {
         if (cond[r * W]) {
            dist[r * W] = 0;
            q.add(r * W);
         }
      }

      while(!q.isEmpty()) {
         int i = (Integer)q.poll();

         for(int n : nb(i)) {
            if (n >= 0 && cond[n] && dist[n] < 0) {
               dist[n] = dist[i] + 1;
               q.add(n);
            }
         }
      }

      boolean[] live = new boolean[N];

      for(int i = 0; i < N; ++i) {
         live[i] = dist[i] >= 0;
      }

      EnumMap<Stage, Integer> counts = new EnumMap(Stage.class);
      if (nparts == 0) {
         return new Result(false, "gui.airwaves.workbench.empty", live, dist, counts, List.of(), List.of());
      } else {
         boolean out = false;

         for(int r = 0; r < H; ++r) {
            out |= live[r * W + W - 1];
         }

         if (!out) {
            return new Result(false, "gui.airwaves.workbench.open", live, dist, counts, List.of(), List.of());
         } else {
            for(int i = 0; i < N; ++i) {
               if (parts[i] >= 0 && !live[i]) {
                  return new Result(false, "gui.airwaves.workbench.loose", live, dist, counts, List.of(), List.of());
               }
            }

            TreeSet<Long> links = new TreeSet();

            for(int a = 0; a < N; ++a) {
               if (parts[a] >= 0) {
                  HashSet<Integer> seen = new HashSet();
                  ArrayDeque<Integer> tq = new ArrayDeque();

                  for(int n : nb(a)) {
                     if (n >= 0) {
                        if (parts[n] >= 0) {
                           links.add(key(a, n));
                        } else if (traces[n]) {
                           seen.add(n);
                           tq.add(n);
                        }
                     }
                  }

                  while(!tq.isEmpty()) {
                     int i = (Integer)tq.poll();

                     for(int n : nb(i)) {
                        if (n >= 0 && n != a && !seen.contains(n)) {
                           if (parts[n] >= 0) {
                              links.add(key(a, n));
                           } else if (traces[n]) {
                              seen.add(n);
                              tq.add(n);
                           }
                        }
                     }
                  }
               }
            }

            EnumMap<Stage, Set<Integer>> anchors = new EnumMap(Stage.class);
            EnumMap<Stage, Integer> first = new EnumMap(Stage.class);
            EnumMap<Stage, Set<Integer>> members = new EnumMap(Stage.class);

            for(long k : links) {
               int a = (int)(k >> 16);
               int b = (int)(k & 65535L);
               Stage s = pair(parts[a], parts[b]);
               if (s != null) {
                  Set<Integer> mem = (Set)members.computeIfAbsent(s, (x) -> new HashSet());
                  mem.add(a);
                  mem.add(b);
                  Set<Integer> an = (Set)anchors.computeIfAbsent(s, (x) -> new HashSet());
                  if (parts[a] == anchor(s)) {
                     an.add(a);
                  }

                  if (parts[b] == anchor(s)) {
                     an.add(b);
                  }

                  first.merge(s, Math.min(dist[a], dist[b]), Math::min);
               }
            }

            for(int i = 0; i < N; ++i) {
               Stage s = single(parts[i]);
               if (s != null && live[i]) {
                  ((Set)anchors.computeIfAbsent(s, (x) -> new HashSet())).add(i);
                  ((Set)members.computeIfAbsent(s, (x) -> new HashSet())).add(i);
                  first.merge(s, dist[i], Math::min);
               }
            }

            for(Map.Entry<Stage, Set<Integer>> e : anchors.entrySet()) {
               int n = e.getKey() == BoardAnalysis.Stage.LOGIC ? (((Set)e.getValue()).size() + 1) / 2 : ((Set)e.getValue()).size();
               if (n > 0) {
                  counts.put((Stage)e.getKey(), n);
               }
            }

            List<BoardDesign> designs = designs(counts);
            List<Stage> program = new ArrayList(first.keySet());
            Objects.requireNonNull(first);
            program.sort(Comparator.comparingInt(first::get).thenComparingInt(Enum::ordinal));
            return new Result(true, "", live, dist, counts, designs, program, members);
         }
      }
   }

   private static long key(int a, int b) {
      return (long)Math.min(a, b) << 16 | (long)Math.max(a, b);
   }

   public static List<BoardDesign> designs(EnumMap<Stage, Integer> c) {
      ToIntFunction<Stage> g = (s) -> (Integer)c.getOrDefault(s, 0);
      List<BoardDesign> d = new ArrayList();
      if (g.applyAsInt(BoardAnalysis.Stage.TANK) > 0 && g.applyAsInt(BoardAnalysis.Stage.OSCILLATOR) > 0 && g.applyAsInt(BoardAnalysis.Stage.TRANSISTOR_AMP) > 0 && g.applyAsInt(BoardAnalysis.Stage.INDICATOR) > 0) {
         d.add(BoardDesign.RADIO);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.OSCILLATOR) > 0 && g.applyAsInt(BoardAnalysis.Stage.TUBE_AMP) > 0 && g.applyAsInt(BoardAnalysis.Stage.TANK) > 0) {
         d.add(BoardDesign.RF);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.TANK) > 0 && g.applyAsInt(BoardAnalysis.Stage.OSCILLATOR) > 0) {
         d.add(BoardDesign.TUNER);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.TUBE_AMP) > 0 && g.applyAsInt(BoardAnalysis.Stage.TANK) == 0 && g.applyAsInt(BoardAnalysis.Stage.OSCILLATOR) == 0) {
         d.add(BoardDesign.AMPLIFIER);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.TRANSISTOR_AMP) > 0 && g.applyAsInt(BoardAnalysis.Stage.TIMER) > 0 && g.applyAsInt(BoardAnalysis.Stage.TUBE_AMP) == 0 && g.applyAsInt(BoardAnalysis.Stage.OSCILLATOR) == 0) {
         d.add(BoardDesign.AUDIO);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.INDICATOR) >= 3) {
         d.add(BoardDesign.DISPLAY);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.LOGIC) > 0 && g.applyAsInt(BoardAnalysis.Stage.INDICATOR) > 0) {
         d.add(BoardDesign.CONTROL);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.MEMORY) + g.applyAsInt(BoardAnalysis.Stage.COUNTER) + g.applyAsInt(BoardAnalysis.Stage.PULSE) > 0) {
         d.add(BoardDesign.COMPUTER);
      }

      if (g.applyAsInt(BoardAnalysis.Stage.DAYLIGHT) + g.applyAsInt(BoardAnalysis.Stage.RAIN) + g.applyAsInt(BoardAnalysis.Stage.MOTION) > 0) {
         d.add(BoardDesign.SENSOR);
      }

      d.add(BoardDesign.LOGIC);
      return d;
   }

   public static Program program(Result r) {
      if (!r.works()) {
         return new Program(BoardAnalysis.Source.REDSTONE, List.of(), false, 20);
      } else {
         Stage src = null;

         for(Stage s : r.program()) {
            if (s == BoardAnalysis.Stage.TANK || s == BoardAnalysis.Stage.OSCILLATOR || s.sensor()) {
               src = s;
               break;
            }
         }

         Source var10000;
         if (src == null) {
            var10000 = BoardAnalysis.Source.REDSTONE;
         } else {
            switch (src.ordinal()) {
               case 0:
                  var10000 = BoardAnalysis.Source.RADIO;
                  break;
               case 1:
                  var10000 = BoardAnalysis.Source.CLOCK;
                  break;
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               default:
                  var10000 = BoardAnalysis.Source.REDSTONE;
                  break;
               case 10:
                  var10000 = BoardAnalysis.Source.DAYLIGHT;
                  break;
               case 11:
                  var10000 = BoardAnalysis.Source.RAIN;
                  break;
               case 12:
                  var10000 = BoardAnalysis.Source.MOTION;
            }
         }

         Source source = var10000;
         List<Step> steps = new ArrayList();
         boolean usedSrc = false;
         boolean lamp = false;

         for(Stage s : r.program()) {
            int n = r.count(s);
            if (s == src && !usedSrc) {
               usedSrc = true;
               --n;
               if (s == BoardAnalysis.Stage.TANK || s == BoardAnalysis.Stage.OSCILLATOR) {
                  n = 0;
               }
            }

            switch (s.ordinal()) {
               case 0:
                  if (s != src) {
                     steps.add(BoardAnalysis.Step.ONLY_ON_AIR);
                  }
                  break;
               case 1:
                  if (s != src) {
                     steps.add(BoardAnalysis.Step.BLINK);
                  }
                  break;
               case 2:
               case 3:
                  if (!steps.contains(BoardAnalysis.Step.AMPLIFY)) {
                     steps.add(BoardAnalysis.Step.AMPLIFY);
                  }
                  break;
               case 4:
                  steps.add(BoardAnalysis.Step.INVERT);
                  break;
               case 5:
                  for(int k = 0; k < n; ++k) {
                     steps.add(BoardAnalysis.Step.HOLD);
                  }
                  break;
               case 6:
                  lamp = true;
                  break;
               case 7:
                  for(int k = 0; k < n; ++k) {
                     steps.add(BoardAnalysis.Step.TOGGLE);
                  }
                  break;
               case 8:
                  for(int k = 0; k < n; ++k) {
                     steps.add(BoardAnalysis.Step.COUNT);
                  }
                  break;
               case 9:
                  for(int k = 0; k < n; ++k) {
                     steps.add(BoardAnalysis.Step.PULSE);
                  }
                  break;
               case 10:
                  if (n > 0) {
                     steps.add(BoardAnalysis.Step.ONLY_DAY);
                  }
                  break;
               case 11:
                  if (n > 0) {
                     steps.add(BoardAnalysis.Step.ONLY_RAIN);
                  }
                  break;
               case 12:
                  if (n > 0) {
                     steps.add(BoardAnalysis.Step.ONLY_PLAYER);
                  }
            }
         }

         int clock = 10 * (1 + r.count(BoardAnalysis.Stage.OSCILLATOR));
         return new Program(source, steps, lamp, clock);
      }
   }

   public static String stepName(Step s) {
      String var10000;
      switch (s.ordinal()) {
         case 0 -> var10000 = "invert";
         case 1 -> var10000 = "hold 1 s";
         case 2 -> var10000 = "amplify";
         case 3 -> var10000 = "blink";
         case 4 -> var10000 = "only on air";
         case 5 -> var10000 = "toggle";
         case 6 -> var10000 = "count";
         case 7 -> var10000 = "pulse";
         case 8 -> var10000 = "only by day";
         case 9 -> var10000 = "only in rain";
         case 10 -> var10000 = "only with a player near";
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static String describe(Program p) {
      StringBuilder var10000 = new StringBuilder;
      String var10002;
      switch (p.source().ordinal()) {
         case 0 -> var10002 = "redstone in";
         case 1 -> var10002 = "station heard";
         case 2 -> var10002 = "clock " + (double)p.clockTicks() / (double)20.0F + " s";
         case 3 -> var10002 = "daylight";
         case 4 -> var10002 = "rain";
         case 5 -> var10002 = "player near";
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      var10000.<init>(var10002);
      StringBuilder b = var10000;
      int hold = 0;

      for(Step s : p.steps()) {
         if (s == BoardAnalysis.Step.HOLD) {
            ++hold;
         } else {
            if (hold > 0) {
               b.append(" > hold ").append(hold).append(" s");
               hold = 0;
            }

            b.append(" > ").append(stepName(s));
         }
      }

      if (hold > 0) {
         b.append(" > hold ").append(hold).append(" s");
      }

      return b.append(" > out").toString();
   }

   public static enum Stage {
      TANK,
      OSCILLATOR,
      TUBE_AMP,
      TRANSISTOR_AMP,
      LOGIC,
      TIMER,
      INDICATOR,
      MEMORY,
      COUNTER,
      PULSE,
      DAYLIGHT,
      RAIN,
      MOTION;

      public boolean chip() {
         return this == MEMORY || this == COUNTER || this == PULSE;
      }

      public boolean sensor() {
         return this == DAYLIGHT || this == RAIN || this == MOTION;
      }

      public String key() {
         String var10000 = this.name();
         return "gui.airwaves.stage." + var10000.toLowerCase(Locale.ROOT);
      }

      public int color() {
         int var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = -3641288;
            case 1 -> var10000 = -6262560;
            case 2 -> var10000 = -26048;
            case 3 -> var10000 = -12566456;
            case 4 -> var10000 = -11509568;
            case 5 -> var10000 = -12818232;
            case 6 -> var10000 = -2081744;
            case 7 -> var10000 = -11760400;
            case 8 -> var10000 = -1523648;
            case 9 -> var10000 = -1023920;
            case 10 -> var10000 = -995232;
            case 11 -> var10000 = -11493136;
            case 12 -> var10000 = -2039584;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      // $FF: synthetic method
      private static Stage[] $values() {
         return new Stage[]{TANK, OSCILLATOR, TUBE_AMP, TRANSISTOR_AMP, LOGIC, TIMER, INDICATOR, MEMORY, COUNTER, PULSE, DAYLIGHT, RAIN, MOTION};
      }
   }

   public static record Result(boolean works, String status, boolean[] live, int[] dist, EnumMap<Stage, Integer> counts, List<BoardDesign> designs, List<Stage> program, EnumMap<Stage, Set<Integer>> members) {
      public Result(boolean works, String status, boolean[] live, int[] dist, EnumMap<Stage, Integer> counts, List<BoardDesign> designs, List<Stage> program) {
         this(works, status, live, dist, counts, designs, program, new EnumMap(Stage.class));
      }

      public int count(Stage s) {
         return (Integer)this.counts.getOrDefault(s, 0);
      }

      public Set<Integer> cells(Stage s) {
         return (Set)this.members.getOrDefault(s, Set.of());
      }
   }

   public static enum Source {
      REDSTONE,
      RADIO,
      CLOCK,
      DAYLIGHT,
      RAIN,
      MOTION;

      // $FF: synthetic method
      private static Source[] $values() {
         return new Source[]{REDSTONE, RADIO, CLOCK, DAYLIGHT, RAIN, MOTION};
      }
   }

   public static enum Step {
      INVERT,
      HOLD,
      AMPLIFY,
      BLINK,
      ONLY_ON_AIR,
      TOGGLE,
      COUNT,
      PULSE,
      ONLY_DAY,
      ONLY_RAIN,
      ONLY_PLAYER;

      // $FF: synthetic method
      private static Step[] $values() {
         return new Step[]{INVERT, HOLD, AMPLIFY, BLINK, ONLY_ON_AIR, TOGGLE, COUNT, PULSE, ONLY_DAY, ONLY_RAIN, ONLY_PLAYER};
      }
   }

   public static record Program(Source source, List<Step> steps, boolean lamp, int clockTicks) {
   }
}
