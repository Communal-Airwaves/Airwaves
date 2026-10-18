package com.airwaves.client;

import com.airwaves.ModRegistry;
import com.airwaves.cable.NodeRef;
import com.airwaves.client.ponder.AirwavesPonderPlugin;
import com.airwaves.item.CableItem;
import com.airwaves.kinetic.KineticDeviceRenderer;
import com.airwaves.logic.Cables;
import com.airwaves.net.VoiceModePayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import java.util.List;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ClientInit {
   public static final KeyMapping VOICE_KEY = new KeyMapping("key.airwaves.voice_menu", 86, "key.categories.airwaves");
   public static final KeyMapping PTT_KEY = new KeyMapping("key.airwaves.push_to_talk", 280, "key.categories.airwaves");
   private static int ticks;

   private ClientInit() {
   }

   public static void init(IEventBus modBus) {
      modBus.addListener(ClientInit::keys);
      modBus.addListener(ClientInit::screens);
      modBus.addListener(ClientInit::renderers);
      modBus.addListener(ClientInit::layers);
      modBus.addListener(ClientInit::models);
      modBus.addListener(ClientInit::setup);
      modBus.addListener(ClientInit::itemExtensions);
      AirwavesSpriteShifts.init(modBus);
      NeoForge.EVENT_BUS.addListener(ClientInit::tick);
      NeoForge.EVENT_BUS.addListener(ClientInit::login);
      NeoForge.EVENT_BUS.addListener(ClientInit::logout);
      NeoForge.EVENT_BUS.addListener(ClientInit::renderWorld);
      NeoForge.EVENT_BUS.addListener(ShiftTooltips::onTooltip);
   }

   private static void keys(RegisterKeyMappingsEvent e) {
      e.register(VOICE_KEY);
      e.register(PTT_KEY);
   }

   private static void screens(RegisterMenuScreensEvent e) {
      e.register((MenuType)ModRegistry.TUNING_MENU.get(), TuningTableScreen::new);
      e.register((MenuType)ModRegistry.WORKBENCH_MENU.get(), CircuitWorkbenchScreen::new);
      e.register((MenuType)ModRegistry.TAPE_MENU.get(), TapePlayerScreen::new);
   }

   private static void renderers(EntityRenderersEvent.RegisterRenderers e) {
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.MIC_BE.get(), (ctx) -> new CableRenderer());
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.SPEAKER_BE.get(), (ctx) -> new CableRenderer());
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.TRANSMITTER_BE.get(), KineticDeviceRenderer::new);
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.RECEIVER_BE.get(), (ctx) -> new CableRenderer());
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.CREATIVE_TX_BE.get(), (ctx) -> new CableRenderer());
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.CREATIVE_RX_BE.get(), (ctx) -> new CableRenderer());
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.INSULATOR_BE.get(), (ctx) -> new InsulatorRenderer());
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.JAMMER_BE.get(), KineticDeviceRenderer::new);
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.DIM_RELAY_BE.get(), KineticDeviceRenderer::new);
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.RELAY_BE.get(), KineticDeviceRenderer::new);
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.TOWER_BE.get(), KineticDeviceRenderer::new);
      e.registerBlockEntityRenderer((BlockEntityType)ModRegistry.TOWER_SHAFT_BE.get(), KineticDeviceRenderer::new);
   }

   private static void setup(FMLClientSetupEvent e) {
      e.enqueueWork(() -> {
         SimpleBlockEntityVisualizer.builder((BlockEntityType)ModRegistry.TRANSMITTER_BE.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender((be) -> false).apply();
         SimpleBlockEntityVisualizer.builder((BlockEntityType)ModRegistry.JAMMER_BE.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender((be) -> false).apply();
         SimpleBlockEntityVisualizer.builder((BlockEntityType)ModRegistry.DIM_RELAY_BE.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender((be) -> false).apply();
         SimpleBlockEntityVisualizer.builder((BlockEntityType)ModRegistry.TOWER_BE.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender((be) -> false).apply();
         SimpleBlockEntityVisualizer.builder((BlockEntityType)ModRegistry.RELAY_BE.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender((be) -> false).apply();
         SimpleBlockEntityVisualizer.builder((BlockEntityType)ModRegistry.TOWER_SHAFT_BE.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender((be) -> false).apply();
         PonderIndex.addPlugin(new AirwavesPonderPlugin());
      });
   }

   private static void models(ModelEvent.RegisterAdditional e) {
      e.register(InsulatorRenderer.SPOOL);
      e.register(InsulatorRenderer.SPOOL_WOUND);
   }

   private static void layers(RegisterGuiLayersEvent e) {
      e.registerAboveAll(ResourceLocation.fromNamespaceAndPath("airwaves", "radio_hud"), (g, dt) -> hud(g));
   }

   private static void pill(GuiGraphics g, int x, int y, int w, int h, int accent) {
      g.fill(x, y, x + w, y + h, -1072688618);
      g.fill(x, y, x + 2, y + h, accent);
      g.fill(x, y, x + w, y + 1, 1090519039);
   }

   private static void hud(GuiGraphics g) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !mc.options.hideGui && mc.screen == null) {
         Font font = mc.font;
         boolean ptt = VoiceConfig.pushToTalk;
         int c = VoiceClient.error != null ? -2056144 : (VoiceConfig.muted ? -2078648 : (VoiceClient.speaking ? -11214736 : -7695456));
         String t = VoiceClient.error != null ? "No mic" : (VoiceConfig.muted ? "Muted" : (VoiceClient.speaking ? "Live" : (ptt ? "PTT: " + PTT_KEY.getTranslatedKeyMessage().getString() : "Voice")));
         int w = font.width(t) + 30;
         pill(g, 4, 4, w, 14, c);
         g.fill(10, 6, 14, 12, c);
         g.fill(9, 11, 15, 12, c);
         g.fill(11, 12, 13, 15, c);
         g.fill(9, 15, 15, 16, c);
         if (VoiceConfig.muted) {
            for(int i = 0; i < 9; ++i) {
               g.fill(8 + i, 6 + i, 9 + i, 7 + i, -1);
            }
         }

         g.drawString(font, t, 20, 7, -1512208, false);
         if (VoiceClient.speaking) {
            float lv = Math.min(1.0F, VoiceClient.level * 6.0F);
            g.fill(20, 16, 20 + (int)((float)(w - 24) * lv), 17, c);
         }

         int y = 22;
         if (ClientData.hhOn) {
            String s = ClientData.hhLabel;
            int bars = ClientData.hhCarrier ? 1 + Math.round(ClientData.hhQuality * 4.0F) : 0;
            int ww = font.width(s) + 40;
            pill(g, 4, y, ww, 14, ClientData.hhCarrier ? -11220768 : -9800832);
            g.drawString(font, s, 10, y + 3, -1512208, false);

            for(int i = 0; i < 5; ++i) {
               g.fill(ww - 26 + i * 4, y + 10 - i * 2, ww - 23 + i * 4, y + 11, i < bars ? -11214736 : -12959670);
            }

            y += 18;
         }

         ItemStack st = mc.player.getMainHandItem();
         NodeRef start = st.getItem() instanceof CableItem ? CableItem.start(st) : null;
         if (start != null) {
            int max = ((CableItem)st.getItem()).type.maxLen;
            double len = CableRenderer.anchor(mc.level, start.pos(), start.slot()).distanceTo(cursor(mc));
            boolean far = len > (double)max;
            int cx = mc.getWindow().getGuiScaledWidth() / 2;
            int cy = mc.getWindow().getGuiScaledHeight() / 2 + 12;
            pill(g, cx - 34, cy, 68, 8, far ? -2078648 : -2588598);
            int fillW = (int)((double)60.0F * Math.min((double)1.0F, len / (double)max));
            g.fill(cx - 30, cy + 3, cx - 30 + fillW, cy + 5, far ? -40864 : -1533344);
            g.fill(cx - 30 + fillW, cy + 3, cx + 30, cy + 5, -14013388);
         }

      }
   }

   private static Vec3 cursor(Minecraft mc) {
      HitResult h = mc.hitResult;
      if (h != null && h.getType() == Type.BLOCK) {
         NodeRef r = CableItem.refAt(mc.level, ((BlockHitResult)h).getBlockPos(), h.getLocation());
         return r != null ? CableRenderer.anchor(mc.level, r.pos(), r.slot()) : h.getLocation();
      } else {
         return mc.player.getEyePosition().add(mc.player.getLookAngle().scale((double)2.5F));
      }
   }

   private static void renderWorld(RenderLevelStageEvent e) {
      if (e.getStage() == Stage.AFTER_PARTICLES) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            ItemStack st = mc.player.getMainHandItem();
            Item var4 = st.getItem();
            if (var4 instanceof CableItem) {
               CableItem ci = (CableItem)var4;
               NodeRef start = CableItem.start(st);
               if (start != null && mc.level.isLoaded(start.pos())) {
                  Vec3 a = CableRenderer.anchor(mc.level, start.pos(), start.slot());
                  int col = ci.type.color;
                  HitResult h = mc.hitResult;
                  NodeRef tgt = h != null && h.getType() == Type.BLOCK ? CableItem.refAt(mc.level, ((BlockHitResult)h).getBlockPos(), h.getLocation()) : null;
                  Vec3 b;
                  if (tgt != null) {
                     b = CableRenderer.anchor(mc.level, tgt.pos(), tgt.slot());
                     col = tgt.equals(start) ? ci.type.color : (Cables.canLink(mc.level, start, tgt, ci.type) ? -8323184 : -40864);
                  } else {
                     b = h != null && h.getType() == Type.BLOCK ? h.getLocation() : mc.player.getEyePosition().add(mc.player.getLookAngle().scale((double)2.5F));
                     if (a.distanceTo(b) > (double)ci.type.maxLen) {
                        col = -40864;
                     }
                  }

                  Vec3 cam = e.getCamera().getPosition();
                  PoseStack ps = e.getPoseStack();
                  ps.pushPose();
                  ps.translate(a.x - cam.x, a.y - cam.y, a.z - cam.z);
                  MultiBufferSource.BufferSource bs = mc.renderBuffers().bufferSource();
                  int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(a));
                  CableRenderer.line(ps, bs, Vec3.ZERO, b.subtract(a), light, light, col, ci.type.width);
                  bs.endBatch(RenderType.entitySolid(CableRenderer.WHITE));
                  ps.popPose();
               }
            }
         }
      }
   }

   private static void tick(ClientTickEvent.Post e) {
      Minecraft mc = Minecraft.getInstance();

      while(VOICE_KEY.consumeClick()) {
         if (mc.screen == null && mc.player != null) {
            mc.setScreen(new VoiceMenuScreen());
         }
      }

      VoiceClient.flush();
      if (++ticks % 2 == 0) {
         AudioPlayback.tick(mc);
      }

      if (ticks % 10 == 0) {
         RadioSounds.tick(mc);
      }

   }

   private static void login(ClientPlayerNetworkEvent.LoggingIn e) {
      VoiceConfig.load();
      VoiceClient.start();
      sendVoiceMode();
   }

   public static void sendVoiceMode() {
      try {
         PacketDistributor.sendToServer(new VoiceModePayload(VoiceConfig.svcActive()), new CustomPacketPayload[0]);
      } catch (Throwable var1) {
      }

   }

   private static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
      VoiceClient.stop();
      AudioPlayback.stopAll();
      RadioSounds.stopAll();
      ClientData.hhOn = false;
      ClientData.spectrum = List.of();
   }

   private static void itemExtensions(RegisterClientExtensionsEvent e) {
      IClientItemExtensions ext = new IClientItemExtensions() {
         private BoardItemRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new BoardItemRenderer();
            }

            return this.renderer;
         }
      };
      e.registerItem(ext, (Item[])ModRegistry.BOARDS.values().stream().map(DeferredHolder::get).toArray((x$0) -> new Item[x$0]));
   }
}
