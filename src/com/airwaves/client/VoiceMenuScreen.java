package com.airwaves.client;

import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VoiceMenuScreen extends Screen {
   private static final int W = 320;
   private static final int H = 250;
   private static final int FRAME = -13026227;
   private static final int FRAME_HI = -11315349;
   private static final int BODY = -14276303;
   private static final int PANEL = -15066078;
   private static final int TEXT = -1644050;
   private static final int DIM = -7695456;
   private static final int GREEN = -11214736;
   private static final int RED = -2078648;
   private static final int AMBER = -2056144;
   private static final int CYAN = -11220768;
   private int x0;
   private int y0;
   private Slider sens;
   private Button muteB;
   private Button deafB;
   private Button modeB;
   private Button monB;
   private Button devB;
   private Button svcB;

   public VoiceMenuScreen() {
      super(Component.literal("Voice & Radio"));
   }

   private static String pct(double v) {
      return String.format(Locale.ROOT, "%d%%", Math.round(v * (double)200.0F));
   }

   protected void init() {
      this.x0 = (this.width - 320) / 2;
      this.y0 = (this.height - 250) / 2;
      int L = this.x0 + 12;
      int R = this.x0 + 166;
      int cw = 142;
      this.muteB = (Button)this.addRenderableWidget(Button.builder(Component.empty(), (b) -> {
         VoiceConfig.muted = !VoiceConfig.muted;
         this.relabel();
      }).bounds(L, this.y0 + 34, 70, 20).build());
      this.deafB = (Button)this.addRenderableWidget(Button.builder(Component.empty(), (b) -> {
         VoiceConfig.deafened = !VoiceConfig.deafened;
         this.relabel();
      }).bounds(L + 72, this.y0 + 34, 70, 20).build());
      this.modeB = (Button)this.addRenderableWidget(Button.builder(Component.empty(), (b) -> {
         VoiceConfig.pushToTalk = !VoiceConfig.pushToTalk;
         this.relabel();
      }).bounds(R, this.y0 + 34, cw, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("<"), (b) -> this.cycleDevice(-1)).bounds(L, this.y0 + 58, 20, 20).build());
      this.devB = (Button)this.addRenderableWidget(Button.builder(Component.empty(), (b) -> this.cycleDevice(1)).bounds(L + 22, this.y0 + 58, 252, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal(">"), (b) -> this.cycleDevice(1)).bounds(this.x0 + 320 - 32, this.y0 + 58, 20, 20).build());
      this.addRenderableWidget(new Slider(L, this.y0 + 82, cw, "Mic gain", (double)VoiceConfig.input / (double)2.0F, VoiceMenuScreen::pct, (v) -> VoiceConfig.input = (float)(v * (double)2.0F)));
      this.sens = (Slider)this.addRenderableWidget(new Slider(R, this.y0 + 82, cw, "Sensitivity", (double)1.0F - (double)VoiceConfig.threshold / 0.03, (v) -> String.format(Locale.ROOT, "%d%%", Math.round(v * (double)100.0F)), (v) -> VoiceConfig.threshold = (float)(((double)1.0F - v) * 0.03)));
      this.addRenderableWidget(new Slider(L, this.y0 + 106, cw, "Voice chat", (double)VoiceConfig.voice / (double)2.0F, VoiceMenuScreen::pct, (v) -> VoiceConfig.voice = (float)(v * (double)2.0F)));
      this.addRenderableWidget(new Slider(R, this.y0 + 106, cw, "Radios", (double)VoiceConfig.radio / (double)2.0F, VoiceMenuScreen::pct, (v) -> VoiceConfig.radio = (float)(v * (double)2.0F)));
      this.monB = (Button)this.addRenderableWidget(Button.builder(Component.empty(), (b) -> {
         VoiceConfig.monitor = !VoiceConfig.monitor;
         this.relabel();
      }).bounds(L, this.y0 + 130, cw, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Reset to defaults"), (b) -> {
         VoiceConfig.reset();
         this.rebuildWidgets();
         VoiceClient.restart();
      }).bounds(R, this.y0 + 130, cw, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Done"), (b) -> this.onClose()).bounds(this.x0 + 160 - 50, this.y0 + 250 - 26, 100, 20).build());
      if (VoiceConfig.svcInstalled()) {
         this.svcB = (Button)this.addRenderableWidget(Button.builder(Component.empty(), (b) -> {
            VoiceConfig.useSvc = !VoiceConfig.useSvc;
            if (VoiceConfig.svcActive()) {
               VoiceClient.stop();
            } else {
               VoiceClient.start();
            }

            ClientInit.sendVoiceMode();
            this.relabel();
         }).bounds(this.x0 + 8, this.y0 + 250 - 26, 98, 20).build());
      }

      this.relabel();
   }

   private void relabel() {
      this.muteB.setMessage(Component.literal(VoiceConfig.muted ? "Unmute" : "Mute"));
      this.deafB.setMessage(Component.literal(VoiceConfig.deafened ? "Undeafen" : "Deafen"));
      this.modeB.setMessage(Component.literal(VoiceConfig.pushToTalk ? "Mode: Push-to-talk [" + ClientInit.PTT_KEY.getTranslatedKeyMessage().getString() + "]" : "Mode: Voice activation"));
      String var10001 = VoiceConfig.monitor ? "ON" : "OFF";
      this.monB.setMessage(Component.literal("Hear myself: " + var10001));
      this.devB.setMessage(Component.literal(this.font.plainSubstrByWidth("Mic: " + (VoiceConfig.device.isEmpty() ? "System default" : VoiceConfig.device), 240)));
      if (this.sens != null) {
         this.sens.active = !VoiceConfig.pushToTalk && !VoiceConfig.svcActive();
      }

      if (this.svcB != null) {
         this.svcB.setMessage(Component.literal(VoiceConfig.svcActive() ? "Voice: Simple VC" : "Voice: Airwaves"));
      }

   }

   private void cycleDevice(int dir) {
      List<String> d = VoiceClient.devices();
      int i = d.indexOf(VoiceConfig.device.isEmpty() ? "Default" : VoiceConfig.device);
      String next = (String)d.get(Math.floorMod(i + dir, d.size()));
      VoiceConfig.device = next.equals("Default") ? "" : next;
      this.relabel();
      VoiceClient.restart();
   }

   public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
      super.renderBackground(g, mx, my, pt);
      g.fill(this.x0 - 2, this.y0 - 2, this.x0 + 320 + 2, this.y0 + 250 + 2, -15987440);
      g.fill(this.x0, this.y0, this.x0 + 320, this.y0 + 250, -13026227);
      g.fill(this.x0, this.y0, this.x0 + 320, this.y0 + 1, -11315349);
      g.fill(this.x0, this.y0, this.x0 + 1, this.y0 + 250, -11315349);
      g.fill(this.x0 + 3, this.y0 + 3, this.x0 + 320 - 3, this.y0 + 250 - 3, -14276303);

      for(int[] p : new int[][]{{5, 5}, {313, 5}, {5, 243}, {313, 243}}) {
         g.fill(this.x0 + p[0], this.y0 + p[1], this.x0 + p[0] + 2, this.y0 + p[1] + 2, -9802114);
      }

      g.fill(this.x0 + 3, this.y0 + 3, this.x0 + 320 - 3, this.y0 + 26, -13618370);
      g.fill(this.x0 + 3, this.y0 + 26, this.x0 + 320 - 3, this.y0 + 27, -15460838);
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      super.render(g, mx, my, pt);
      g.drawString(this.font, "VOICE & RADIO", this.x0 + 12, this.y0 + 11, -1644050, false);
      String st;
      int sc;
      if (VoiceConfig.svcActive()) {
         st = "VIA SIMPLE VOICE CHAT";
         sc = -11220768;
      } else if (VoiceClient.error != null) {
         st = "NO MIC";
         sc = -2056144;
      } else if (VoiceConfig.muted) {
         st = "MUTED";
         sc = -2078648;
      } else if (VoiceClient.speaking) {
         st = "ON AIR";
         sc = -11214736;
      } else {
         st = VoiceConfig.pushToTalk ? "HOLD " + ClientInit.PTT_KEY.getTranslatedKeyMessage().getString() : "LISTENING";
         sc = -7695456;
      }

      if (VoiceConfig.deafened) {
         st = st + " / DEAF";
      }

      int pw = this.font.width(st) + 16;
      int px = this.x0 + 320 - 12 - pw;
      g.fill(px, this.y0 + 7, px + pw, this.y0 + 21, -15460838);
      g.fill(px + 4, this.y0 + 12, px + 8, this.y0 + 16, sc);
      g.drawString(this.font, st, px + 11, this.y0 + 10, sc, false);
      int gx = this.x0 + 12;
      int gy = this.y0 + 156;
      int gw = 296;
      int gh = 48;
      g.fill(gx - 1, gy - 1, gx + gw + 1, gy + gh + 1, -15987440);
      g.fill(gx, gy, gx + gw, gy + gh, -15066078);

      for(int i = 1; i < 4; ++i) {
         g.fill(gx, gy + gh * i / 4, gx + gw, gy + gh * i / 4 + 1, -14539475);
      }

      int n = VoiceClient.HISTORY.length;
      int hp = VoiceClient.histPos;
      float colW = (float)gw / (float)n;

      for(int i = 0; i < n; ++i) {
         int k = (hp + i) % n;
         float v = Math.min(1.0F, VoiceClient.HISTORY[k] * 6.0F);
         int h = Math.max(1, (int)(v * (float)(gh - 2)));
         int x = gx + (int)((float)i * colW);
         g.fill(x, gy + gh - h, x + Math.max(1, (int)colW), gy + gh, VoiceClient.HIST_SPEAK[k] ? -11214736 : -10853776);
      }

      if (!VoiceConfig.pushToTalk) {
         int ty = gy + gh - (int)(Math.min(1.0F, Math.max(VoiceConfig.threshold, VoiceClient.floor * 2.6F + 0.0015F) * 6.0F) * (float)(gh - 2));

         for(int x = gx; x < gx + gw; x += 4) {
            g.fill(x, ty, x + 2, ty + 1, -2078648);
         }

         g.drawString(this.font, "threshold", gx + gw - 46, ty - 9, -2078648, false);
      }

      float db = VoiceClient.level > 1.0E-5F ? (float)((double)20.0F * Math.log10((double)VoiceClient.level)) : -99.0F;
      g.drawString(this.font, String.format(Locale.ROOT, "Input %.0f dB", db), gx + 4, gy + 3, -7695456, false);
      if (VoiceClient.error != null) {
         g.drawString(this.font, this.font.plainSubstrByWidth(VoiceClient.error, 296), this.x0 + 12, this.y0 + 209, -2056144, false);
      }

   }

   public void onClose() {
      VoiceConfig.save();
      super.onClose();
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static class Slider extends AbstractSliderButton {
      private final String label;
      private final DoubleConsumer cb;
      private final DoubleFunction<String> fmt;

      Slider(int x, int y, int w, String label, double v, DoubleFunction<String> fmt, DoubleConsumer cb) {
         super(x, y, w, 20, Component.empty(), v);
         this.label = label;
         this.cb = cb;
         this.fmt = fmt;
         this.updateMessage();
      }

      protected void updateMessage() {
         String var10001 = this.label;
         this.setMessage(Component.literal(var10001 + ": " + (String)this.fmt.apply(this.value)));
      }

      protected void applyValue() {
         this.cb.accept(this.value);
      }
   }
}
