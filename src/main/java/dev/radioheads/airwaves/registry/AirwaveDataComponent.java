package dev.radioheads.airwaves.registry;

import com.mojang.serialization.Codec;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Tuning;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AirwaveDataComponent {
    private AirwaveDataComponent() {}

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Airwaves.MOD_ID);

    public static final DataComponentType<Tuning> TUNING = register("tuning", Tuning.CODEC, Tuning.STREAM_CODEC);

    private static <T> DataComponentType<T> register(String name, Codec<T> persistent, StreamCodec<? super RegistryFriendlyByteBuf, T> network) {
        DataComponentType<T> type = new DataComponentType.Builder<T>().persistent(persistent).networkSynchronized(network).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }
}
