package com.airwaves.client;

import com.airwaves.logic.BoardAnalysis;
import com.airwaves.logic.BoardDesign;
import com.airwaves.logic.BoardLayout;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public final class BoardIcons {
   private static final int GX = 3;
   private static final int GY = 8;
   private static final int N = 0;
   private static final int FX0 = 1;
   private static final int FX1 = 28;
   private static final int FY0 = 6;
   private static final int FY1 = 24;
   private static final Map<String, ResourceLocation> CACHE = new LinkedHashMap<String, ResourceLocation>(64, 0.75F, true) {
      protected boolean removeEldestEntry(Map.Entry<String, ResourceLocation> e) {
         if (this.size() <= 256) {
            return false;
         } else {
            Minecraft.getInstance().getTextureManager().release((ResourceLocation)e.getValue());
            return true;
         }
      }
   };
   private static final int PCB = rgb(34, 110, 62);
   private static final int PCB_HI = rgb(52, 140, 82);
   private static final int PCB_LO = rgb(20, 72, 40);
   private static final int PCB_DK = rgb(14, 52, 30);
   private static final int GOLD = rgb(230, 186, 80);
   private static final int GOLD_D = rgb(170, 128, 46);
   private static final int COPPER = rgb(196, 120, 60);
   private static final int COPPER_L = rgb(240, 170, 90);
   private static final int TAN = rgb(220, 196, 150);
   private static final int TAN_D = rgb(170, 140, 96);
   private static final int BAND = rgb(150, 40, 34);
   private static final int BLU = rgb(60, 104, 200);
   private static final int BLU_L = rgb(120, 160, 240);
   private static final int BLU_D = rgb(36, 64, 130);
   private static final int RED = rgb(220, 50, 40);
   private static final int RED_L = rgb(255, 150, 120);
   private static final int RED_OFF = rgb(110, 30, 26);
   private static final int LEG = rgb(150, 150, 160);
   private static final int BLK = rgb(40, 40, 46);
   private static final int BLK_L = rgb(90, 90, 100);
   private static final int GLS = rgb(200, 220, 230);
   private static final int ORG = rgb(255, 160, 60);
   private static final int COP = rgb(200, 110, 60);
   private static final int COP_L = rgb(250, 170, 100);
   private static final int COP_D = rgb(140, 70, 36);
   private static final int SIL = rgb(196, 200, 210);
   private static final int SIL_L = rgb(240, 244, 250);
   private static final int SIL_D = rgb(130, 134, 146);
   private static final int PUR = rgb(170, 110, 230);
   private static final int YEL = rgb(250, 210, 70);
   private static final int WHT = rgb(236, 236, 240);
   private static final int SKY = rgb(90, 170, 250);
   private static final int FACE = rgb(47, 138, 92);
   private static final int FACE_HI = rgb(68, 168, 104);
   private static final int EDGE = rgb(36, 84, 65);
   private static final int EDGE_D = rgb(23, 58, 46);
   private static final int BOT_D = rgb(15, 38, 30);
   private static final int G_HI = rgb(247, 208, 96);
   private static final int G = rgb(236, 182, 70);
   private static final int G_MID = rgb(224, 175, 84);
   private static final int G_LO = rgb(206, 160, 90);
   private static final int G_DK = rgb(158, 105, 71);
   private static final int G_PIN = rgb(247, 203, 108);
   private static final int G_PIN_D = rgb(181, 130, 60);
   private static final int[][][] SPRITES;
   private static final int K = 3;
   private static final int SZ = 48;
   private static final int SHADOW;
   private static final ResourceLocation TEMPLATE;
   private static NativeImage template;
   private static boolean templateTried;
   private static final int BOARD;
   private static final int FACE_RING;
   private static final int T_HI;
   private static final int T_MID;
   private static final int T_LO;

   private BoardIcons() {
   }

   private static int rgb(int r, int g, int b) {
      return -16777216 | r << 16 | g << 8 | b;
   }

   private static void put(NativeImage im, int x, int y, int argb) {
      if (argb != 0 && x >= 0 && y >= 0 && x < 48 && y < 48) {
         int a = argb >>> 24;
         int r = argb >> 16 & 255;
         int g = argb >> 8 & 255;
         int b = argb & 255;
         im.setPixelRGBA(x, y, a << 24 | b << 16 | g << 8 | r);
      }
   }

   public static ResourceLocation texture(BoardLayout layout) {
      ResourceLocation rl = (ResourceLocation)CACHE.get(layout.cells());
      if (rl != null) {
         return rl;
      } else {
         NativeImage im = new NativeImage(48, 48, true);
         draw(im, layout);
         String var10001 = Integer.toHexString(layout.cells().hashCode());
         rl = ResourceLocation.fromNamespaceAndPath("airwaves", "board_icon/" + var10001 + "_" + CACHE.size());
         Minecraft.getInstance().getTextureManager().register(rl, new DynamicTexture(im));
         CACHE.put(layout.cells(), rl);
         return rl;
      }
   }

   private static void base(NativeImage im) {
      for(int x = 1; x <= 28; ++x) {
         for(int y = 6; y <= 24; ++y) {
            put(im, x, y, FACE);
         }
      }

      for(int x = 2; x < 28; ++x) {
         put(im, x, 7, FACE_HI);
         put(im, x, 23, FACE_HI);
      }

      for(int y = 7; y < 24; ++y) {
         put(im, 2, y, FACE_HI);
         put(im, 27, y, FACE_HI);
      }

      for(int y = 6; y <= 26; ++y) {
         put(im, 29, y, EDGE);
         put(im, 30, y, EDGE_D);
      }

      for(int x = 1; x <= 28; ++x) {
         put(im, x, 25, EDGE);
         put(im, x, 26, EDGE_D);
      }

      put(im, 29, 26, BOT_D);
      put(im, 30, 26, BOT_D);

      for(int x = 5; x < 26; x += 2) {
         put(im, x, 3, G_PIN);
         put(im, x, 4, G_PIN);
         put(im, x, 5, G_PIN_D);
      }

      for(int x : new int[]{9, 15, 21}) {
         put(im, x, 25, G_LO);
         put(im, x, 26, G_DK);
      }

   }

   private static NativeImage template() {
      if (!templateTried) {
         templateTried = true;

         try {
            Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(TEMPLATE);
            if (res.isPresent()) {
               InputStream in = ((Resource)res.get()).open();

               try {
                  template = NativeImage.read(in);
               } catch (Throwable var5) {
                  if (in != null) {
                     try {
                        in.close();
                     } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                     }
                  }

                  throw var5;
               }

               if (in != null) {
                  in.close();
               }
            }
         } catch (Exception var6) {
            template = null;
         }
      }

      return template;
   }

   private static void hline(NativeImage im, int x0, int x1, int y) {
      for(int x = Math.min(x0, x1); x <= Math.max(x0, x1); ++x) {
         put(im, x, y, T_HI);
         put(im, x, y + 1, T_LO);
      }

   }

   private static void vline(NativeImage im, int x, int y0, int y1) {
      for(int y = Math.min(y0, y1); y <= Math.max(y0, y1); ++y) {
         put(im, x, y, T_MID);
         put(im, x + 1, y, T_LO);
      }

   }

   static void draw(NativeImage im, BoardLayout layout) {
      int W = 8;
      NativeImage t = template();
      if (t != null) {
         for(int y = 0; y < 48; ++y) {
            for(int x = 0; x < 48; ++x) {
               int tx = x * t.getWidth() / 48;
               int ty = y * t.getHeight() / 48;
               int c = t.getPixelRGBA(tx, ty);
               int bx = tx * 16 / t.getWidth();
               int by = ty * 16 / t.getHeight();
               if (bx >= 2 && bx <= 12 && by >= 3 && by <= 10 && c >>> 24 != 0 && (c & 255) > (c >> 8 & 255)) {
                  boolean ring = (bx == 3 || bx == 11) && by >= 4 && by <= 9 || (by == 4 || by == 9) && bx >= 3 && bx <= 11;
                  put(im, x, y, ring ? FACE_RING : BOARD);
               } else {
                  im.setPixelRGBA(x, y, c);
               }
            }
         }
      } else {
         base(im);
      }

      int[] parts = layout.parts();
      BoardAnalysis.Result a = BoardAnalysis.analyse(parts, layout.traces());
      int count = 0;

      for(int p : parts) {
         if (p >= 0) {
            ++count;
         }
      }

      boolean big = count <= 12;
      int[][] pos = new int[parts.length][];
      int n = 0;
      TreeMap<Integer, List<Integer>> rows = new TreeMap();

      for(int i = 0; i < parts.length; ++i) {
         if (parts[i] >= 0) {
            if (big) {
               pos[i] = new int[]{2, 9 + n % 4 * 7 + 1, 12 + n / 4 * 7};
            } else {
               pos[i] = new int[]{1, 9 + i % W * 3, 12 + i / W * 3};
            }

            if (big) {
               ((List)rows.computeIfAbsent(pos[i][2], (k) -> new ArrayList())).add(pos[i][1]);
            }

            ++n;
         }
      }

      if (big && !rows.isEmpty()) {
         Random rnd = new Random((long)layout.cells().hashCode());
         int lx = 7;
         int first = (Integer)rows.firstKey();
         int last = (Integer)rows.lastKey();
         vline(im, lx, 9, last + 2);

         for(Map.Entry<Integer, List<Integer>> e : rows.entrySet()) {
            int cy = (Integer)e.getKey() + 2;
            int right = (Integer)Collections.max((Collection)e.getValue()) + 3;
            hline(im, lx, right, cy);
            if (rnd.nextFloat() < 0.7F) {
               hline(im, right, 38, cy);
               vline(im, 37, cy, cy < 22 ? 13 : 30);
            }
         }

         for(int x0 : (List)rows.get(first)) {
            if (rnd.nextFloat() < 0.55F) {
               vline(im, x0 + 2, 9, first);
            }
         }

         int by = Math.min(31, last + 8);
         int bx = rnd.nextBoolean() ? 19 : 37;
         hline(im, lx, bx, by);
         vline(im, bx, by, 33);
      }

      for(int i = 0; i < parts.length; ++i) {
         if (parts[i] >= 0) {
            int s = pos[i][0];
            int x0 = pos[i][1];
            int y0 = pos[i][2];
            int[][] spr = SPRITES[parts[i]];

            for(int pass = 0; pass < 2; ++pass) {
               for(int yy = 0; yy < 3; ++yy) {
                  for(int xx = 0; xx < 3; ++xx) {
                     int c = spr[yy][xx];
                     if (c != 0) {
                        if (parts[i] == BoardDesign.Part.LED.ordinal() && !a.works() && (c == RED || c == RED_L)) {
                           c = RED_OFF;
                        }

                        int o = pass == 0 ? 1 : 0;
                        int cc = pass == 0 ? SHADOW : c;

                        for(int dy = 0; dy < s; ++dy) {
                           for(int dx = 0; dx < s; ++dx) {
                              put(im, x0 + xx * s + dx + o, y0 + yy * s + dy + o, cc);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   static {
      SPRITES = new int[][][]{{{0, TAN_D, 0}, {TAN, BAND, TAN}, {0, TAN_D, 0}}, {{0, BLU_L, 0}, {BLU, BLU_L, BLU}, {BLU_D, BLU_D, BLU_D}}, {{0, RED, 0}, {RED, RED_L, RED}, {0, LEG, 0}}, {{BLK, BLK, BLK}, {BLK, BLK_L, BLK}, {LEG, 0, LEG}}, {{0, GLS, 0}, {GLS, ORG, GLS}, {LEG, LEG, LEG}}, {{COP, COP_L, COP}, {COP_D, COP, COP_D}, {COP, COP_L, COP}}, {{SIL, SIL_L, SIL}, {SIL, PUR, SIL}, {SIL_D, SIL_D, SIL_D}}, {{LEG, LEG, LEG}, {BLK, BLU_L, BLK}, {LEG, LEG, LEG}}, {{LEG, LEG, LEG}, {BLK, YEL, BLK}, {LEG, LEG, LEG}}, {{LEG, LEG, LEG}, {BLK, RED, BLK}, {LEG, LEG, LEG}}, {{0, YEL, 0}, {TAN, BAND, TAN}, {0, LEG, 0}}, {{0, SKY, 0}, {COP, BLU, COP}, {PCB_HI, COP, PCB_HI}}, {{0, WHT, 0}, {WHT, SIL, WHT}, {SIL_D, SIL_D, SIL_D}}};
      SHADOW = rgb(20, 50, 36);
      TEMPLATE = ResourceLocation.fromNamespaceAndPath("airwaves", "textures/item/circuit_board.png");
      BOARD = rgb(47, 138, 92);
      FACE_RING = rgb(68, 168, 104);
      T_HI = rgb(236, 182, 70);
      T_MID = rgb(224, 175, 84);
      T_LO = rgb(206, 160, 90);
   }
}
