package dev.radioheads.airwaves.api.antenna;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record AntennaState(
        AntennaType type,
        Level level,
        BlockPos pos,
        int height,
        @Nullable Tier tier,
        @Nullable AntennaDirection direction
) {
    public static AntennaState empty(AntennaType type, Level level, BlockPos pos) {
        return new AntennaState(type, level, pos, 0, null, null);
    }

    public boolean isActive() {
        return height > 0 && tier != null;
    }
}
