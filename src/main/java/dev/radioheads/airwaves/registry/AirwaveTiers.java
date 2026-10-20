package dev.radioheads.airwaves.registry;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.antenna.Tier;
import dev.radioheads.airwaves.api.registry.antenna.tier.TierEntry;

public final class AirwaveTiers {
    private AirwaveTiers() {}

    public static final TierEntry
        LOW_POWER = Airwaves.REGISTRATE.tier("low_power", () -> new Tier(20, 1.0, 0, 0.0)).register(),
        MID_POWER = Airwaves.REGISTRATE.tier("mid_power", () -> new Tier(40, 1.3, 0.08f, 0.2)).register(),
        HIGH_POWER = Airwaves.REGISTRATE.tier("high_power", () -> new Tier(64, 1.7, 0.15f, 0.4)).register(),
        MAX_POWER = Airwaves.REGISTRATE.tier("max_power", () -> new Tier(96, 2.3, 0.22f, 0.6)).register(),
        CREATIVE = Airwaves.REGISTRATE.tier("creative", () -> new Tier(256, 1.0, 0, 0)).register();

    public static void init() {}
}
