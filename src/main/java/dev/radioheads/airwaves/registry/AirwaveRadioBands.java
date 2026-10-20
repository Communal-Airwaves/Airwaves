package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.registry.radio.RadioBandEntry;

public class AirwaveRadioBands {
    public static final RadioBandEntry<RadioBand>
            FM = Airwaves.REGISTRATE.radioBand("fm", () -> new RadioBand(87.5, 108.0, 98.1, "MHz")).register(),
            AM = Airwaves.REGISTRATE.radioBand("am", () -> new RadioBand(530.0, 1700.0, 1000.0, "kHz")).register()
    ;

    public static void init() {}
}
