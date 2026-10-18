package com.airwaves.client;

import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import com.airwaves.logic.AntennaUtil;
import com.airwaves.menu.TuningTableMenu;
import com.airwaves.net.SpectrumRequestPayload;
import com.airwaves.net.StationInfo;
import com.airwaves.net.TunePayload;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class TuningTableScreen extends AbstractContainerScreen<TuningTableMenu> {
   private static final int FRAME = -13026227;
   private static final int FRAME_HI = -11315349;
   private static final int BODY = -14013130;
   private static final int GLASS = -16116724;
   private static final int LCD_ON = -8585322;
   private static final int LCD_DIM = -14796252;
   private static final int TEXT = -1644050;
   private static final int DIM = -7695456;
   private static final int AMBER = -2052032;
   private static final int RED = -47032;
   private static final int SX = 8;
   private static final int SY = 108;
   private static final int SW = 240;
   private static final int SH = 40;
   private boolean fm = true;
   private boolean reachTab;
   private double freq = 98.1;
   private double lastFm = 98.1;
   private double lastAm = (double)1000.0F;
   private EditBox box;
   private int dirty = -1;
   private int ticks;
   private ItemStack shown;
   private final Button[] step;
   private Button specTab;
   private Button reachTabB;

   public TuningTableScreen(TuningTableMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.shown = ItemStack.EMPTY;
      this.step = new Button[6];
      this.imageWidth = 256;
      this.imageHeight = 242;
   }

   private String fmt() {
      return this.fm ? String.format(Locale.ROOT, "%.2f", this.freq) : String.format(Locale.ROOT, "%.1f", this.freq);
   }

   private double[] steps() {
      return this.fm ? new double[]{(double)-1.0F, -0.1, -0.01, 0.01, 0.1, (double)1.0F} : new double[]{(double)-10.0F, (double)-1.0F, -0.1, 0.1, (double)1.0F, (double)10.0F};
   }

   private static String stepLabel(double s) {
      double a = Math.abs(s);
      String t = a >= (double)1.0F ? String.valueOf((int)a) : String.format(Locale.ROOT, "%s", a).replace("0.", ".");
      return (s < (double)0.0F ? "-" : "+") + t;
   }

   protected void init() {
      super.init();
      int x = this.leftPos;
      int y = this.topPos;
      this.addRenderableWidget(Button.builder(Component.literal("AM"), (b) -> this.setBand(false)).bounds(x + 36, y + 54, 22, 16).build());
      this.addRenderableWidget(Button.builder(Component.literal("FM"), (b) -> this.setBand(true)).bounds(x + 60, y + 54, 22, 16).build());
      this.box = new EditBox(this.font, x + 85, y + 55, 43, 14, Component.empty());
      this.box.setMaxLength(7);
      this.box.setFilter((s) -> s.matches("\\d{0,4}(\\.\\d{0,2})?"));
      this.box.setResponder((s) -> {
         try {
            if (!s.isEmpty() && !s.equals(".")) {
               this.freq = Double.parseDouble(s);
               this.dirty = 0;
            }
         } catch (NumberFormatException var3) {
         }

      });
      this.addRenderableWidget(this.box);
      this.box.setValue(this.fmt());

      for(int i = 0; i < 6; ++i) {
         this.step[i] = (Button)this.addRenderableWidget(Button.builder(Component.literal(stepLabel(this.steps()[i])), (b) -> this.setFreq(this.freq + this.steps()[i])).bounds(x + 8 + i * 21, y + 74, 20, 14).build());
      }

      this.addRenderableWidget(Button.builder(Component.literal("<<"), (b) -> this.seek(-1)).bounds(x + 150, y + 94, 18, 12).build());
      this.addRenderableWidget(Button.builder(Component.literal(">>"), (b) -> this.seek(1)).bounds(x + 170, y + 94, 18, 12).build());
      this.specTab = (Button)this.addRenderableWidget(Button.builder(Component.literal("Spec"), (b) -> this.reachTab = false).bounds(x + 192, y + 94, 27, 12).build());
      this.reachTabB = (Button)this.addRenderableWidget(Button.builder(Component.literal("Reach"), (b) -> this.reachTab = true).bounds(x + 220, y + 94, 28, 12).build());
      PacketDistributor.sendToServer(new SpectrumRequestPayload(), new CustomPacketPayload[0]);
   }

   private void relabel() {
      double[] st = this.steps();

      for(int i = 0; i < 6; ++i) {
         this.step[i].setMessage(Component.literal(stepLabel(st[i])));
      }

   }

   private void setBand(boolean f) {
      if (f != this.fm) {
         if (this.fm) {
            this.lastFm = this.freq;
         } else {
            this.lastAm = this.freq;
         }

         this.fm = f;
         this.freq = f ? this.lastFm : this.lastAm;
         this.box.setValue(this.fmt());
         this.relabel();
         this.send();
      }
   }

   private void setFreq(double f) {
      this.freq = (new Tuning(this.fm, f)).clamped().freq();
      this.box.setValue(this.fmt());
      this.send();
   }

   private void send() {
      if (((TuningTableMenu)this.menu).getSlot(0).hasItem()) {
         PacketDistributor.sendToServer(new TunePayload(this.fm, (new Tuning(this.fm, this.freq)).clamped().freq()), new CustomPacketPayload[0]);
      }

   }

   private List<StationInfo> band() {
      return ClientData.spectrum.stream().filter((s) -> s.fm() == this.fm).toList();
   }

   private StationInfo here() {
      for(StationInfo s : this.band()) {
         if (Math.round(s.freq() * (double)100.0F) == Math.round(this.freq * (double)100.0F)) {
            return s;
         }
      }

      return null;
   }

   private void seek(int dir) {
      StationInfo best = null;

      for(StationInfo s : this.band()) {
         if (dir > 0 && s.freq() > this.freq + 0.005 && (best == null || s.freq() < best.freq())) {
            best = s;
         }

         if (dir < 0 && s.freq() < this.freq - 0.005 && (best == null || s.freq() > best.freq())) {
            best = s;
         }
      }

      if (best == null) {
         for(StationInfo s : this.band()) {
            if (best != null) {
               if (dir > 0) {
                  if (!(s.freq() < best.freq())) {
                     continue;
                  }
               } else if (!(s.freq() > best.freq())) {
                  continue;
               }
            }

            best = s;
         }
      }

      if (best != null) {
         this.setFreq(best.freq());
      }

   }

   protected void containerTick() {
      super.containerTick();
      if (++this.ticks % 20 == 0) {
         PacketDistributor.sendToServer(new SpectrumRequestPayload(), new CustomPacketPayload[0]);
      }

      if (this.dirty >= 0 && ++this.dirty > 12) {
         this.freq = (new Tuning(this.fm, this.freq)).clamped().freq();
         this.send();
         this.dirty = -1;
      }

      ItemStack s = ((TuningTableMenu)this.menu).getSlot(0).getItem();
      if (!ItemStack.isSameItemSameComponents(s, this.shown)) {
         if (!s.isEmpty()) {
            Tuning t = (Tuning)s.getOrDefault((DataComponentType)ModRegistry.TUNING.get(), Tuning.DEFAULT);
            if (t.fm() != this.fm) {
               this.fm = t.fm();
               this.relabel();
            }

            this.freq = t.freq();
            if (!this.box.isFocused()) {
               this.box.setValue(this.fmt());
            }
         }

         this.shown = s.copy();
      }

      this.specTab.active = this.reachTab;
      this.reachTabB.active = !this.reachTab;
   }

   private double fx(int px) {
      return Tuning.min(this.fm) + (double)(px - (this.leftPos + 8)) / (double)240.0F * (Tuning.max(this.fm) - Tuning.min(this.fm));
   }

   private int px(double f) {
      return this.leftPos + 8 + (int)Math.round((f - Tuning.min(this.fm)) / (Tuning.max(this.fm) - Tuning.min(this.fm)) * (double)239.0F);
   }

   public boolean mouseClicked(double mx, double my, int b) {
      int x0 = this.leftPos + 8;
      int y0 = this.topPos + 108;
      if (!this.reachTab && b == 0 && mx >= (double)x0 && mx < (double)(x0 + 240) && my >= (double)y0 && my < (double)(y0 + 40)) {
         StationInfo near = null;
         int nd = 5;

         for(StationInfo s : this.band()) {
            int d = Math.abs(this.px(s.freq()) - (int)mx);
            if (d < nd) {
               nd = d;
               near = s;
            }
         }

         this.setFreq(near != null ? near.freq() : (double)Math.round(this.fx((int)mx) * (double)(this.fm ? 10 : 1)) / (this.fm ? (double)10.0F : (double)1.0F));
         return true;
      } else {
         return super.mouseClicked(mx, my, b);
      }
   }

   public boolean mouseScrolled(double mx, double my, double sx, double sy) {
      if (my < (double)(this.topPos + 92) || mx >= (double)(this.leftPos + 8) && mx < (double)(this.leftPos + 8 + 240) && my < (double)(this.topPos + 108 + 40)) {
         this.setFreq(this.freq + Math.signum(sy) * (hasShiftDown() ? (double)(this.fm ? 1 : 10) : (this.fm ? 0.1 : (double)1.0F)));
         return true;
      } else {
         return super.mouseScrolled(mx, my, sx, sy);
      }
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      super.render(g, mx, my, pt);
      if (!this.reachTab && mx >= this.leftPos + 8 && mx < this.leftPos + 8 + 240 && my >= this.topPos + 108 && my < this.topPos + 108 + 40) {
         for(StationInfo s : this.band()) {
            if (Math.abs(this.px(s.freq()) - mx) <= 3) {
               Font var10001 = this.font;
               String var10002 = (new Tuning(s.fm(), s.freq())).display();
               g.renderTooltip(var10001, Component.literal(var10002 + (s.name().isEmpty() ? "" : "  \"" + s.name() + "\"") + String.format(Locale.ROOT, "  %d%%", Math.round(s.quality() * 100.0F))), mx, my);
               return;
            }
         }
      }

      this.renderTooltip(g, mx, my);
   }

   protected void renderLabels(GuiGraphics g, int mx, int my) {
   }

   private void inset(GuiGraphics g, int x, int y, int w, int h, int fill) {
      g.fill(x - 1, y - 1, x + w + 1, y + h + 1, -15987440);
      g.fill(x, y, x + w, y + h, fill);
      g.fill(x, y + h, x + w + 1, y + h + 1, -11907490);
   }

   protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
      int x = this.leftPos;
      int y = this.topPos;
      g.fill(x - 2, y - 2, x + 258, y + 244, -15987440);
      g.fill(x, y, x + 256, y + 242, -13026227);
      g.fill(x, y, x + 256, y + 1, -11315349);
      g.fill(x, y, x + 1, y + 242, -11315349);
      g.fill(x + 3, y + 3, x + 253, y + 239, -14013130);

      for(int[] p : new int[][]{{5, 5}, {249, 5}, {5, 235}, {249, 235}}) {
         g.fill(x + p[0], y + p[1], x + p[0] + 2, y + p[1] + 2, -9802114);
      }

      g.drawString(this.font, "TUNING TABLE", x + 10, y + 7, -1644050, false);
      g.drawString(this.font, "ITEM", x + 9, y + 21, -7695456, false);

      for(Slot s : ((TuningTableMenu)this.menu).slots) {
         this.inset(g, x + s.x, y + s.y, 16, 16, -11907241);
      }

      this.inset(g, x + 36, y + 20, 92, 31, -16116724);
      boolean has = ((TuningTableMenu)this.menu).getSlot(0).hasItem();
      int on = has ? -8585322 : -11892138;
      g.drawString(this.font, "AM", x + 39, y + 23, !this.fm ? on : -14796252, false);
      g.drawString(this.font, "FM", x + 39, y + 33, this.fm ? on : -14796252, false);
      String num = this.fmt();
      g.pose().pushPose();
      g.pose().translate((float)(x + 124 - this.font.width(num) * 2), (float)(y + 23), 0.0F);
      g.pose().scale(2.0F, 2.0F, 1.0F);
      g.drawString(this.font, num, 0, 0, on, false);
      g.pose().popPose();
      StationInfo st = this.here();
      String name = !has ? "" : (this.fm ? "MHz  " : "kHz  ") + (st == null ? "" : st.name());
      g.drawString(this.font, this.font.plainSubstrByWidth(name, 86), x + 39, y + 42, has ? -11745180 : -12948924, false);
      this.drawScope(g, x + 136, y + 20, 112, 46, st);
      this.inset(g, x + 136, y + 70, 112, 16, -16116724);
      float q = st == null ? 0.0F : st.quality();
      g.drawString(this.font, "S", x + 139, y + 74, -7695456, false);

      for(int i = 0; i < 15; ++i) {
         boolean lit = q * 15.0F > (float)i;
         int col = i < 9 ? -11214736 : (i < 12 ? -2052032 : -47032);
         g.fill(x + 147 + i * 5, y + 73, x + 150 + i * 5, y + 83, lit ? col : -15062498);
      }

      g.drawString(this.font, String.format(Locale.ROOT, "%3d%%", Math.round(q * 100.0F)), x + 224, y + 74, q > 0.0F ? -8585322 : -7695456, false);
      List<StationInfo> b = this.band();
      g.drawString(this.font, this.reachTab ? "COVERAGE" : "SPECTRUM", x + 9, y + 96, -1644050, false);
      this.inset(g, x + 8, y + 108, 240, 40, -16052460);
      if (this.reachTab) {
         this.drawReach(g, x + 8, y + 108, 118, 40, false);
         this.drawReach(g, x + 8 + 120 + 2, y + 108, 118, 40, true);
      } else {
         this.drawSpectrum(g, x + 8, y + 108, b);
      }

      g.drawString(this.font, "Inventory", x + 47, y + 150, -7695456, false);
   }

   private void drawSpectrum(GuiGraphics g, int x, int y, List<StationInfo> b) {
      Random r = new Random(Util.getMillis() / 90L);

      for(int i = 0; i < 240; i += 2) {
         int h = 1 + r.nextInt(3);
         g.fill(x + i, y + 40 - 10 - h, x + i + 1, y + 40 - 10, -14796252);
      }

      double lo = Tuning.min(this.fm);
      double hi = Tuning.max(this.fm);
      double[] ticks = this.fm ? new double[]{(double)88.0F, (double)92.0F, (double)96.0F, (double)100.0F, (double)104.0F, (double)108.0F} : new double[]{(double)600.0F, (double)800.0F, (double)1000.0F, (double)1200.0F, (double)1400.0F, (double)1600.0F};

      for(double t : ticks) {
         int px = this.px(t);
         g.fill(px, y + 40 - 10, px + 1, y + 40 - 8, -11907490);
         String s = String.valueOf((int)t);
         g.drawString(this.font, s, Math.min(x + 240 - this.font.width(s), Math.max(x, px - this.font.width(s) / 2)), y + 40 - 8, -7695456, false);
      }

      g.fill(x, y + 40 - 10, x + 240, y + 40 - 9, -14009810);

      for(StationInfo s : b) {
         int px = this.px(s.freq());
         int h = 3 + (int)(s.quality() * 26.0F);
         float wob = (float)(0.9 + 0.1 * Math.sin((double)Util.getMillis() / (double)150.0F + s.freq()));
         h = (int)((float)h * wob);
         g.fill(px - 1, y + 40 - 10 - h, px + 2, y + 40 - 10, -11214736);
         g.fill(px, y + 40 - 10 - h, px + 1, y + 40 - 10 - h + 1, -5177408);
      }

      int nx = this.px(this.freq);
      g.fill(nx, y, nx + 1, y + 40 - 9, -47032);
      g.fill(nx - 1, y, nx + 2, y + 2, -47032);
   }

   private void drawScope(GuiGraphics g, int x, int y, int w, int h, StationInfo st) {
      this.inset(g, x, y, w, h, -16248312);

      for(int i = 1; i < 4; ++i) {
         g.fill(x, y + h * i / 4, x + w, y + h * i / 4 + 1, -15585256);
      }

      for(int i = 1; i < 6; ++i) {
         g.fill(x + w * i / 6, y, x + w * i / 6 + 1, y + h, -15585256);
      }

      double t = (double)(Util.getMillis() % 100000L) / (double)1000.0F;
      double norm = (this.freq - Tuning.min(this.fm)) / (Tuning.max(this.fm) - Tuning.min(this.fm));
      double cycles = (double)6.0F + (double)22.0F * norm;
      double amp = (double)h * 0.4;
      float noise = st == null ? 0.55F : (1.0F - st.quality()) * 0.6F;
      Random r = new Random(Util.getMillis() / 50L);
      int cy = y + h / 2;
      int prev = cy;

      for(int i = 0; i < w; ++i) {
         double u = (double)i / (double)w;
         double m = Math.sin((Math.PI * 2D) * ((double)1.5F * u - 0.6 * t));
         double ph = (Math.PI * 2D) * cycles * u - t * (double)14.0F;
         double val;
         if (this.fm) {
            val = Math.sin(ph + (double)4.0F * m);
         } else {
            double a = ((double)1.0F + 0.65 * m) / 1.65;
            val = a * Math.sin(ph);
            int e = (int)(amp * a);
            g.fill(x + i, cy - e, x + i + 1, cy - e + 1, -13997514);
            g.fill(x + i, cy + e, x + i + 1, cy + e + 1, -13997514);
         }

         val = val * (double)(1.0F - noise) + (r.nextDouble() * (double)2.0F - (double)1.0F) * (double)noise;
         int yy = (int)((double)cy - amp * val);
         g.fill(x + i, Math.min(yy, prev), x + i + 1, Math.max(yy, prev) + 1, -10682502);
         prev = yy;
      }

      g.drawString(this.font, this.fm ? "FM" : "AM", x + 3, y + 3, -13989312, false);
   }

   private void drawReach(GuiGraphics g, int x, int y, int w, int h, boolean fmPanel) {
      boolean sel = fmPanel == this.fm;
      if (sel) {
         g.fill(x, y, x + w, y + 1, -11214736);
      }

      int cx = x + h / 2 + 2;
      int cy = y + h / 2;
      double scale = ((double)h / (double)2.0F - (double)2.0F) / AntennaUtil.range(20, false);
      double rmax = AntennaUtil.range(20, fmPanel) * scale;
      Random rnd = new Random(Util.getMillis() / 120L + (long)(fmPanel ? 7 : 3));

      for(int i = 0; i < 160; ++i) {
         double a = rnd.nextDouble() * Math.PI * (double)2.0F;
         double u = Math.sqrt(rnd.nextDouble());
         double rho = rmax * u;
         double p = fmPanel ? Math.pow(u, 3.2) : Math.pow(u, 1.35);
         int px = cx + (int)(rho * Math.cos(a));
         int py = cy + (int)(rho * Math.sin(a));
         if (py >= y && py < y + h) {
            g.fill(px, py, px + 1, py + 1, rnd.nextDouble() < p * 0.9 ? -7696488 : -12791712);
         }
      }

      int[] hs = new int[]{4, 10, 20};
      int[] cols = new int[]{-2052032, -10436384, -1};

      for(int k = 0; k < 3; ++k) {
         double rr = AntennaUtil.range(hs[k], fmPanel) * scale;
         int stepsN = (int)((Math.PI * 2D) * rr * 1.4);

         for(int s = 0; s < stepsN; ++s) {
            double a = (double)(s * 2) * Math.PI / (double)stepsN;
            double jit = fmPanel ? (double)0.0F : (rnd.nextDouble() - (double)0.5F) * (double)2.0F;
            int px = cx + (int)((rr + jit) * Math.cos(a));
            int py = cy + (int)((rr + jit) * Math.sin(a));
            if (py >= y && py < y + h) {
               g.fill(px, py, px + 1, py + 1, cols[k]);
            }
         }
      }

      g.fill(cx - 1, cy - 1, cx + 1, cy + 1, -47032);
      int tx = x + h + 4;
      g.drawString(this.font, fmPanel ? "FM" : "AM", tx, y + 2, sel ? -11214736 : -1644050, false);
      g.drawString(this.font, String.format(Locale.ROOT, "%d/%d/%d", Math.round(AntennaUtil.range(4, fmPanel)), Math.round(AntennaUtil.range(10, fmPanel)), Math.round(AntennaUtil.range(20, fmPanel))), tx, y + 12, -7695456, false);
   }
}
