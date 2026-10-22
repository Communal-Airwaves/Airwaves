package dev.radioheads.airwaves.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class RangedDouble extends AbstractRangedNumber<Double> {
    private RangedDouble(double value, double min, double max) {
        super(value, min, max);
    }

    public static RangedDouble of(double value, double min, double max) {
        return new RangedDouble(value, min, max);
    }

    public static RangedDouble clamped(double value, double min, double max) {
        if (Double.isNaN(value)) throw new IllegalArgumentException("value is NaN");
        return new RangedDouble(Math.clamp(value, min, max), min, max);
    }

    public static Codec<RangedDouble> codec(double min, double max) {
        return Codec.DOUBLE.comapFlatMap(
                v -> Double.isFinite(v) && v >= min && v <= max
                        ? DataResult.success(of(v, min, max))
                        : DataResult.error(() -> v + " is outside [" + min + ", " + max + "]"),
                RangedDouble::value);
    }

    @Override
    public Double clamp(Double v) {
        return Math.clamp(v, min(), max());
    }

    @Override
    protected boolean isFinite(Double v) {
        return Double.isFinite(v);
    }
}
