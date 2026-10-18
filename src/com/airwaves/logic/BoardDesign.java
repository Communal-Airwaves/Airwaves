package com.airwaves.logic;

import com.airwaves.ModRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum BoardDesign {
   RADIO("radio_board", -10444560, new String[]{"........", "-kc-xc-.", "..l..t--", "..r--r..", "........"}),
   RF("rf_board", -2076592, new String[]{"........", "-xc--u--", "..k..r..", "..c.....", "........"}),
   TUNER("tuner_board", -11220768, new String[]{"........", "-kc--xc-", "........", "........", "........"}),
   AMPLIFIER("amplifier_board", -1533376, new String[]{"........", "..u--u..", "-ru--ur-", "..c.....", "........"}),
   AUDIO("audio_board", -9383840, new String[]{"........", "-rc--rt-", ".....t..", "........", "........"}),
   DISPLAY("display_board", -999360, new String[]{"........", ".l..l..l", "-r--r--r", "....t...", "........"}),
   CONTROL("control_board", -5213968, new String[]{"........", "-tt--rl-", "........", "........", "........"}),
   COMPUTER("computer_board", -12558112, new String[]{"........", "-rc-m-n-", "........", "........", "........"}),
   SENSOR("sensor_board", -12533600, new String[]{"........", "-d--q-rl", "........", "........", "........"}),
   LOGIC("logic_board", -6642512, new String[]{"........", "-kc-tt-.", "......rc", "........", "........"});

   public static final int W = 8;
   public static final int H = 5;
   public static final int CELLS = 40;
   public static final int EMPTY = -1;
   public static final int TRACE = -2;
   public final String id;
   public final int accent;
   private final String[] rows;

   private BoardDesign(String id, int accent, String... rows) {
      this.id = id;
      this.accent = accent;
      this.rows = rows;
   }

   public Item item() {
      return ModRegistry.board(this);
   }

   public static BoardDesign of(Item item) {
      for(BoardDesign d : values()) {
         if (d.item() == item) {
            return d;
         }
      }

      return null;
   }

   public int[] example() {
      int[] out = new int[40];

      for(int r = 0; r < 5; ++r) {
         for(int c = 0; c < 8; ++c) {
            char ch = this.rows[r].charAt(c);
            out[r * 8 + c] = ch == '-' ? -2 : (ch == '.' ? -1 : BoardDesign.Part.byLetter(ch).ordinal());
         }
      }

      return out;
   }

   public BoardLayout exampleLayout() {
      return BoardLayout.of(this.example());
   }

   // $FF: synthetic method
   private static BoardDesign[] $values() {
      return new BoardDesign[]{RADIO, RF, TUNER, AMPLIFIER, AUDIO, DISPLAY, CONTROL, COMPUTER, SENSOR, LOGIC};
   }

   public static enum Part {
      RESISTOR('r'),
      CAPACITOR('c'),
      LED('l'),
      TRANSISTOR('t'),
      TUBE('u'),
      COIL('k'),
      CRYSTAL('x'),
      MEMORY_CHIP('m'),
      COUNTER_CHIP('n'),
      PULSE_CHIP('q'),
      LIGHT_SENSOR('d'),
      RAIN_SENSOR('w'),
      MOTION_SENSOR('o');

      public final char letter;

      private Part(char letter) {
         this.letter = letter;
      }

      public Item item() {
         Item var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = (Item)ModRegistry.RESISTOR.get();
            case 1 -> var10000 = (Item)ModRegistry.CAPACITOR.get();
            case 2 -> var10000 = (Item)ModRegistry.LED.get();
            case 3 -> var10000 = (Item)ModRegistry.TRANSISTOR.get();
            case 4 -> var10000 = ModRegistry.electronTube();
            case 5 -> var10000 = (Item)ModRegistry.COPPER_COIL.get();
            case 6 -> var10000 = (Item)ModRegistry.CRYSTAL_OSCILLATOR.get();
            case 7 -> var10000 = (Item)ModRegistry.MEMORY_CHIP.get();
            case 8 -> var10000 = (Item)ModRegistry.COUNTER_CHIP.get();
            case 9 -> var10000 = (Item)ModRegistry.PULSE_CHIP.get();
            case 10 -> var10000 = (Item)ModRegistry.LIGHT_SENSOR.get();
            case 11 -> var10000 = (Item)ModRegistry.RAIN_SENSOR.get();
            case 12 -> var10000 = (Item)ModRegistry.MOTION_SENSOR.get();
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      public static Part of(ItemStack s) {
         if (s.isEmpty()) {
            return null;
         } else {
            for(Part p : values()) {
               if (s.is(p.item())) {
                  return p;
               }
            }

            return null;
         }
      }

      public boolean standalone() {
         return this.ordinal() >= MEMORY_CHIP.ordinal();
      }

      public static Part byLetter(char c) {
         for(Part p : values()) {
            if (p.letter == c) {
               return p;
            }
         }

         throw new IllegalArgumentException("no part '" + c + "'");
      }

      // $FF: synthetic method
      private static Part[] $values() {
         return new Part[]{RESISTOR, CAPACITOR, LED, TRANSISTOR, TUBE, COIL, CRYSTAL, MEMORY_CHIP, COUNTER_CHIP, PULSE_CHIP, LIGHT_SENSOR, RAIN_SENSOR, MOTION_SENSOR};
      }
   }
}
