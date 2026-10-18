package com.airwaves.net;

import com.airwaves.ModRegistry;
import com.airwaves.Tuning;
import com.airwaves.client.ClientHandlers;
import com.airwaves.compat.svc.SvcState;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.menu.TuningTableMenu;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Net {
   private Net() {
   }

   public static void register(RegisterPayloadHandlersEvent e) {
      PayloadRegistrar r = e.registrar("3");
      r.playToServer(TunePayload.TYPE, TunePayload.CODEC, Net::onTune);
      r.playToServer(VoiceFramePayload.TYPE, VoiceFramePayload.CODEC, Net::onVoice);
      r.playToClient(VoiceRelayPayload.TYPE, VoiceRelayPayload.CODEC, ClientHandlers::onVoiceRelay);
      r.playToClient(RadioAudioPayload.TYPE, RadioAudioPayload.CODEC, ClientHandlers::onRadioAudio);
      r.playToServer(SpectrumRequestPayload.TYPE, SpectrumRequestPayload.CODEC, Net::onSpectrumRequest);
      r.playToClient(SpectrumPayload.TYPE, SpectrumPayload.CODEC, ClientHandlers::onSpectrum);
      r.playToClient(HandheldAudioPayload.TYPE, HandheldAudioPayload.CODEC, ClientHandlers::onHandheldAudio);
      r.playToClient(HandheldStatePayload.TYPE, HandheldStatePayload.CODEC, ClientHandlers::onHandheldState);
      r.playToClient(CablePulsePayload.TYPE, CablePulsePayload.CODEC, ClientHandlers::onPulse);
      r.playToClient(StaticBurstPayload.TYPE, StaticBurstPayload.CODEC, ClientHandlers::onBurst);
      r.playToClient(RadioSoundPayload.TYPE, RadioSoundPayload.CODEC, ClientHandlers::onRadioSound);
      r.playToServer(VoiceModePayload.TYPE, VoiceModePayload.CODEC, Net::onVoiceMode);
   }

   private static void onTune(TunePayload p, IPayloadContext ctx) {
      Player var4 = ctx.player();
      if (var4 instanceof ServerPlayer sp) {
         AbstractContainerMenu var5 = sp.containerMenu;
         if (var5 instanceof TuningTableMenu m) {
            ItemStack s = m.getSlot(0).getItem();
            if (s.isEmpty()) {
               return;
            }

            s.set((DataComponentType)ModRegistry.TUNING.get(), (new Tuning(p.fm(), p.freq())).clamped());
            m.broadcastChanges();
            return;
         }
      }

   }

   private static void onSpectrumRequest(SpectrumRequestPayload p, IPayloadContext ctx) {
      Player var3 = ctx.player();
      if (var3 instanceof ServerPlayer sp) {
         PacketDistributor.sendToPlayer(sp, new SpectrumPayload(RadioNetwork.spectrum(sp)), new CustomPacketPayload[0]);
      }

   }

   private static void onVoiceMode(VoiceModePayload p, IPayloadContext ctx) {
      Player var3 = ctx.player();
      if (var3 instanceof ServerPlayer sp) {
         SvcState.set(sp.getUUID(), p.simpleVoiceChat());
      }

   }

   private static void onVoice(VoiceFramePayload p, IPayloadContext ctx) {
      Player var3 = ctx.player();
      if (var3 instanceof ServerPlayer sp) {
         if (!sp.isSpectator()) {
            if (SvcState.usesSvc(sp.getUUID())) {
               return;
            }

            byte[] d = p.data();
            if (d.length >= 16 && d.length <= 960) {
               PacketDistributor.sendToPlayersNear(sp.serverLevel(), sp, sp.getX(), sp.getY(), sp.getZ(), (double)48.0F, new VoiceRelayPayload(sp.getUUID(), d), new CustomPacketPayload[0]);
               RadioNetwork.onVoice(sp, d);
               return;
            }

            return;
         }
      }

   }
}
