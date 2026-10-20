package dev.radioheads.airwaves.util;

// Part of old code.
public final class AntennaUtil {
    private AntennaUtil() {}

    public record Info(int height, int tier, double fixedRange, Float beam) {
        public static final Info NONE = new Info(0, 0);

        public Info(int height, int tier) {
            this(height, tier, -1.0F, null);
        }

        public Info(int height, int tier, double fixedRange) {
            this(height, tier, fixedRange, null);
        }

        public boolean creative() {
            return this.fixedRange >= 0.0D;
        }
    }
}
