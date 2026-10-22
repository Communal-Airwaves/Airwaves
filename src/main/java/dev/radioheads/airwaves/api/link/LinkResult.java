package dev.radioheads.airwaves.api.link;

public record LinkResult(
        double range,
        float quality,
        double rxGain,
        double txDirectionalGain,
        double rxDirectionalGain
) {
    public static final LinkResult NONE = new LinkResult(0.0, 0.0F, 1.0, 1.0, 1.0);
}
