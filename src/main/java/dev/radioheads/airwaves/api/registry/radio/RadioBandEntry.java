package dev.radioheads.airwaves.api.registry.radio;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.radio.RadioBand;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RadioBandEntry<T extends RadioBand> extends RegistryEntry<RadioBand, T> {
    public RadioBandEntry(AbstractRegistrate<?> owner, DeferredHolder<RadioBand, T> key) {
        super(owner, key);
    }

    public static <T extends RadioBand> RadioBandEntry<T> cast(RegistryEntry<RadioBand, T> entry) {
        return RegistryEntry.cast(RadioBandEntry.class, entry);
    }
}
