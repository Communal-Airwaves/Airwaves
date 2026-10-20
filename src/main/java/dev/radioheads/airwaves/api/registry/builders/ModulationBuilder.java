package dev.radioheads.airwaves.api.registry.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.registry.modulation.ModulationEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModulationBuilder<P> extends AbstractBuilder<Modulation, Modulation, P, ModulationBuilder<P>> {
    private final NonNullSupplier<Modulation> factory;

    public ModulationBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<Modulation>> registryKey, NonNullSupplier<Modulation> factory) {
        super(owner, parent, name, callback, registryKey);
        this.factory = factory;
    }

    @Override
    protected Modulation createEntry() {
        return factory.get();
    }

    @Override
    protected RegistryEntry<Modulation, Modulation> createEntryWrapper(DeferredHolder<Modulation, Modulation> delegate) {
        return new ModulationEntry(getOwner(), delegate);
    }

    @Override
    public ModulationEntry register() {
        return (ModulationEntry) super.register();
    }
}
