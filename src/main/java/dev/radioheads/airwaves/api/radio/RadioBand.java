package dev.radioheads.airwaves.api.radio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import dev.radioheads.airwaves.registry.AirwaveModulations;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record RadioBand(
        double min,
        double max,
        String unit,
        Optional<Double> defaultFrequency,
        Optional<String> pathTypeOverride,
        Holder<Modulation> modulation
) {
    public static final Codec<RadioBand> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("min").forGetter(RadioBand::min),
            Codec.DOUBLE.fieldOf("max").forGetter(RadioBand::max),
            Codec.STRING.fieldOf("unit").forGetter(RadioBand::unit),
            Codec.DOUBLE.optionalFieldOf("defaultFrequency").forGetter(RadioBand::defaultFrequency),
            Codec.STRING.optionalFieldOf("path_type").forGetter(RadioBand::pathTypeOverride),
            RegistryFixedCodec.create(AirwaveRegistries.MODULATION)
                    .optionalFieldOf("modulation", AirwaveModulations.FM)
                    .forGetter(RadioBand::modulation)
    ).apply(inst, RadioBand::new));

    public RadioBand {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min > max) {
            throw new IllegalArgumentException("Invalid radio band range: " + min + "–" + max);
        }

        defaultFrequency.ifPresent(frequency -> {
            if (!Double.isFinite(frequency) || frequency < min || frequency > max) {
                throw new IllegalArgumentException("Default frequency must be finite and within the band range");
            }
        });
    }

    public RadioBand(double min, double max, String unit) {
        this(min, max, unit, Optional.empty(), Optional.empty(), AirwaveModulations.FM);
    }

    public RadioBand(double min, double max, String unit, double defaultFrequency) {
        this(min, max, unit, Optional.of(defaultFrequency), Optional.empty(), AirwaveModulations.FM);
    }

    public RadioBand withPathType(String pathType) {
        return new RadioBand(min, max, unit, defaultFrequency, Optional.of(pathType), modulation);
    }

    public RadioBand withModulation(Holder<Modulation> modulation) {
        return new RadioBand(min, max, unit, defaultFrequency, pathTypeOverride, modulation);
    }

    public String pathType(ResourceLocation id) {
        return pathTypeOverride.orElseGet(() -> id.getPath().toUpperCase(Locale.ROOT));
    }

    public double clamp(double frequency) {
        double fallback = defaultFrequency.orElse(min);
        double f = Double.isFinite(frequency) ? frequency : fallback;

        f = Math.clamp(f, min, max);

        return Math.round(f * 100.0D) / 100.0D;
    }

    public String display(double frequency, ResourceLocation id) {
        return String.format(Locale.ROOT, "%.2f %s %s", frequency, unit, pathType(id));
    }
}