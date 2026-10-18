package com.airwaves.client;

import com.airwaves.net.CablePulsePayload;
import com.airwaves.net.HandheldAudioPayload;
import com.airwaves.net.HandheldStatePayload;
import com.airwaves.net.RadioAudioPayload;
import com.airwaves.net.RadioSoundPayload;
import com.airwaves.net.SpectrumPayload;
import com.airwaves.net.StaticBurstPayload;
import com.airwaves.net.VoiceRelayPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientHandlers {
   private ClientHandlers() {
   }

   public static void onVoiceRelay(VoiceRelayPayload p, IPayloadContext ctx) {
      AudioPlayback.playProximity(p.talker(), p.data());
   }

   public static void onRadioAudio(RadioAudioPayload p, IPayloadContext ctx) {
      AudioPlayback.playRadio(p);
   }

   public static void onHandheldAudio(HandheldAudioPayload p, IPayloadContext ctx) {
      AudioPlayback.playHandheld(p);
   }

   public static void onBurst(StaticBurstPayload p, IPayloadContext ctx) {
      AudioPlayback.burst(p.strength());
   }

   public static void onRadioSound(RadioSoundPayload p, IPayloadContext ctx) {
      RadioSounds.play(p);
   }

   public static void onPulse(CablePulsePayload p, IPayloadContext ctx) {
      CablePulses.add(p);
   }

   public static void onSpectrum(SpectrumPayload p, IPayloadContext ctx) {
      ClientData.spectrum = p.stations();
      ClientData.spectrumTime = System.currentTimeMillis();
   }

   public static void onHandheldState(HandheldStatePayload p, IPayloadContext ctx) {
      ClientData.hhOn = p.on();
      ClientData.hhCarrier = p.carrier();
      ClientData.hhQuality = p.quality();
      ClientData.hhFm = p.fm();
      ClientData.hhLabel = p.label();
   }
}
