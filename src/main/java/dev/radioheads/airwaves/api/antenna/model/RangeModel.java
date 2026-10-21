package dev.radioheads.airwaves.api.antenna.model;

import dev.radioheads.airwaves.api.antenna.AntennaState;
import dev.radioheads.airwaves.api.radio.Modulation;

public interface RangeModel {
    double range(AntennaState state, Modulation modulation);
}
