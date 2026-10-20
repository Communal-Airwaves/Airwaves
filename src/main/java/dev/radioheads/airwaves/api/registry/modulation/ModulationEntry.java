package dev.radioheads.airwaves.api.registry.modulation;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.radio.Modulation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModulationEntry extends RegistryEntry<Modulation, Modulation> {
    public ModulationEntry(AbstractRegistrate<?> owner, DeferredHolder<Modulation, Modulation> key) {
        super(owner, key);
    }

    public static ModulationEntry cast(RegistryEntry<Modulation, Modulation> entry) {
        return RegistryEntry.cast(ModulationEntry.class, entry);
    }
}
