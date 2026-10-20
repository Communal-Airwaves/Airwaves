package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Modulation(double rangeFactor) {
    public static final Codec<Modulation> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("range_factor").forGetter(Modulation::rangeFactor)
    ).apply(inst, Modulation::new));

    public Modulation {
        if (!Double.isFinite(rangeFactor) || rangeFactor <= 0.0)
            throw new IllegalArgumentException("rangeFactor must be finite and > 0: " + rangeFactor);
    }
}
