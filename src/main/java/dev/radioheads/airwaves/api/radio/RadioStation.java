package dev.radioheads.airwaves.api.radio;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public record RadioStation(
        UUID id,
        BlockPos pos,
        double antennaTop, //TODO: Does this need to change? What does this do?
        double range,
        float quality,
        String name
) {
    public static final double CREATIVE_RANGE = 1.0E8D;
}
