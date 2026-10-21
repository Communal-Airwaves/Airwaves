package dev.radioheads.airwaves.api.antenna;

import dev.radioheads.airwaves.api.antenna.model.DirectionPattern;
import dev.radioheads.airwaves.api.antenna.model.QualityModel;
import dev.radioheads.airwaves.api.antenna.model.RangeModel;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record AntennaType(
        Predicate<BlockState> matches,
        int maxHeight,
        List<Holder<Tier>> tiers,
        RangeModel rangeModel,
        QualityModel qualityModel,
        @Nullable DirectionPattern direction
) {
    public AntennaType {
        if (maxHeight < 1) throw new IllegalArgumentException("maxHeight must be >= 1");
        if (tiers.isEmpty()) throw new IllegalArgumentException("At least one tier required");
        tiers = List.copyOf(tiers);
    }

    public Holder<Tier> tierFor(int requestedLevel) {
        int index = Math.clamp(requestedLevel - 1, 0, tiers.size() - 1);
        return tiers.get(index);
    }

    public boolean isDirectional() {
        return direction != null;
    }
}
