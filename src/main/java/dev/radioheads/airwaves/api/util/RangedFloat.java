package dev.radioheads.airwaves.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class RangedFloat extends AbstractRangedNumber<Float> {
    private RangedFloat(float value, float min, float max) {
        super(value, min, max);
    }

    public static RangedFloat of(float value, float min, float max) {
        return new RangedFloat(value, min, max);
    }

    public static RangedFloat clamped(float value, float min, float max) {
        if (Float.isNaN(value)) throw new IllegalArgumentException("value is NaN");
        return new RangedFloat(Math.clamp(value, min, max), min, max);
    }

    public static Codec<RangedFloat> codec(float min, float max) {
        return Codec.FLOAT.comapFlatMap(
                v -> Float.isFinite(v) && v >= min && v <= max
                        ? DataResult.success(of(v, min, max))
                        : DataResult.error(() -> v + " is outside [" + min + ", " + max + "]"),
                RangedFloat::value);
    }

    @Override
    public Float clamp(Float v) {
        return Math.clamp(v, min(), max());
    }

    @Override
    protected boolean isFinite(Float v) {
        return Float.isFinite(v);
    }
}
