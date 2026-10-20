package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Optional;

public record RadioBand(double min, double max, double defaultFrequency, String unit, Optional<String> pathTypeOverride) {
    //TODO: Is defaultFrequency really needed?
    public static final Codec<RadioBand> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("min").forGetter(RadioBand::min),
            Codec.DOUBLE.fieldOf("max").forGetter(RadioBand::max),
            Codec.DOUBLE.fieldOf("default_frequency").forGetter(RadioBand::defaultFrequency),
            Codec.STRING.fieldOf("unit").forGetter(RadioBand::unit),
            Codec.STRING.optionalFieldOf("path_type").forGetter(RadioBand::pathTypeOverride)
    ).apply(inst, RadioBand::new));

    public RadioBand(double min, double max, double defaultFrequency, String unit) {
        this(min, max, defaultFrequency, unit, Optional.empty());
    }

    public RadioBand withPathType(String pathType) {
        return new RadioBand(min, max, defaultFrequency, unit, Optional.of(pathType));
    }

    public String pathType(ResourceLocation id) {
        return pathTypeOverride.orElseGet(() -> id.getPath().toUpperCase(Locale.ROOT));
    }

    public double clamp(double frequency) {
        double f = Double.isFinite(frequency) ? frequency : defaultFrequency;
        f = Math.clamp(f, min, max);
        return Math.round(f * 100.0D) / 100.0D;
    }

    public String display(double frequency, ResourceLocation id) {
        return String.format(Locale.ROOT, "%.2f %s %s", frequency, unit, pathType(id));
    }
}