package dev.radioheads.airwaves.api.registry.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.radioheads.airwaves.api.antenna.Tier;
import dev.radioheads.airwaves.api.registry.antenna.tier.TierEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TierBuilder<P> extends AbstractBuilder<Tier, Tier, P, TierBuilder<P>> {
    private final NonNullSupplier<Tier> factory;

    public TierBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<Tier>> registryKey, NonNullSupplier<Tier> factory) {
        super(owner, parent, name, callback, registryKey);
        this.factory = factory;
    }

    @Override
    protected Tier createEntry() {
        return factory.get();
    }

    @Override
    protected RegistryEntry<Tier, Tier> createEntryWrapper(DeferredHolder<Tier, Tier> delegate) {
        return new TierEntry(getOwner(), delegate);
    }

    @Override
    public TierEntry register() {
        return (TierEntry) super.register();
    }
}
