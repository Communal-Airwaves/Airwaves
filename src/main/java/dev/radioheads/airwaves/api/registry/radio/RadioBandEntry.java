package dev.radioheads.airwaves.api.registry.radio;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.radio.RadioBand;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RadioBandEntry extends RegistryEntry<RadioBand, RadioBand> {
    public RadioBandEntry(AbstractRegistrate<?> owner, DeferredHolder<RadioBand, RadioBand> key) {
        super(owner, key);
    }

    public static RadioBandEntry cast(RegistryEntry<RadioBand, RadioBand> entry) {
        return RegistryEntry.cast(RadioBandEntry.class, entry);
    }
}
