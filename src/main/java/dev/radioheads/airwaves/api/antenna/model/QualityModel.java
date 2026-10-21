package dev.radioheads.airwaves.api.antenna.model;

import dev.radioheads.airwaves.api.antenna.AntennaState;

public interface QualityModel {
    float quality(AntennaState state);

    QualityModel DEFAULT = state -> {
        if (state.height() <= 0 || state.tier() == null) return 0.0F;
        float heightPart = 0.5F * Math.min(1.0F, (float) state.height() / 20.0F);
        return Math.min(1.0F, 0.5F + heightPart + state.tier().qualityBonus());
    };
}
