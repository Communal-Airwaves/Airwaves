package com.airwaves.client.jei;

import com.airwaves.ModRegistry;
import com.airwaves.client.ProgramFlow;
import com.airwaves.logic.BoardAnalysis;
import com.airwaves.logic.BoardDesign;
import com.airwaves.logic.BoardLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class BoardRecipeCategory implements IRecipeCategory<Recipe> {
   public static final RecipeType<Recipe> TYPE = RecipeType.create("airwaves", "circuit_board", Recipe.class);
   private static final int W = 206;
   private static final int H = 128;
   private static final int CELL = 10;
   private static final int BX = 13;
   private static final int BY = 10;
   private static final int PANEL = -3750202;
   private static final int HI = -1;
   private static final int LO = -11184811;
   private static final int PCB = -14718150;
   private static final int PCB_HI = -13727154;
   private static final int PCB_LO = -15448278;
   private static final int PAD = -3634628;
   private static final int PAD_LIVE = -15254;
   private static final int HOLE = -15184083;
   private static final int UNDER = -14016234;
   private static final int TEXT = -12959928;
   private static final int STEP = -14721396;
   private final IDrawable icon;

   public BoardRecipeCategory(IGuiHelper gui) {
      this.icon = gui.createDrawableItemStack(new ItemStack((ItemLike)ModRegistry.CIRCUIT_WORKBENCH_ITEM.get()));
   }

   public RecipeType<Recipe> getRecipeType() {
      return TYPE;
   }

   public Component getTitle() {
      return Component.translatable("block.airwaves.circuit_workbench");
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public int getWidth() {
      return 206;
   }

   public int getHeight() {
      return 128;
   }

   private static ItemStack board(Recipe r) {
      ItemStack st = new ItemStack(r.design().item());
      st.set((DataComponentType)ModRegistry.BOARD_LAYOUT.get(), r.layout());
      return st;
   }

   public void setRecipe(IRecipeLayoutBuilder b, Recipe r, IFocusGroup focuses) {
      Map<BoardDesign.Part, Integer> n = new LinkedHashMap();

      for(int p : r.layout().parts()) {
         if (p >= 0) {
            n.merge(BoardDesign.Part.values()[p], 1, Integer::sum);
         }
      }

      int x = 2;
      b.addInputSlot(x, 76).setStandardSlotBackground().addItemStack(new ItemStack((ItemLike)ModRegistry.CIRCUIT_BOARD.get()));
      x += 20;

      for(Map.Entry<BoardDesign.Part, Integer> e : n.entrySet()) {
         if (x > 162) {
            break;
         }

         b.addInputSlot(x, 76).setStandardSlotBackground().addItemStack(new ItemStack(((BoardDesign.Part)e.getKey()).item(), (Integer)e.getValue()));
         x += 18;
      }

      b.addOutputSlot(186, 76).setStandardSlotBackground().addItemStack(board(r));
   }

   public void draw(Recipe r, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
      Font font = Minecraft.getInstance().font;
      int[] parts = r.layout().parts();
      boolean[] tr = r.layout().traces();
      BoardAnalysis.Result a = BoardAnalysis.analyse(parts, tr);
      float time = (float)(System.currentTimeMillis() % 100000L) / 50.0F;
      bevel(g, 0, 0, 105, 68, -3750202, -1, -11184811);
      int x0 = 4;
      int y0 = 5;
      int x1 = 102;
      int y1 = 65;
      bevel(g, x0, y0, x1, y1, -14718150, -13727154, -15448278);

      for(int row = 0; row < 5; ++row) {
         int cy = 10 + row * 10;
         g.fill(6, cy + 3, 11, cy + 7, a.live()[row * 8] ? -15254 : -3634628);
         g.fill(95, cy + 3, 100, cy + 7, a.works() && a.live()[row * 8 + 8 - 1] ? -15254 : -3634628);
      }

      for(int i = 0; i < 40; ++i) {
         int sx = 13 + i % 8 * 10;
         int sy = 10 + i / 8 * 10;
         g.fill(sx + 1, sy + 1, sx + 10 - 1, sy + 10 - 1, parts[i] >= 0 ? -14016234 : -15184083);
      }

      for(int i = 0; i < 40; ++i) {
         if (parts[i] >= 0 || tr[i]) {
            int col = i % 8;
            int row = i / 8;
            int cx = 13 + col * 10 + 5 - 1;
            int cy = 10 + row * 10 + 5 - 1;
            int cc = copper(a.live()[i], a.dist()[i], a.works(), time);
            if (col == 0) {
               g.fill(11, cy, cx, cy + 2, cc);
            }

            if (col == 7) {
               g.fill(cx, cy, 95, cy + 2, cc);
            }

            if (col < 7 && (parts[i + 1] >= 0 || tr[i + 1])) {
               g.fill(cx, cy, cx + 10, cy + 2, cc);
            }

            if (row < 4 && (parts[i + 8] >= 0 || tr[i + 8])) {
               g.fill(cx, cy, cx + 2, cy + 10, cc);
            }

            if (tr[i]) {
               g.fill(cx - 1, cy - 1, cx + 3, cy + 3, a.live()[i] ? -15254 : -3634628);
            }
         }
      }

      for(int i = 0; i < 40; ++i) {
         if (parts[i] >= 0) {
            int sx = 13 + i % 8 * 10;
            int sy = 10 + i / 8 * 10;
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate((float)sx, (float)sy, 0.0F);
            ps.scale(0.625F, 0.625F, 1.0F);
            g.renderItem(new ItemStack(BoardDesign.Part.values()[parts[i]].item()), 0, 0);
            ps.popPose();
         }
      }

      int sx = 109;
      int sy = 2;
      String[] steps = new String[]{"Slot a blank board", "Lay out the parts", "Copper from IN to OUT", "Pick: " + r.design().item().getDescription().getString()};

      for(int k = 0; k < steps.length; ++k) {
         boolean on = (int)(time / 20.0F) % steps.length == k;
         g.fill(sx, sy, sx + 7, sy + 7, on ? -1522584 : -6643800);
         g.fill(sx, sy, sx + 7, sy + 1, on ? -3920 : -4143924);
         g.fill(sx, sy + 6, sx + 7, sy + 7, on ? -6657520 : -9801864);
         small(g, font, String.valueOf(k + 1), (float)(sx + 2), (float)(sy + 1), on ? -12965368 : -1);
         small(g, font, steps[k], (float)(sx + 9), (float)(sy + 1), on ? -14721396 : -12959928);
         sy += 9;
      }

      sy += 3;
      int total = 0;

      for(int v : a.counts().values()) {
         total += v;
      }

      small(g, font, Component.translatable("gui.airwaves.workbench.stages").getString() + " (" + total + ")", (float)sx, (float)sy, -12959928);
      sy += 8;
      int shown = 0;

      for(Map.Entry<BoardAnalysis.Stage, Integer> e : a.counts().entrySet()) {
         if (shown++ == 3) {
            break;
         }

         g.fill(sx, sy + 1, sx + 3, sy + 4, ((BoardAnalysis.Stage)e.getKey()).color());
         small(g, font, Component.translatable(((BoardAnalysis.Stage)e.getKey()).key()).getString() + ((Integer)e.getValue() > 1 ? " x" + String.valueOf(e.getValue()) : ""), (float)(sx + 5), (float)sy, -12959928);
         sy += 7;
      }

      g.fill(166, 82, 182, 85, -7631989);
      int fill = (int)(time % 40.0F) * 16 / 40;
      g.fill(166, 82, 166 + fill, 85, -13727170);

      for(int k = 0; k < 4; ++k) {
         g.fill(182 + k, 80 + k, 183 + k, 87 - k, -7631989);
      }

      bevel(g, 0, 96, 206, 120, -3750202, -11184811, -1);
      ProgramFlow.draw(g, font, BoardAnalysis.program(a), 3, 99, 203, 119, (int)(time / 6.0F));
      small(g, font, "or any circuit with these stages", 2.0F, 122.0F, -8749176);
   }

   public List<Component> getTooltipStrings(Recipe r, IRecipeSlotsView slots, double mx, double my) {
      int col = (int)Math.floor((mx - (double)13.0F) / (double)10.0F);
      int row = (int)Math.floor((my - (double)10.0F) / (double)10.0F);
      if (col >= 0 && row >= 0 && col < 8 && row < 5) {
         int i = row * 8 + col;
         int p = r.layout().parts()[i];
         if (p < 0) {
            return r.layout().traces()[i] ? List.of(Component.translatable("gui.airwaves.part.copper")) : List.of();
         } else {
            BoardDesign.Part part = BoardDesign.Part.values()[p];
            String name = part.name().toLowerCase(Locale.ROOT);
            return List.of(part.item().getDescription(), Component.translatable("gui.airwaves.part." + name).withStyle(ChatFormatting.GRAY));
         }
      } else {
         return List.of();
      }
   }

   private static int copper(boolean live, int dist, boolean works, float time) {
      if (!live) {
         return -5214162;
      } else if (!works) {
         return -3110336;
      } else {
         int k = (int)(Math.pow((double)0.5F + (double)0.5F * Math.sin((double)time * (double)0.5F - (double)dist * 2.2), (double)6.0F) * (double)110.0F);
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

   private static void small(GuiGraphics g, Font f, String s, float x, float y, int col) {
      PoseStack ps = g.pose();
      ps.pushPose();
      ps.translate(x, y, 0.0F);
      ps.scale(0.75F, 0.75F, 1.0F);
      g.drawString(f, s, 0, 0, col, false);
      ps.popPose();
   }

   public static record Recipe(BoardDesign design, BoardLayout layout) {
   }
}
