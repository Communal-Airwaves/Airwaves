package dev.radioheads.airwaves.api.antenna;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record Tier(
        int heightCap,
        double rangeMultiplier,
        float qualityBonus,
        double rxGainBonus
) {
    public static final Codec<Tier> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("height_cap").forGetter(Tier::heightCap),
            Codec.DOUBLE.fieldOf("range_multiplier").forGetter(Tier::rangeMultiplier),
            Codec.FLOAT.fieldOf("quality_bonus").forGetter(Tier::qualityBonus),
            Codec.DOUBLE.fieldOf("rx_gain_bonus").forGetter(Tier::rxGainBonus)
    ).apply(inst, Tier::new));

    public Tier {
        if (heightCap < 1) throw new IllegalArgumentException("heightCap must be >= 1: " + heightCap);
        if (!(rangeMultiplier > 0.0)) throw new IllegalArgumentException("rangeMultiplier must be > 0");
    }

    public Component displayName() {
        ResourceLocation id = AirwaveRegistries.TIER_REGISTRY.getKey(this);
        return Component.translatable("airwaves.tier." + id.getPath());
    }
}
