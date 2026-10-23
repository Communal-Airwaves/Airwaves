package dev.radioheads.airwaves.api.registry.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.antenna.AntennaType;
import dev.radioheads.airwaves.api.registry.antenna.AntennaTypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AntennaTypeBuilder<P> extends AbstractBuilder<AntennaType, AntennaType, P, AntennaTypeBuilder<P>> {
    private final NonNullSupplier<AntennaType> factory;

    public AntennaTypeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<AntennaType>> registryKey, NonNullSupplier<AntennaType> factory) {
        super(owner, parent, name, callback, registryKey);
        this.factory = factory;
    }

    @Override
    protected AntennaType createEntry() {
        return factory.get();
    }

    @Override
    protected AntennaTypeEntry createEntryWrapper(DeferredHolder<AntennaType, AntennaType> delegate) {
        return new AntennaTypeEntry(getOwner(), delegate);
    }

    @Override
    public AntennaTypeEntry register() {
        return (AntennaTypeEntry) super.register();
    }
}
