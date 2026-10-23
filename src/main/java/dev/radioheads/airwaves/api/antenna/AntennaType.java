package dev.radioheads.airwaves.api.antenna;

import dev.radioheads.airwaves.api.antenna.model.DirectionPattern;
import dev.radioheads.airwaves.api.antenna.model.QualityModel;
import dev.radioheads.airwaves.api.antenna.model.RangeModel;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record AntennaType(
        Predicate<BlockState> matches,
        int maxHeight,
        TagKey<Tier> tiers,
        RangeModel rangeModel,
        QualityModel qualityModel,
        @Nullable DirectionPattern direction
) {
    public AntennaType {
        if (maxHeight < 1) throw new IllegalArgumentException("maxHeight must be >= 1");
    }

    public Holder<Tier> tierFor(int requestedLevel) {
        HolderSet.Named<Tier> set = AirwaveRegistries.TIER_REGISTRY.getTag(tiers)
                .orElseThrow(() -> new IllegalStateException("Tier tag " + tiers.location() + " is not bound"));

        int size = set.size();
        if (size == 0)
            throw new IllegalStateException("Tier tag " + tiers.location() + " is empty");

        int index = Math.clamp(requestedLevel - 1, 0, size - 1);
        int position = 0;
        for (Holder<Tier> tier : set) {
            if (position++ == index) return tier;
        }
        throw new IllegalStateException("Tier tag changed while reading index " + index);
    }

    public boolean isDirectional() {
        return direction != null;
    }
}
