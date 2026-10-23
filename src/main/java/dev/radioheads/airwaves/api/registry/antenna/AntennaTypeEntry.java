package dev.radioheads.airwaves.api.registry.antenna;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.antenna.AntennaType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AntennaTypeEntry extends RegistryEntry<AntennaType, AntennaType> {
    public AntennaTypeEntry(AbstractRegistrate<?> owner, DeferredHolder<AntennaType, AntennaType> key) {
        super(owner, key);
    }
}
