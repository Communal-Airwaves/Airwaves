package dev.radioheads.airwaves.api.antenna.pattern;

import dev.radioheads.airwaves.api.antenna.AntennaDirection;
import dev.radioheads.airwaves.api.antenna.model.DirectionPattern;

public record CosinePattern(double peakGain, double halfAngleDeg) implements DirectionPattern {
    @Override
    public double gain(AntennaDirection beam, double dx, double dz) {
        double d = Math.hypot(dx, dz);
        if (d < 1.0E-6) return 1.0;

        double cos = (dx * beam.dx() + dz * beam.dz()) / d;
        double edge = Math.cos(Math.toRadians(halfAngleDeg));
        if (cos <= edge) return 0.0;

        double k = (cos - edge) / (1.0 - edge);
        return peakGain * k * Math.sqrt(k);
    }
}
