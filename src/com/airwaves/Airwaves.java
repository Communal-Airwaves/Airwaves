package com.airwaves;

import com.airwaves.be.TransmitterBE;
import com.airwaves.cable.ForeignWires;
import com.airwaves.client.ClientInit;
import com.airwaves.compat.svc.SvcState;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.logic.SoundCapture;
import com.airwaves.logic.TapeAudio;
import com.airwaves.net.Net;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.api.stress.BlockStressValues;
import java.util.function.DoubleSupplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

@Mod("airwaves")
public class Airwaves {
   public static final String MODID = "airwaves";
   public static final String BUILD = "9.9";

   public Airwaves(IEventBus modBus, ModContainer container) {
      ModRegistry.register(modBus);
      ModSounds.register(modBus);
      modBus.addListener(Net::register);
      modBus.addListener(Airwaves::commonSetup);
      container.registerConfig(Type.COMMON, Config.SPEC);
      NeoForge.EVENT_BUS.register(this);
      NeoForge.EVENT_BUS.register(ForeignWires.class);
      NeoForge.EVENT_BUS.register(SoundCapture.class);
      if (FMLEnvironment.dist == Dist.CLIENT) {
         ClientInit.init(modBus);
      }

   }

   @SubscribeEvent
   public void levelTick(LevelTickEvent.Post e) {
      Level var3 = e.getLevel();
      if (var3 instanceof ServerLevel sl) {
         if (sl.getGameTime() % 20L == 0L) {
            RadioNetwork.tick(sl);
         }

      }
   }

   private static void commonSetup(FMLCommonSetupEvent e) {
      e.enqueueWork(() -> {
         SimpleRegistry<Block, DoubleSupplier> impacts = BlockStressValues.IMPACTS;

         for(int i = 0; i < ModRegistry.TRANSMITTERS.size(); ++i) {
            double v = TransmitterBE.STRESS[i];
            impacts.register((Block)((DeferredBlock)ModRegistry.TRANSMITTERS.get(i)).get(), (DoubleSupplier)() -> v);
         }

         impacts.register((Block)ModRegistry.JAMMER.get(), (DoubleSupplier)() -> (double)8.0F);
         impacts.register((Block)ModRegistry.DIMENSIONAL_RELAY.get(), (DoubleSupplier)() -> (double)16.0F);
         impacts.register((Block)ModRegistry.RELAY_TOWER_SMALL.get(), (DoubleSupplier)() -> (double)12.0F);
         impacts.register((Block)ModRegistry.RELAY_TOWER_BIG.get(), (DoubleSupplier)() -> (double)24.0F);
         impacts.register((Block)ModRegistry.RADIO_TOWER.get(), (DoubleSupplier)() -> (double)64.0F);
      });
   }

   @SubscribeEvent
   public void login(PlayerEvent.PlayerLoggedInEvent e) {
      CompoundTag tag = e.getEntity().getPersistentData();
      if (!tag.getBoolean("airwaves_manual")) {
         tag.putBoolean("airwaves_manual", true);
         e.getEntity().getInventory().add(new ItemStack((ItemLike)ModRegistry.FIELD_MANUAL.get()));
      }
   }

   @SubscribeEvent
   public void lightning(EntityJoinLevelEvent e) {
      Entity var4 = e.getEntity();
      if (var4 instanceof LightningBolt b) {
         Level var5 = e.getLevel();
         if (var5 instanceof ServerLevel sl) {
            RadioNetwork.lightning(sl, b.position());
         }
      }

   }

   @SubscribeEvent
   public void serverStopped(ServerStoppedEvent e) {
      RadioNetwork.clear();
      SoundCapture.clear();
      TapeAudio.clear();
      SvcState.USING_SVC.clear();
   }
}
