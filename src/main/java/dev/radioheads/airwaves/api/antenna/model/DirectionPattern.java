package dev.radioheads.airwaves.api.antenna.model;

import dev.radioheads.airwaves.api.antenna.AntennaDirection;

public interface DirectionPattern {
    double gain(AntennaDirection beam, double dx, double dz);
}
