package com.airwaves.client;

import com.airwaves.item.TapeItem;
import com.airwaves.logic.TapeData;
import com.airwaves.menu.TapePlayerMenu;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class TapePlayerScreen extends AbstractContainerScreen<TapePlayerMenu> {
   private static final int BODY = -10733020;
   private static final int BODY_HI = -8761808;
   private static final int BODY_LO = -12966890;
   private static final int BEZEL = -14804456;
   private static final int GLASS = -15327204;
   private static final int PHOSPHOR = -4653096;
   private static final int PHOS_DIM = -9453676;
   private static final int RED = -42422;
   private static final int AMBER = -15798;
   private static final int PANEL = -13949406;
   private static final int METAL = -6644572;
   private static final int SX = 20;
   private static final int SY = 16;
   private static final int SW = 132;
   private static final int SH = 86;
   private static final int[][] BUTTONS = new int[][]{{48, 120}, {84, 120}, {120, 120}, {156, 120}};
   private static final int BW = 30;
   private static final int BH = 20;
   private final Random rng = new Random();

   public TapePlayerScreen(TapePlayerMenu m, Inventory inv, Component title) {
      super(m, inv, title);
      this.imageWidth = 200;
      this.imageHeight = 234;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   private static void round(GuiGraphics g, int x, int y, int w, int h, int r, int col) {
      for(int row = 0; row < h; ++row) {
         int d = row < r ? r - row : (row >= h - r ? row - (h - r) + 1 : 0);
         int in = d == 0 ? 0 : (int)Math.ceil((double)r - Math.sqrt((double)Math.max(0, r * r - d * d)));
         g.fill(x + in, y + row, x + w - in, y + row + 1, col);
      }

   }

   private static void disc(GuiGraphics g, int cx, int cy, int r, int col) {
      for(int dy = -r; dy <= r; ++dy) {
         int dx = (int)Math.sqrt((double)(r * r - dy * dy));
         g.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, col);
      }

   }

   protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
      int x = this.leftPos;
      int y = this.topPos;
      round(g, x, y, this.imageWidth, 146, 12, -12966890);
      round(g, x + 1, y + 1, this.imageWidth - 2, 143, 11, -10733020);
      g.fill(x + 12, y + 2, x + this.imageWidth - 12, y + 3, -8761808);
      round(g, x + 20 - 6, y + 16 - 6, 144, 98, 14, -14804456);
      round(g, x + 20, y + 16, 132, 86, 11, -15327204);
      this.drawScreen(g, x + 20, y + 16);
      int px = x + 162;
      g.fill(px, y + 12, px + 28, y + 104, -13949406);
      disc(g, px + 14, y + 26, 9, -15461872);
      disc(g, px + 14, y + 26, 7, -6644572);
      double a1 = (double)(System.currentTimeMillis() % 4000L) / (double)4000.0F * (((TapePlayerMenu)this.menu).mode() == 1 ? (Math.PI * 2D) : (double)0.0F) + 0.8;
      g.fill(px + 14 + (int)(Math.cos(a1) * (double)5.0F), y + 26 + (int)(Math.sin(a1) * (double)5.0F), px + 16 + (int)(Math.cos(a1) * (double)5.0F), y + 28 + (int)(Math.sin(a1) * (double)5.0F), -14671836);
      disc(g, px + 14, y + 52, 7, -15461872);
      disc(g, px + 14, y + 52, 5, -6644572);
      g.fill(px + 14, y + 47, px + 15, y + 52, -14671836);

      for(int i = 0; i < 6; ++i) {
         g.fill(px + 5, y + 68 + i * 5, px + 23, y + 70 + i * 5, -15461872);
      }

      boolean rec = ((TapePlayerMenu)this.menu).mode() == 2;
      boolean play = ((TapePlayerMenu)this.menu).mode() == 1;
      boolean blink = System.currentTimeMillis() / 400L % 2L == 0L;
      disc(g, px + 14, y + 104 - 6, 2, rec && blink ? -42422 : (play ? -10821510 : -12962768));
      int sx = x + 22 - 5;
      int sy = y + 122 - 5;
      round(g, sx, sy, 26, 26, 3, -15067628);
      g.fill(sx + 2, sy + 2, sx + 24, sy + 24, -13752284);
      g.fill(sx + 4, sy + 23, sx + 22, sy + 24, -11911110);
      String[] lab = new String[]{play ? "■" : "▶", "●", "↻", "◀◀"};
      int[] col = new int[]{play ? -15798 : -4653096, rec ? -42422 : -2064272, ((TapePlayerMenu)this.menu).looping() ? -4653096 : -7697782, -3092272};

      for(int i = 0; i < 4; ++i) {
         int bx = x + BUTTONS[i][0];
         int by = y + BUTTONS[i][1];
         boolean hover = mx >= bx && mx < bx + 30 && my >= by && my < by + 20;
         boolean down = i == 0 && play || i == 1 && rec || i == 2 && ((TapePlayerMenu)this.menu).looping();
         round(g, bx, by, 30, 20, 3, -15461872);
         round(g, bx + 1, by + (down ? 2 : 1), 28, 17, 3, hover ? -11779008 : -12962768);
         if (!down) {
            g.fill(bx + 3, by + 2, bx + 30 - 3, by + 3, -10857908);
         }

         g.drawCenteredString(this.font, lab[i], bx + 15, by + (down ? 7 : 6), col[i]);
      }

      g.fill(x + 12, y + 148, x + this.imageWidth - 12, y + this.imageHeight - 4, -14015198);

      for(int i = 0; i < 36; ++i) {
         int r = i < 27 ? i / 9 : 3;
         int c = i % 9;
         int ix = x + 20 - 1 + c * 18;
         int iy = y + (r < 3 ? 152 + r * 18 : 210) - 1;
         g.fill(ix, iy, ix + 18, iy + 18, -15330800);
         g.fill(ix + 1, iy + 1, ix + 17, iy + 17, -12831696);
      }

   }

   private void drawScreen(GuiGraphics g, int x, int y) {
      int mode = ((TapePlayerMenu)this.menu).mode();
      ItemStack st = ((TapePlayerMenu)this.menu).tape();
      boolean hasTape = st.getItem() instanceof TapeItem;
      TapeData d = hasTape ? TapeItem.data(st) : TapeData.EMPTY;
      float amount = !hasTape ? 1.0F : (mode == 1 ? 0.18F : (mode == 2 ? 0.3F : 0.55F));
      this.rng.setSeed(System.currentTimeMillis() / 45L);

      for(int yy = 2; yy < 84; yy += 2) {
         int inset = yy < 11 ? 11 - (int)Math.sqrt((double)(121 - (11 - yy) * (11 - yy))) : (yy > 75 ? 11 - (int)Math.sqrt((double)Math.max(0, 121 - (yy - 75) * (yy - 75))) : 0);

         for(int xx = 2 + inset; xx < 130 - inset; xx += 2) {
            if (!(this.rng.nextFloat() > amount)) {
               int v = 60 + this.rng.nextInt(170);
               int a = (int)(60.0F + 160.0F * amount);
               g.fill(x + xx, y + yy, x + xx + 2, y + yy + 2, a << 24 | v << 16 | v << 8 | v);
            }
         }
      }

      for(int yy = 1; yy < 86; yy += 3) {
         g.fill(x + 6, y + yy, x + 132 - 6, y + yy + 1, 805306368);
      }

      int roll = (int)(System.currentTimeMillis() / 30L % 106L) - 10;
      g.fill(x + 4, y + Math.max(2, roll), x + 132 - 4, y + Math.min(84, roll + 6), 352321535);
      int cx = x + 66;
      if (!hasTape) {
         g.drawCenteredString(this.font, "NO TAPE", cx, y + 43 - 4, -4653096);
      } else {
         String name = st.getHoverName().getString();
         name = this.font.plainSubstrByWidth(name, 116);
         g.drawCenteredString(this.font, name, cx, y + 9, -4653096);
         int total = mode == 2 ? ((TapePlayerMenu)this.menu).position() : d.length();
         String var10000 = TapeData.time(((TapePlayerMenu)this.menu).position());
         String time = var10000 + (mode == 2 ? "" : " / " + TapeData.time(total));
         if (d.blank() && mode != 2) {
            time = "BLANK";
         }

         g.pose().pushPose();
         g.pose().translate((float)cx, (float)(y + 30), 0.0F);
         g.pose().scale(2.0F, 2.0F, 1.0F);
         g.drawCenteredString(this.font, time, 0, 0, mode == 2 ? -42422 : -4653096);
         g.pose().popPose();
         int bx0 = x + 14;
         int bx1 = x + 132 - 14;
         int by = y + 56;
         g.fill(bx0, by, bx1, by + 4, 1614174282);
         float k = mode == 2 ? Math.min(1.0F, (float)((TapePlayerMenu)this.menu).position() / 6000.0F) : (d.length() > 0 ? Math.min(1.0F, (float)((TapePlayerMenu)this.menu).position() / (float)d.length()) : 0.0F);
         g.fill(bx0, by, bx0 + (int)((float)(bx1 - bx0) * k), by + 4, mode == 2 ? -42422 : -9453676);
         switch (mode) {
            case 1 -> var10000 = "▶ PLAYING";
            case 2 -> var10000 = "● RECORDING";
            default -> var10000 = d.blank() ? "● PRESS REC" : "■ STOPPED";
         }

         String state = var10000;
         g.drawString(this.font, state, x + 14, y + 66, mode == 2 ? -42422 : -9453676, false);
         if (((TapePlayerMenu)this.menu).looping()) {
            g.drawString(this.font, "↻ LOOP", x + 132 - 14 - this.font.width("↻ LOOP"), y + 66, -15798, false);
         }

         if (!d.blank()) {
            g.drawCenteredString(this.font, d.cues().size() + " sounds" + (d.hasVoice() ? " + voice" : ""), cx, y + 76, -11629972);
         } else if (mode == 2) {
            g.drawCenteredString(this.font, "talk or make noise nearby", cx, y + 76, -7713202);
         }

      }
   }

   protected void renderLabels(GuiGraphics g, int mx, int my) {
      g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX + 12, this.inventoryLabelY + 4, -4214884, false);
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      super.render(g, mx, my, pt);
      String[] tips = new String[]{"Play / Stop", "Record", "Loop", "Rewind"};

      for(int i = 0; i < 4; ++i) {
         int bx = this.leftPos + BUTTONS[i][0];
         int by = this.topPos + BUTTONS[i][1];
         if (mx >= bx && mx < bx + 30 && my >= by && my < by + 20) {
            g.renderTooltip(this.font, Component.literal(tips[i]), mx, my);
         }
      }

      this.renderTooltip(g, mx, my);
   }

   public boolean mouseClicked(double mx, double my, int btn) {
      for(int i = 0; i < 4; ++i) {
         int bx = this.leftPos + BUTTONS[i][0];
         int by = this.topPos + BUTTONS[i][1];
         if (btn == 0 && mx >= (double)bx && mx < (double)(bx + 30) && my >= (double)by && my < (double)(by + 20)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode != null) {
               mc.gameMode.handleInventoryButtonClick(((TapePlayerMenu)this.menu).containerId, i);
            }

            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));
            return true;
         }
      }

      return super.mouseClicked(mx, my, btn);
   }
}
