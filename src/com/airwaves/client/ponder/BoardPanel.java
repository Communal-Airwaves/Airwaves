package com.airwaves.client.ponder;

import com.airwaves.ModRegistry;
import com.airwaves.logic.BoardAnalysis;
import com.airwaves.logic.BoardDesign;
import com.airwaves.logic.BoardLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.AnimatedOverlayElementBase;
import net.createmod.ponder.foundation.instruction.FadeInOutInstruction;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class BoardPanel extends AnimatedOverlayElementBase {
   private static final int BOX_W = 214;
   private static final int BOX_H = 104;
   private static final int CELL = 10;
   private static final int BX = 13;
   private static final int BY = 9;
   private static final int PART_TICKS = 6;
   private static final int TRACE_TICKS = 2;
   private static final int START = 4;
   private static final int PCB = -14718150;
   private static final int PCB_HI = -13727154;
   private static final int PCB_LO = -15448278;
   private static final int PAD = -3634628;
   private static final int PAD_LIVE = -15254;
   private static final int TRACE = -5214162;
   private static final int HOLE = -15184083;
   private static final int UNDER = -14016234;
   private static final int LABEL = -6248272;
   private static final int PROGRAM = -10434320;
   private final Vec3 anchor;
   private final int[] parts;
   private final boolean[] traces;
   private final List<Integer> partOrder = new ArrayList();
   private final List<Integer> traceOrder = new ArrayList();
   private final boolean build;
   private Pointing pointing;
   int age;
   private int cachedKey;
   private BoardAnalysis.Result cached;

   public BoardPanel(Vec3 anchor, boolean build, String... rows) {
      this.pointing = Pointing.RIGHT;
      this.cachedKey = -1;
      this.anchor = anchor;
      this.build = build;
      StringBuilder b = new StringBuilder();

      for(String r : rows) {
         b.append(r);
      }

      BoardLayout l = new BoardLayout(b.toString());
      this.parts = l.parts();
      this.traces = l.traces();

      for(int c = 0; c < 8; ++c) {
         for(int r = 0; r < 5; ++r) {
            int i = r * 8 + c;
            if (this.parts[i] >= 0) {
               this.partOrder.add(i);
            } else if (this.traces[i]) {
               this.traceOrder.add(i);
            }
         }
      }

   }

   public static BoardPanel show(SceneBuilder builder, Vec3 anchor, int ticks, boolean build, String... rows) {
      return show(builder, anchor, Pointing.RIGHT, ticks, build, rows);
   }

   public static BoardPanel show(SceneBuilder builder, Vec3 anchor, Pointing pointing, int ticks, boolean build, String... rows) {
      BoardPanel p = new BoardPanel(anchor, build, rows);
      p.pointing = pointing;
      builder.addInstruction(new Show(p, ticks));
      return p;
   }

   public static BoardPanel show(SceneBuilder builder, Vec3 anchor, int ticks, boolean build, BoardDesign design) {
      int[] e = design.example();
      String cells = BoardLayout.of(e).cells();
      String[] rows = new String[5];

      for(int r = 0; r < 5; ++r) {
         rows[r] = cells.substring(r * 8, (r + 1) * 8);
      }

      return show(builder, anchor, ticks, build, rows);
   }

   public static int buildTicks(String... rows) {
      return (new BoardPanel(Vec3.ZERO, true, rows)).doneAt();
   }

   private int partsShown() {
      return this.build ? Math.max(0, Math.min(this.partOrder.size(), (this.age - 4) / 6 + 1)) : this.partOrder.size();
   }

   private int tracesShown() {
      if (!this.build) {
         return this.traceOrder.size();
      } else {
         int t0 = 4 + this.partOrder.size() * 6;
         return Math.max(0, Math.min(this.traceOrder.size(), (this.age - t0) / 2 + 1));
      }
   }

   private int doneAt() {
      return 4 + this.partOrder.size() * 6 + this.traceOrder.size() * 2;
   }

   private BoardAnalysis.Result analysis(int np, int nt) {
      int key = np * 100 + nt;
      if (key != this.cachedKey) {
         int[] p = new int[40];
         boolean[] t = new boolean[40];
         Arrays.fill(p, -1);

         for(int k = 0; k < np; ++k) {
            p[(Integer)this.partOrder.get(k)] = this.parts[(Integer)this.partOrder.get(k)];
         }

         for(int k = 0; k < nt; ++k) {
            t[(Integer)this.traceOrder.get(k)] = true;
         }

         this.cached = BoardAnalysis.analyse(p, t);
         this.cachedKey = key;
      }

      return this.cached;
   }

   public void render(PonderScene scene, PonderUI screen, GuiGraphics g, float pt, float fade) {
      if (!(fade < 0.0625F)) {
         Font font = screen.getFontRenderer();
         Vec2 at = scene.getTransform().sceneToScreen(this.anchor, pt);
         float sx = at.x;
         float sy = at.y;
         float dx = 0.0F;
         float dy = 0.0F;
         if (this.pointing == Pointing.RIGHT) {
            sx = Math.max(sx, 230.0F);
            sy = Math.max(sy, 56.0F);
            dx = -10.0F * (1.0F - fade);
         } else {
            sy = Math.max(sy, 118.0F);
            sx = Math.max(sx, 111.0F);
            dy = -10.0F * (1.0F - fade);
         }

         PoseStack ps = g.pose();
         ps.pushPose();
         ps.translate(sx + dx, sy + dy, 400.0F);
         PonderUI.renderSpeechBox(g, 0, 0, 214, 104, false, this.pointing, true);
         ps.translate(0.0F, 0.0F, 100.0F);
         int np = this.partsShown();
         int nt = this.tracesShown();
         BoardAnalysis.Result r = this.analysis(np, nt);
         boolean works = r.works();
         float time = (float)this.age + pt;
         int x0 = 4;
         int y0 = 4;
         int x1 = 102;
         int y1 = 64;
         bevel(g, x0, y0, x1, y1, -14718150, -13727154, -15448278);

         for(int row = 0; row < 5; ++row) {
            int a = row * 8;
            int b = a + 8 - 1;
            int cy = 9 + row * 10;
            g.fill(6, cy + 3, 11, cy + 7, r.live()[a] ? -15254 : -3634628);
            g.fill(95, cy + 3, 100, cy + 7, works && r.live()[b] ? -15254 : -3634628);
         }

         boolean[] partOn = new boolean[40];
         boolean[] traceOn = new boolean[40];

         for(int k = 0; k < np; ++k) {
            partOn[(Integer)this.partOrder.get(k)] = true;
         }

         for(int k = 0; k < nt; ++k) {
            traceOn[(Integer)this.traceOrder.get(k)] = true;
         }

         for(int i = 0; i < 40; ++i) {
            int cx0 = 13 + i % 8 * 10;
            int sy2 = 9 + i / 8 * 10;
            g.fill(cx0 + 1, sy2 + 1, cx0 + 10 - 1, sy2 + 10 - 1, partOn[i] ? -14016234 : -15184083);
            g.fill(cx0 + 1, sy2 + 1, cx0 + 2, sy2 + 2, -3634628);
         }

         for(int i = 0; i < 40; ++i) {
            if (partOn[i] || traceOn[i]) {
               int col = i % 8;
               int row = i / 8;
               int cx = 13 + col * 10 + 5 - 1;
               int cy = 9 + row * 10 + 5 - 1;
               int cc = copper(r.live()[i], r.dist()[i], works, time);
               if (col == 0) {
                  g.fill(11, cy, cx, cy + 2, cc);
               }

               if (col == 7) {
                  g.fill(cx, cy, 95, cy + 2, cc);
               }

               if (col < 7 && (partOn[i + 1] || traceOn[i + 1])) {
                  g.fill(cx, cy, cx + 10, cy + 2, cc);
               }

               if (row < 4 && (partOn[i + 8] || traceOn[i + 8])) {
                  g.fill(cx, cy, cx + 2, cy + 10, cc);
               }

               if (traceOn[i]) {
                  g.fill(cx - 1, cy - 1, cx + 3, cy + 3, r.live()[i] ? -15254 : -3634628);
               }
            }
         }

         for(int k = 0; k < np; ++k) {
            int i = (Integer)this.partOrder.get(k);
            int cx0 = 13 + i % 8 * 10;
            int sy2 = 9 + i / 8 * 10;
            item(g, new ItemStack(BoardDesign.Part.values()[this.parts[i]].item()), (float)cx0, (float)sy2, 0.625F);
         }

         if (this.build && np > 0 && np <= this.partOrder.size() && nt == 0) {
            int placedAt = 4 + (np - 1) * 6;
            float k = (time - (float)placedAt) / 6.0F;
            if (k < 1.0F) {
               int i = (Integer)this.partOrder.get(np - 1);
               int cx0 = 13 + i % 8 * 10;
               int sy2 = 9 + i / 8 * 10;
               int a = (int)(255.0F * (1.0F - k));
               outline(g, cx0 - 1, sy2 - 1, cx0 + 10 + 1, sy2 + 10 + 1, a << 24 | 16771232);
            }
         }

         int rx = x1 + 8;
         int ry = 5;
         small(g, font, Component.translatable("gui.airwaves.workbench.stages").getString(), (float)rx, (float)ry, -6248272);
         ry += 10;
         if (works) {
            int shown = 0;

            for(Map.Entry<BoardAnalysis.Stage, Integer> e : r.counts().entrySet()) {
               if (shown == 6) {
                  break;
               }

               g.fill(rx, ry + 1, rx + 4, ry + 5, ((BoardAnalysis.Stage)e.getKey()).color());
               String var10000 = Component.translatable(((BoardAnalysis.Stage)e.getKey()).key()).getString();
               String name = var10000 + ((Integer)e.getValue() > 1 ? " x" + String.valueOf(e.getValue()) : "");
               small(g, font, name, (float)(rx + 7), (float)ry, -1);
               ry += 8;
               ++shown;
            }
         } else {
            small(g, font, np < this.partOrder.size() ? "..." : Component.translatable("gui.airwaves.workbench.open").getString(), (float)rx, (float)ry, -6643544);
         }

         int by = y1 + 6;
         if (works) {
            int bx = 4;
            List<BoardDesign> ds = r.designs();
            int[] e = new int[40];

            for(int i = 0; i < e.length; ++i) {
               e[i] = partOn[i] ? this.parts[i] : (traceOn[i] ? -2 : -1);
            }

            BoardLayout layout = BoardLayout.of(e);

            for(int k = 0; k < ds.size() && k < 6; ++k) {
               float pop = Math.min(1.0F, (time - (float)this.doneAt() - (float)(k * 3)) / 4.0F);
               if (!(pop <= 0.0F)) {
                  ItemStack st = new ItemStack(((BoardDesign)ds.get(k)).item());
                  st.set((DataComponentType)ModRegistry.BOARD_LAYOUT.get(), layout);
                  if (k == 0) {
                     bevel(g, bx - 1, by - 1, bx + 17, by + 17, -1522584, -3920, -6657520);
                  }

                  item(g, st, (float)bx + 8.0F * (1.0F - pop), (float)by + 8.0F * (1.0F - pop), pop);
                  bx += 20;
               }
            }

            if (!ds.isEmpty()) {
               small(g, font, (new ItemStack(((BoardDesign)ds.get(0)).item())).getHoverName().getString(), (float)(bx + 2), (float)(by + 5), -5984);
            }

            String prog = BoardAnalysis.describe(BoardAnalysis.program(r));
            int maxW = 274;
            if (font.width(prog) > maxW) {
               String var69 = font.plainSubstrByWidth(prog, maxW - font.width("..."));
               prog = var69 + "...";
            }

            small(g, font, prog, 4.0F, (float)(by + 21), -10434320);
         }

         ps.popPose();
      }
   }

   private static int copper(boolean live, int dist, boolean works, float time) {
      if (!live) {
         return -5214162;
      } else if (!works) {
         return -3110336;
      } else {
         double ph = (double)time * (double)0.5F - (double)dist * 2.2;
         int k = (int)(Math.pow((double)0.5F + (double)0.5F * Math.sin(ph), (double)6.0F) * (double)110.0F);
         return -16777216 | Math.min(255, 216 + k / 3) << 16 | Math.min(255, 150 + k) << 8 | Math.min(255, 64 + k);
      }
   }

   private static void bevel(GuiGraphics g, int x0, int y0, int x1, int y1, int fill, int hi, int lo) {
      g.fill(x0, y0, x1, y1, fill);
      g.fill(x0, y0, x1, y0 + 1, hi);
      g.fill(x0, y0, x0 + 1, y1, hi);
      g.fill(x0, y1 - 1, x1, y1, lo);
      g.fill(x1 - 1, y0, x1, y1, lo);
   }

   private static void outline(GuiGraphics g, int x0, int y0, int x1, int y1, int c) {
      g.fill(x0, y0, x1, y0 + 1, c);
      g.fill(x0, y1 - 1, x1, y1, c);
      g.fill(x0, y0, x0 + 1, y1, c);
      g.fill(x1 - 1, y0, x1, y1, c);
   }

   private static void item(GuiGraphics g, ItemStack st, float x, float y, float scale) {
      PoseStack ps = g.pose();
      ps.pushPose();
      ps.translate(x, y, 0.0F);
      ps.scale(scale, scale, 1.0F);
      g.renderItem(st, 0, 0);
      ps.popPose();
   }

   private static void small(GuiGraphics g, Font font, String s, float x, float y, int color) {
      PoseStack ps = g.pose();
      ps.pushPose();
      ps.translate(x, y, 50.0F);
      ps.scale(0.75F, 0.75F, 1.0F);
      g.drawString(font, s, 0, 0, color, false);
      ps.popPose();
   }

   public static class Show extends FadeInOutInstruction {
      private final BoardPanel panel;

      public Show(BoardPanel panel, int ticks) {
         super(ticks);
         this.panel = panel;
      }

      protected void show(PonderScene scene) {
         this.panel.age = 0;
         scene.addElement(this.panel);
         this.panel.setVisible(true);
      }

      protected void hide(PonderScene scene) {
         this.panel.setVisible(false);
      }

      protected void applyFade(PonderScene scene, float fade) {
         this.panel.setFade(fade);
      }

      public void tick(PonderScene scene) {
         super.tick(scene);
         this.panel.age = Math.max(0, this.totalTicks - this.remainingTicks - 5);
      }
   }
}
