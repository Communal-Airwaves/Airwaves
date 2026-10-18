package com.airwaves.client;

import com.airwaves.be.YagiAntennaBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelData;

public class YagiRenderer implements BlockEntityRenderer<YagiAntennaBE> {
   public static final ModelResourceLocation ARMS = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("airwaves", "block/yagi_antenna_arms"));
   public static final float PX = 7.8738F;
   public static final float PZ = 8.475F;

   public YagiRenderer(BlockEntityRendererProvider.Context c) {
   }

   public void render(YagiAntennaBE be, float pt, PoseStack ms, MultiBufferSource buf, int light, int overlay) {
      float target = (float)be.bearing();
      if (Float.isNaN(be.shown)) {
         be.shown = target;
      }

      float diff = ((target - be.shown) % 360.0F + 540.0F) % 360.0F - 180.0F;
      be.shown = Math.abs(diff) < 0.05F ? target : be.shown + diff * 0.08F;
      BakedModel model = Minecraft.getInstance().getModelManager().getModel(ARMS);
      ms.pushPose();
      ms.translate(0.4921125F, 0.0F, 0.5296875F);
      ms.mulPose(Axis.YP.rotationDegrees(-be.shown));
      ms.translate(-0.4921125F, 0.0F, -0.5296875F);
      Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buf.getBuffer(RenderType.cutout()), be.getBlockState(), model, 1.0F, 1.0F, 1.0F, light, overlay, ModelData.EMPTY, RenderType.cutout());
      ms.popPose();
   }

   public boolean shouldRenderOffScreen(YagiAntennaBE be) {
      return true;
   }
}
