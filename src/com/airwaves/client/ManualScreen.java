package com.airwaves.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

public class ManualScreen extends Screen {
   private static final int W = 364;
   private static final int H = 244;
   private static final int SIDE = 116;
   private static final int LINE = 10;
   private static final int CX = 130;
   private static final int CW = 222;
   private static final int BODY_TOP = 36;
   private static final int FOOT = 22;
   private static final int INK = -12899814;
   private static final int INK_SOFT = -9546184;
   private static final int HEAD = -8765676;
   private static final int PAPER = -924474;
   private static final int PAPER_DARK = -1780818;
   private static final int SIDEBAR = -1649232;
   private static final int COVER = -10798558;
   private static final int COVER_EDGE = -12966892;
   private static final int BRASS = -2644662;
   private static final int BRASS_DARK = -7576540;
   private static final int BRASS_LIGHT = -995456;
   private static final int LINK = -14721396;
   private static final int LINK_HOVER = -13728056;
   private static final int COVER_HI = -9549268;
   private static final int RULE = 1434074900;
   private static final int TIP_BG = 862036042;
   private static final int TIP_BAR = -11630022;
   private static final int SEL = 1087874378;
   private final List<Chapter> chapters = new ArrayList();
   private final Map<String, List<Integer>> groups = new LinkedHashMap();
   private final List<Hit> hits = new ArrayList();
   private static int lastChapter = 0;
   private static int lastPage = 0;
   private int chapter;
   private int page;

   public static void open() {
      Minecraft.getInstance().setScreen(new ManualScreen());
   }

   protected ManualScreen() {
      super(Component.translatable("item.airwaves.field_manual"));
   }

   private int bodyLines() {
      return 18;
   }

   protected void init() {
      this.chapters.clear();
      this.groups.clear();

      for(Chapter c : this.load()) {
         this.layout(c);
         ((List)this.groups.computeIfAbsent(c.group(), (k) -> new ArrayList())).add(this.chapters.size());
         this.chapters.add(c);
      }

      this.chapter = Math.max(0, Math.min(this.chapters.size() - 1, lastChapter));
      this.page = this.chapters.isEmpty() ? 0 : Math.max(0, Math.min(((Chapter)this.chapters.get(this.chapter)).pages().size() - 1, lastPage));
   }

   private void layout(Chapter c) {
      int per = this.bodyLines();
      List<Line> pg = new ArrayList();
      int used = c.icons().isEmpty() ? 0 : 2;
      List<List<Line>> pages = c.pages();

      for(String para : c.paragraphs()) {
         if (para.startsWith("@fig:")) {
            String[] fp = para.split(":");
            int n = fp.length > 2 ? Math.max(3, Math.min(per, Integer.parseInt(fp[2].trim()))) : 6;
            if (used + pg.size() + n > per) {
               pages.add(pg);
               pg = new ArrayList();
               used = 0;
            }

            pg.add(new Line(FormattedCharSequence.EMPTY, -12899814, 0, false, false, (String)null, fp[1], n));

            for(int k = 1; k < n; ++k) {
               pg.add(blank());
            }

            if (used + pg.size() < per) {
               pg.add(blank());
            }
         } else {
            boolean sub = para.startsWith("## ");
            boolean tip = para.startsWith("> ");
            String bullet = null;
            int indent = 0;
            String text = para;
            if (sub) {
               text = para.substring(3);
            } else if (tip) {
               text = para.substring(2);
               indent = 6;
            } else if (para.startsWith("- ")) {
               bullet = "•";
               text = para.substring(2);
               indent = 8;
            } else {
               int dot = para.indexOf(". ");
               if (dot > 0 && dot <= 2 && para.substring(0, dot).chars().allMatch(Character::isDigit)) {
                  bullet = para.substring(0, dot + 1);
                  text = para.substring(dot + 2);
                  indent = 11;
               }
            }

            Component comp = (Component)(sub ? Component.literal(text).withStyle((s) -> s.withBold(true)) : rich(text));
            List<FormattedCharSequence> ls = this.font.split(comp, 222 - indent - (tip ? 4 : 0));
            if (sub && used + pg.size() + 2 > per) {
               pages.add(pg);
               pg = new ArrayList();
               used = 0;
            }

            if (tip && ls.size() <= per && used + pg.size() + ls.size() > per) {
               pages.add(pg);
               pg = new ArrayList();
               used = 0;
            }

            for(int i = 0; i < ls.size(); ++i) {
               if (used + pg.size() >= per) {
                  pages.add(pg);
                  pg = new ArrayList();
                  used = 0;
               }

               pg.add(new Line((FormattedCharSequence)ls.get(i), sub ? -8765676 : -12899814, indent, sub, tip, i == 0 ? bullet : null, (String)null, 0));
            }

            boolean nextIsList = false;
            int idx = c.paragraphs().indexOf(para);
            if (bullet != null && idx >= 0 && idx + 1 < c.paragraphs().size()) {
               String nx = (String)c.paragraphs().get(idx + 1);
               nextIsList = nx.startsWith("- ") || nx.indexOf(". ") > 0 && nx.indexOf(". ") <= 2 && nx.substring(0, nx.indexOf(". ")).chars().allMatch(Character::isDigit);
            }

            if (!sub && !nextIsList && used + pg.size() < per && used + pg.size() > 0) {
               pg.add(blank());
            }
         }
      }

      while(!pg.isEmpty() && ((Line)pg.get(pg.size() - 1)).text() == FormattedCharSequence.EMPTY && ((Line)pg.get(pg.size() - 1)).fig() == null) {
         pg.remove(pg.size() - 1);
      }

      pages.add(pg);
   }

