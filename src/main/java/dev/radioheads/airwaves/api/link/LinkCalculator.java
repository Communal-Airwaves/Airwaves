package dev.radioheads.airwaves.api.link;

import dev.radioheads.airwaves.api.antenna.AntennaState;
import dev.radioheads.airwaves.api.radio.Modulation;

public final class LinkCalculator {
    private LinkCalculator() {}

    /**
     * @param dx horizontal offset from transmitter to receiver (x)
     * @param dz horizontal offset from transmitter to receiver (z)
     */
    public static LinkResult evaluate(AntennaState tx, AntennaState rx, double dx, double dz, Modulation modulation) {
        if (!tx.isActive() || !rx.isActive()) return LinkResult.NONE;

        double range = tx.type().rangeModel().range(tx, modulation);
        float quality = tx.type().qualityModel().quality(tx);

        return new LinkResult(
                range,
                quality,
                rxGain(rx),
                directionalGain(tx, dx, dz),
                directionalGain(rx, -dx, -dz)
        );
    }

    /** Receive gain: height term times tier term. */
    public static double rxGain(AntennaState state) {
        if (state.height() <= 0 || state.tier() == null) return 1.0;
        double heightTerm = 1.0 + 0.12 * Math.min(state.height(), 30);
        double tierTerm = 1.0 + state.tier().rxGainBonus();
        return heightTerm * tierTerm;
    }

    private static double directionalGain(AntennaState state, double dx, double dz) {
        if (!state.type().isDirectional() || state.direction() == null) return 1.0;
        return state.type().direction().gain(state.direction(), dx, dz);
    }
}
