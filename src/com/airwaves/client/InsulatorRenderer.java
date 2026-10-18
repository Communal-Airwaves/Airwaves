package com.airwaves.client;

import com.airwaves.be.InsulatorBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;

public class InsulatorRenderer extends CableRenderer<InsulatorBE> {
   public static final ModelResourceLocation SPOOL = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("airwaves", "block/insulator_spool"));
   public static final ModelResourceLocation SPOOL_WOUND = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("airwaves", "block/insulator_spool_wound"));
   private static final float PIVOT = 0.48125F;

   public void render(InsulatorBE be, float pt, PoseStack ps, MultiBufferSource buf, int light, int overlay) {
      super.render(be, pt, ps, buf, light, overlay);
      Minecraft mc = Minecraft.getInstance();
      BakedModel model = mc.getModelManager().getModel(be.links().isEmpty() ? SPOOL : SPOOL_WOUND);
      float t = (float)(System.currentTimeMillis() - be.changedAt) / 1400.0F;
      float angle = !(t >= 1.0F) && be.changedAt != 0L ? (1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t)) * 720.0F : 0.0F;
      Direction f = be.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? (Direction)be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
      ps.pushPose();
      ps.translate((double)0.5F, (double)0.0F, (double)0.5F);
      ps.mulPose(Axis.YP.rotationDegrees(-f.toYRot() + 180.0F));
      ps.translate((double)-0.5F, (double)0.0F, (double)-0.5F);
      ps.translate((double)0.5F, (double)0.48125F, (double)0.5F);
      ps.mulPose(Axis.XP.rotationDegrees(angle));
      ps.translate((double)-0.5F, (double)-0.48125F, (double)-0.5F);
      mc.getBlockRenderer().getModelRenderer().renderModel(ps.last(), buf.getBuffer(RenderType.cutout()), (BlockState)null, model, 1.0F, 1.0F, 1.0F, light, overlay, ModelData.EMPTY, RenderType.cutout());
      ps.popPose();
   }
}
