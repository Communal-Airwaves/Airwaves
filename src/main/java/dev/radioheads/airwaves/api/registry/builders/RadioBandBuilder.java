package dev.radioheads.airwaves.api.registry.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.radio.RadioBand;
import dev.radioheads.airwaves.api.registry.radio.RadioBandEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class RadioBandBuilder<P> extends AbstractBuilder<RadioBand, RadioBand, P, RadioBandBuilder<P>> {
    private final NonNullSupplier<RadioBand> factory;

    public RadioBandBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                            ResourceKey<? extends Registry<RadioBand>> registryKey, NonNullSupplier<RadioBand> factory) {
        super(owner, parent, name, callback, registryKey);
        this.factory = factory;
    }

    @Override
    protected @NotNull RadioBand createEntry() {
        return factory.get();
    }

    @Override
    protected RegistryEntry<RadioBand, RadioBand> createEntryWrapper(DeferredHolder<RadioBand, RadioBand> delegate) {
        return new RadioBandEntry(getOwner(), delegate);
    }

    @Override
    public RadioBandEntry register() {
        return (RadioBandEntry) super.register();
    }
}