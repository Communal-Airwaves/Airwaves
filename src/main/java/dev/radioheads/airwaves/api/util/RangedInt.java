package dev.radioheads.airwaves.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class RangedInt extends AbstractRangedNumber<Integer> {
    private RangedInt(int value, int min, int max) {
        super(value, min, max);
    }

    public static RangedInt of(int value, int min, int max) {
        return new RangedInt(value, min, max);
    }

    public static RangedInt clamped(int value, int min, int max) {
        return new RangedInt(Math.clamp((long) value, min, max), min, max);
    }

    public static Codec<RangedInt> codec(int min, int max) {
        return Codec.INT.comapFlatMap(
                v -> v >= min && v <= max
                        ? DataResult.success(of(v, min, max))
                        : DataResult.error(() -> v + " is outside [" + min + ", " + max + "]"),
                RangedInt::value);
    }

    @Override
    public Integer clamp(Integer v) {
        return Math.clamp(v.longValue(), min(), max());
    }

    @Override
    protected boolean isFinite(Integer v) {
        return true;
    }
}
