package dev.radioheads.airwaves.api.radio;

import dev.radioheads.airwaves.api.antenna.AntennaDirection;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;

//TODO: What needs to change?
public record Emitter(
        UUID root,
        BlockPos pos,
        double topY,
        Tuning tuning,
        double range,
        float baseQ,
        String name,
        Optional<AntennaDirection> direction
) {
    public static final double CREATIVE = 1.0E8D;

    public Emitter(UUID root, BlockPos pos, double topY, Tuning tuning, double range, float baseQ, String name, AntennaDirection direction) {
        this(root, pos, topY, tuning, range, baseQ, name, Optional.of(direction));
    }

    public Emitter(UUID root, BlockPos pos, double topY, Tuning tuning, double range, float baseQ, String name) {
        this(root, pos, topY, tuning, range, baseQ, name, Optional.empty());
    }

    //TODO: Create AntennaUtil
//    public double rangeTo(BlockPos at) {
//        return this.beam == null ? this.range : this.range * AntennaUtil.pattern(this.beam, (double)(at.getX() - this.pos.getX()), (double)(at.getZ() - this.pos.getZ()));
//    }

    public boolean creative() {
        return this.range >= 1.0E8D;
    }
}