   private static Line blank() {
      return new Line(FormattedCharSequence.EMPTY, -12899814, 0, false, false, (String)null, (String)null, 0);
   }

   private static Component rich(String s) {
      MutableComponent out = Component.empty();
      boolean bold = false;

      for(String part : s.split("\\*\\*", -1)) {
         if (!part.isEmpty()) {
            out.append(Component.literal(part).withStyle((st) -> st.withBold(bold).withColor(bold ? 5910030 : 3877402)));
         }

         bold = !bold;
      }

      return out;
   }

   private List<Chapter> load() {
      List<Chapter> out = new ArrayList();
      String lang = Minecraft.getInstance().getLanguageManager().getSelected();
      JsonArray arr = read(lang);
      if (arr == null) {
         arr = read("en_us");
      }

      if (arr == null) {
         return out;
      } else {
         for(JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            List<ItemStack> icons = new ArrayList();
            if (o.has("icons")) {
               for(JsonElement i : o.getAsJsonArray("icons")) {
                  Item it = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse(i.getAsString()));
                  if (it != Items.AIR) {
                     icons.add(new ItemStack(it));
                  }
               }
            }

            List<String> paras = new ArrayList();

            for(JsonElement p : o.getAsJsonArray("text")) {
               paras.add(p.getAsString());
            }

            String title = o.get("title").getAsString();
            out.add(new Chapter(title, o.has("short") ? o.get("short").getAsString() : title, o.has("group") ? o.get("group").getAsString() : "Manual", icons, paras, new ArrayList()));
         }

         return out;
      }
   }

   private static JsonArray read(String lang) {
      try {
         Optional<Resource> r = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("airwaves", "manual/" + lang + ".json"));
         if (r.isEmpty()) {
            return null;
         } else {
            InputStreamReader in = new InputStreamReader(((Resource)r.get()).open(), StandardCharsets.UTF_8);

            JsonArray var3;
            try {
               var3 = JsonParser.parseReader(in).getAsJsonArray();
            } catch (Throwable var6) {
               try {
                  in.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            in.close();
            return var3;
         }
      } catch (Exception var7) {
         return null;
      }
   }

   private void go(int ch, int pg) {
      if (!this.chapters.isEmpty()) {
         ch = Math.max(0, Math.min(this.chapters.size() - 1, ch));
         int n = ((Chapter)this.chapters.get(ch)).pages().size();
         pg = pg < 0 ? n - 1 : Math.min(n - 1, pg);
         if (ch != this.chapter || pg != this.page) {
            this.chapter = ch;
            this.page = pg;
            lastChapter = ch;
            lastPage = pg;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
         }
      }
   }

   private void next() {
      if (this.page + 1 < this.cur().pages().size()) {
         this.go(this.chapter, this.page + 1);
      } else if (this.chapter + 1 < this.chapters.size()) {
         this.go(this.chapter + 1, 0);
      }

   }

   private void prev() {
      if (this.page > 0) {
         this.go(this.chapter, this.page - 1);
      } else if (this.chapter > 0) {
         this.go(this.chapter - 1, -1);
      }

   }

   private Chapter cur() {
      return (Chapter)this.chapters.get(this.chapter);
   }

   private float scale() {
      return Math.min(1.0F, Math.min(((float)this.width - 8.0F) / 380.0F, ((float)this.height - 8.0F) / 260.0F));
   }

   private int left() {
      return (int)(((float)this.width / this.scale() - 364.0F) / 2.0F);
   }

   private int top() {
      return (int)(((float)this.height / this.scale() - 244.0F) / 2.0F);
   }

   public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
      this.renderTransparentBackground(g);
   }

   public void render(GuiGraphics g, int rmx, int rmy, float pt) {
      super.render(g, rmx, rmy, pt);
      float sc = this.scale();
      int mx = (int)((float)rmx / sc);
      int my = (int)((float)rmy / sc);
      this.hits.clear();
      g.pose().pushPose();
      g.pose().scale(sc, sc, 1.0F);
      int x = this.left();
      int y = this.top();
      g.fill(x - 8, y - 7, x + 364 + 8, y + 244 + 8, -12966892);
      g.fillGradient(x - 7, y - 6, x + 364 + 7, y + 244 + 7, -9549268, -10798558);

      for(int[] c : new int[][]{{x - 8, y - 7}, {x + 364 - 2, y - 7}, {x - 8, y + 244 - 2}, {x + 364 - 2, y + 244 - 2}}) {
         g.fill(c[0], c[1], c[0] + 10, c[1] + 10, -7576540);
         g.fill(c[0] + 1, c[1] + 1, c[0] + 9, c[1] + 9, -2644662);
         g.fill(c[0] + 1, c[1] + 1, c[0] + 9, c[1] + 2, -995456);
         g.fill(c[0] + 4, c[1] + 4, c[0] + 6, c[1] + 6, -7576540);
      }

      g.fill(x, y, x + 364, y + 244, -924474);
      g.fillGradient(x + 116, y, x + 364, y + 244, -594732, -924474);
      g.fill(x, y, x + 116, y + 244, -1649232);

      for(int k = 0; k < 5; ++k) {
         g.fill(x + 116 + k, y, x + 116 + k + 1, y + 244, (int)((double)60.0F * ((double)1.0F - (double)k / (double)5.0F)) << 24 | 5914656);
      }

      g.fill(x + 116 - 1, y, x + 116, y + 244, -4678032);
      if (!this.chapters.isEmpty()) {
         this.sidebar(g, x, y, mx, my);
         this.content(g, x, y, mx, my);
      }

      ItemStack hs = null;
      if (!this.chapters.isEmpty() && this.page == 0) {
         List<ItemStack> ic = this.cur().icons();

         for(int k = 0; k < ic.size(); ++k) {
            int ix = x + 130 + k * 19;
            int iy = y + 36;
            if (mx >= ix && mx < ix + 16 && my >= iy && my < iy + 16) {
               hs = (ItemStack)ic.get(k);
            }
         }
      }

      g.pose().popPose();
      if (hs != null) {
         g.renderTooltip(this.font, hs, rmx, rmy);
      }

   }

   private void sidebar(GuiGraphics g, int x, int y, int mx, int my) {
      int sx = x + 7;
      int w = 104;
      g.fill(sx - 2, y + 6, sx + w + 2, y + 22, -7576540);
      g.fill(sx - 1, y + 7, sx + w + 1, y + 21, -2644662);
      g.fill(sx - 1, y + 7, sx + w + 1, y + 8, -995456);
      String t = "FIELD MANUAL";
      g.drawString(this.font, t, sx + (w - this.font.width(t)) / 2, y + 10, -11915248, false);
      int ry = y + 28;
      String openGroup = this.cur().group();

      for(Map.Entry<String, List<Integer>> grp : this.groups.entrySet()) {
         boolean open = ((String)grp.getKey()).equals(openGroup);
         int first = (Integer)((List)grp.getValue()).get(0);
         boolean hov = mx >= sx - 2 && mx < sx + w + 2 && my >= ry - 1 && my < ry + 13;
         if (open) {
            g.fill(sx - 2, ry - 1, sx + w + 2, ry + 13, 814507044);
         } else if (hov) {
            g.fill(sx - 2, ry - 1, sx + w + 2, ry + 13, 402653184);
         }

         ItemStack icon = ((Chapter)this.chapters.get(first)).icons().isEmpty() ? ItemStack.EMPTY : (ItemStack)((Chapter)this.chapters.get(first)).icons().get(0);
         if (!icon.isEmpty()) {
            g.pose().pushPose();
            g.pose().translate((float)sx, (float)ry, 0.0F);
            g.pose().scale(0.75F, 0.75F, 1.0F);
            g.renderItem(icon, 0, 0);
            g.pose().popPose();
         }

         String name = this.font.plainSubstrByWidth((String)grp.getKey(), w - 26);
         g.drawString(this.font, name, sx + 15, ry + 2, open ? -8765676 : (hov ? -13728056 : -12899814), false);
         g.drawString(this.font, open ? "▾" : "▸", sx + w - 6, ry + 2, open ? -8765676 : -9546184, false);
         this.hits.add(new Hit(sx - 2, ry - 1, sx + w + 2, ry + 13, () -> this.go(first, 0)));
         ry += 14;
         if (open) {
            for(int ci : (List)grp.getValue()) {
               boolean sel = ci == this.chapter;
               boolean h2 = mx >= sx + 6 && mx < sx + w + 2 && my >= ry - 1 && my < ry + 10;
               if (sel) {
                  g.fill(sx + 6, ry - 1, sx + w + 2, ry + 10, 1087874378);
                  g.fill(sx + 6, ry - 1, sx + 8, ry + 10, -7576540);
               }

               String cn = this.font.plainSubstrByWidth(((Chapter)this.chapters.get(ci)).shortTitle(), w - 14);
               g.drawString(this.font, cn, sx + 11, ry + 1, sel ? -8765676 : (h2 ? -13728056 : -14721396), false);
               this.hits.add(new Hit(sx + 6, ry - 1, sx + w + 2, ry + 10, () -> this.go(ci, 0)));
               ry += 11;
            }

            ry += 3;
         }
      }

      String release = (String)ModList.get().getModContainerById("airwaves").map((c) -> c.getModInfo().getVersion().toString()).orElse("");
      String v = "v" + release + " · build 9.9";
      g.pose().pushPose();
      g.pose().translate((float)sx, (float)(y + 244 - 10), 0.0F);
      g.pose().scale(0.6F, 0.6F, 1.0F);
      g.drawString(this.font, v, 0, 0, -6650784, false);
      g.pose().popPose();
   }

   private void content(GuiGraphics g, int x, int y, int mx, int my) {
      Chapter c = this.cur();
      int px = x + 130;
      g.drawString(this.font, c.group(), px, y + 6, -6256028, false);
      g.drawString(this.font, Component.literal(this.font.plainSubstrByWidth(c.title(), 222)).withStyle((s) -> s.withBold(true)), px, y + 17, -8765676, false);
      g.fill(px, y + 28, px + 222, y + 29, 1434074900);
      int py = y + 36;
      if (this.page == 0 && !c.icons().isEmpty()) {
         for(int k = 0; k < c.icons().size(); ++k) {
            int ix = px + k * 19;
            g.fill(ix - 1, py - 1, ix + 17, py + 17, -7576540);
            g.fill(ix, py, ix + 16, py + 16, -1780818);
            g.fill(ix, py, ix + 16, py + 1, -2854);
            g.renderItem((ItemStack)c.icons().get(k), ix, py);
         }

         py += 20;
      }

      float t = (float)(Util.getMillis() % 3600000L) / 1000.0F;
      List<Line> lines = (List)c.pages().get(Math.min(this.page, c.pages().size() - 1));

      for(int i = 0; i < lines.size(); ++i) {
         Line l = (Line)lines.get(i);
         if (l.fig() != null) {
            ManualFigures.draw(g, this.font, l.fig(), px, py, 222, l.figLines() * 10 - 2, t);
         } else {
            if (l.tip()) {
               boolean first = i == 0 || !((Line)lines.get(i - 1)).tip();
               boolean last = i + 1 >= lines.size() || !((Line)lines.get(i + 1)).tip();
               g.fill(px, py - (first ? 2 : 1), px + 222, py + 10 - (last ? 0 : 1), 862036042);
               g.fill(px, py - (first ? 2 : 1), px + 2, py + 10 - (last ? 0 : 1), -11630022);
            }

            if (l.bullet() != null) {
               g.drawString(this.font, l.bullet(), px + (l.bullet().length() > 1 ? 0 : 2), py, l.bullet().length() > 1 ? -8765676 : -9546184, false);
            }

            g.drawString(this.font, l.text(), px + l.indent() + (l.tip() ? 2 : 0), py, l.color(), false);
            if (l.heading()) {
               g.fill(px, py + 10 - 1, px + Math.min(222, this.font.width(l.text())), py + 10, 1434074900);
            }
         }

         py += 10;
      }

      int fy = y + 244 - 22 + 4;
      g.fill(px, fy - 4, px + 222, fy - 3, 1434074900);
      int n = c.pages().size();
      String mid = n > 1 ? "page " + (this.page + 1) + " / " + n : "";
      g.drawString(this.font, mid, px + (222 - this.font.width(mid)) / 2, fy + 2, -9546184, false);
      boolean hasPrev = this.page > 0 || this.chapter > 0;
      boolean hasNext = this.page + 1 < n || this.chapter + 1 < this.chapters.size();
      if (hasPrev) {
         String lb = this.page > 0 ? "◀ Back" : "◀ " + ((Chapter)this.chapters.get(this.chapter - 1)).shortTitle();
         this.button(g, px, fy, lb, 81, mx, my, false, this::prev);
      }

      if (hasNext) {
         String lb = this.page + 1 < n ? "More ▶" : ((Chapter)this.chapters.get(this.chapter + 1)).shortTitle() + " ▶";
         this.button(g, px + 222, fy, lb, 81, mx, my, true, this::next);
      }

   }

   private void button(GuiGraphics g, int ax, int ay, String label, int maxW, int mx, int my, boolean right, Runnable act) {
      String s = label;
      if (this.font.width(label) > maxW - 8) {
         boolean arrowEnd = label.endsWith("▶");
         String core = arrowEnd ? label.substring(0, label.length() - 2) : label.substring(2);
         String var10000 = this.font.plainSubstrByWidth(core, maxW - 8 - this.font.width("… ▶"));
         core = var10000 + "…";
         s = arrowEnd ? core + " ▶" : "◀ " + core;
      }

      int bw = this.font.width(s) + 8;
      int x0 = right ? ax - bw : ax;
      int x1 = x0 + bw;
      int y0 = ay - 1;
      int y1 = ay + 11;
      boolean hov = mx >= x0 && mx < x1 && my >= y0 && my < y1;
      g.fill(x0, y0, x1, y1, -7576540);
      g.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, hov ? -995456 : -2644662);
      g.drawString(this.font, s, x0 + 4, ay + 1, -12966896, false);
      this.hits.add(new Hit(x0, y0, x1, y1, act));
   }

   public boolean mouseClicked(double rmx, double rmy, int button) {
      double mx = rmx / (double)this.scale();
      double my = rmy / (double)this.scale();

      for(Hit h : List.copyOf(this.hits)) {
         if (mx >= (double)h.x0() && mx < (double)h.x1() && my >= (double)h.y0() && my < (double)h.y1()) {
            h.action().run();
            return true;
         }
      }

      return super.mouseClicked(rmx, rmy, button);
   }

   public boolean mouseScrolled(double mx, double my, double sx, double sy) {
      if (sy > (double)0.0F) {
         this.prev();
      } else if (sy < (double)0.0F) {
         this.next();
      }

      return true;
   }

   public boolean keyPressed(int key, int sc, int mods) {
      if (this.chapters.isEmpty()) {
         return super.keyPressed(key, sc, mods);
      } else if (key == 263) {
         this.prev();
         return true;
      } else if (key == 262) {
         this.next();
         return true;
      } else if (key != 265 && key != 266) {
         if (key != 264 && key != 267) {
            if (key == 268) {
               this.go(0, 0);
               return true;
            } else {
               return super.keyPressed(key, sc, mods);
            }
         } else {
            this.go(this.chapter + 1, 0);
            return true;
         }
      } else {
         this.go(this.chapter - 1, 0);
         return true;
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static record Line(FormattedCharSequence text, int color, int indent, boolean heading, boolean tip, String bullet, String fig, int figLines) {
   }

   private static record Chapter(String title, String shortTitle, String group, List<ItemStack> icons, List<String> paragraphs, List<List<Line>> pages) {
   }

   private static record Hit(int x0, int y0, int x1, int y1, Runnable action) {
   }
}
