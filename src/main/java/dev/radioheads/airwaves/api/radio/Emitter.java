package dev.radioheads.airwaves.api.radio;

import net.minecraft.core.BlockPos;

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
        Float beam
) {
    public static final double CREATIVE = 1.0E8D;

    public Emitter(UUID root, BlockPos pos, double topY, Tuning tuning, double range, float baseQ, String name) {
        this(root, pos, topY, tuning, range, baseQ, name, null);
    }

    //TODO: Create AntennaUtil
//    public double rangeTo(BlockPos at) {
//        return this.beam == null ? this.range : this.range * AntennaUtil.pattern(this.beam, (double)(at.getX() - this.pos.getX()), (double)(at.getZ() - this.pos.getZ()));
//    }

    public boolean creative() {
        return this.range >= 1.0E8D;
    }
}
