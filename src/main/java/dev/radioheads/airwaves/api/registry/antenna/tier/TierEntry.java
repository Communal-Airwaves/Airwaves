package dev.radioheads.airwaves.api.registry.antenna.tier;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.radioheads.airwaves.api.antenna.Tier;
import dev.radioheads.airwaves.api.radio.Modulation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TierEntry extends RegistryEntry<Tier, Tier> {
    public TierEntry(AbstractRegistrate<?> owner, DeferredHolder<Tier, Tier> key) {
        super(owner, key);
    }

    public static TierEntry cast(RegistryEntry<Modulation, Modulation> entry) {
        return RegistryEntry.cast(TierEntry.class, entry);
    }
}
