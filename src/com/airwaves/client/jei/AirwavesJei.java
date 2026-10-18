package com.airwaves.client.jei;

import com.airwaves.ModRegistry;
import com.airwaves.logic.BoardDesign;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class AirwavesJei implements IModPlugin {
   public ResourceLocation getPluginUid() {
      return ResourceLocation.fromNamespaceAndPath("airwaves", "jei");
   }

   public void registerCategories(IRecipeCategoryRegistration r) {
      r.addRecipeCategories(new IRecipeCategory[]{new BoardRecipeCategory(r.getJeiHelpers().getGuiHelper())});
   }

   public void registerRecipes(IRecipeRegistration r) {
      List<BoardRecipeCategory.Recipe> list = new ArrayList();

      for(BoardDesign d : BoardDesign.values()) {
         list.add(new BoardRecipeCategory.Recipe(d, d.exampleLayout()));
      }

      r.addRecipes(BoardRecipeCategory.TYPE, list);
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
      r.addRecipeCatalyst(new ItemStack((ItemLike)ModRegistry.CIRCUIT_WORKBENCH_ITEM.get()), new RecipeType[]{BoardRecipeCategory.TYPE});
   }
}
