package com.airwaves.client;

import com.airwaves.logic.BoardAnalysis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class ProgramFlow {
   private static final int TEXT = -12959928;
   private static final int OK = -13727170;

   private ProgramFlow() {
   }

   public static void draw(GuiGraphics g, Font font, BoardAnalysis.Program p, int x0, int y0, int right, int bottom, int step) {
      List<String> label = new ArrayList();
      List<Integer> col = new ArrayList();
      switch (p.source()) {
         case RADIO:
            label.add("RADIO");
            col.add(BoardAnalysis.Stage.TANK.color());
            break;
         case CLOCK:
            double var10001 = (double)p.clockTicks();
            label.add("CLOCK " + var10001 / (double)20.0F + "s");
            col.add(BoardAnalysis.Stage.OSCILLATOR.color());
            break;
         case DAYLIGHT:
            label.add("SUN");
            col.add(BoardAnalysis.Stage.DAYLIGHT.color());
            break;
         case RAIN:
            label.add("RAIN");
            col.add(BoardAnalysis.Stage.RAIN.color());
            break;
         case MOTION:
            label.add("NEAR");
            col.add(BoardAnalysis.Stage.MOTION.color());
            break;
         default:
            label.add("REDSTONE");
            col.add(-5232096);
      }

      int hold = 0;

      for(int k = 0; k <= p.steps().size(); ++k) {
         BoardAnalysis.Step st = k < p.steps().size() ? (BoardAnalysis.Step)p.steps().get(k) : null;
         if (st == BoardAnalysis.Step.HOLD) {
            ++hold;
         } else {
            if (hold > 0) {
               label.add("HOLD " + hold + "s");
               col.add(BoardAnalysis.Stage.TIMER.color());
               hold = 0;
            }

            if (st == null) {
               break;
            }

            switch (st) {
               case INVERT:
                  label.add("NOT");
                  col.add(BoardAnalysis.Stage.LOGIC.color());
                  break;
               case AMPLIFY:
                  label.add("AMP");
                  col.add(BoardAnalysis.Stage.TRANSISTOR_AMP.color());
                  break;
               case BLINK:
                  label.add("BLINK");
                  col.add(BoardAnalysis.Stage.OSCILLATOR.color());
                  break;
               case ONLY_ON_AIR:
                  label.add("ON AIR?");
                  col.add(BoardAnalysis.Stage.TANK.color());
                  break;
               case TOGGLE:
                  label.add("TOGGLE");
                  col.add(BoardAnalysis.Stage.MEMORY.color());
                  break;
               case COUNT:
                  label.add("COUNT");
                  col.add(BoardAnalysis.Stage.COUNTER.color());
                  break;
               case PULSE:
                  label.add("PULSE");
                  col.add(BoardAnalysis.Stage.PULSE.color());
                  break;
               case ONLY_DAY:
                  label.add("DAY?");
                  col.add(BoardAnalysis.Stage.DAYLIGHT.color());
                  break;
               case ONLY_RAIN:
                  label.add("RAIN?");
                  col.add(BoardAnalysis.Stage.RAIN.color());
                  break;
               case ONLY_PLAYER:
                  label.add("NEAR?");
                  col.add(BoardAnalysis.Stage.MOTION.color());
            }
         }
      }

      label.add(p.lamp() ? "OUT + LAMP" : "OUT");
      col.add(-13727170);
      int x = x0;
      int y = y0;
      int lit = step % label.size();

      for(int k = 0; k < label.size(); ++k) {
         int w = (int)Math.ceil((double)((float)font.width((String)label.get(k)) * 0.6F)) + 5;
         if (x + w > right && x > x0) {
            x = x0;
            y += 10;
         }

         if (y + 9 > bottom) {
            g.drawString(font, "…", right - 6, y - 10, -12959928, false);
            return;
         }

         int c = (Integer)col.get(k);
         g.fill(x, y, x + w, y + 8, c);
         g.fill(x, y, x + w, y + 1, 1358954495);
         g.fill(x, y + 7, x + w, y + 8, 1342177280);
         if (k == lit) {
            g.fill(x - 1, y - 1, x + w + 1, y, -2880);
            g.fill(x - 1, y + 8, x + w + 1, y + 9, -2880);
            g.fill(x - 1, y, x, y + 8, -2880);
            g.fill(x + w, y, x + w + 1, y + 8, -2880);
         }

         g.pose().pushPose();
         g.pose().translate((float)(x + 3), (float)(y + 2), 0.0F);
         g.pose().scale(0.6F, 0.6F, 1.0F);
         g.drawString(font, (String)label.get(k), 0, 0, bright(c) ? -14671840 : -1, false);
         g.pose().popPose();
         x += w;
         if (k < label.size() - 1) {
            for(int a = 0; a < 3; ++a) {
               g.fill(x + 1 + a, y + 1 + a, x + 2 + a, y + 7 - a, -12959928);
            }

            x += 5;
         }
      }

   }

   private static boolean bright(int argb) {
      int r = argb >> 16 & 255;
      int gg = argb >> 8 & 255;
      int b = argb & 255;
      return (double)r * 0.3 + (double)gg * 0.59 + (double)b * 0.11 > (double)150.0F;
   }
}
