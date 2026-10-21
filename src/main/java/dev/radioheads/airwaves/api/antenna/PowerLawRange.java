package dev.radioheads.airwaves.api.antenna;

import dev.radioheads.airwaves.api.antenna.model.RangeModel;
import dev.radioheads.airwaves.api.radio.Modulation;

public record PowerLawRange(
        double baseRange,
        int referenceHeight,
        int pivotHeight,
        double lowExponent,
        double highExponent
) implements RangeModel {
    @Override
    public double range(AntennaState state, Modulation modulation) {
        int h = state.height();
        if (h <= 0 || state.tier() == null) return 0.0;

        double r;
        if (h <= pivotHeight) {
            r = baseRange * Math.pow((double) h / referenceHeight, lowExponent);
        } else {
            double atPivot = baseRange * Math.pow((double) pivotHeight / referenceHeight, lowExponent);
            r = atPivot * Math.pow((double) h / pivotHeight, highExponent);
        }
        return r * state.tier().rangeMultiplier() * modulation.rangeFactor();
    }
}
