package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import dev.radioheads.airwaves.api.registry.builders.ModulationBuilder;
import dev.radioheads.airwaves.api.registry.modulation.ModulationEntry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AirwaveModulations {
    private AirwaveModulations() {}

    public static final ModulationEntry
            FM = Airwaves.REGISTRATE.modulation("fm", () -> new Modulation(1.0)).register(),
            AM = Airwaves.REGISTRATE.modulation("am", () -> new Modulation(1.3)).register()
    ;

    public static void init() {}
}
