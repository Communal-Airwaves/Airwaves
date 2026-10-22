package dev.radioheads.airwaves.api.util;

import java.util.Objects;

public abstract class AbstractRangedNumber<T extends Number & Comparable<T>> {
    private final T value;
    private final T min;
    private final T max;

    protected AbstractRangedNumber(T value, T min, T max) {
        this.value = Objects.requireNonNull(value, "value");
        this.min = Objects.requireNonNull(min, "min");
        this.max = Objects.requireNonNull(max, "max");

        if (!isFinite(value) || !isFinite(min) || !isFinite(max))
            throw new IllegalArgumentException("Value and bounds must be finite: " + value + " in [" + min + ", " + max + "]");
        if (min.compareTo(max) > 0)
            throw new IllegalArgumentException("min " + min + " is greater than max " + max);
        if (!contains(value))
            throw new IllegalArgumentException(value + " is outside [" + min + ", " + max + "]");
    }

    protected abstract boolean isFinite(T v);

    public T value() {
        return value;
    }

    public T min() {
        return min;
    }

    public T max() {
        return max;
    }

    public boolean contains(T v) {
        return v.compareTo(min) >= 0 && v.compareTo(max) <= 0;
    }

    public abstract T clamp(T v);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRangedNumber<?> other = (AbstractRangedNumber<?>) o;
        return value.equals(other.value) && min.equals(other.min) && max.equals(other.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, min, max);
    }

    @Override
    public String toString() {
        return value + " [" + min + ", " + max + "]";
    }
}
