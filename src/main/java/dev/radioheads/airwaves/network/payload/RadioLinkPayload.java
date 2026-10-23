package dev.radioheads.airwaves.network.payload;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.api.radio.Modulation;
import dev.radioheads.airwaves.api.registry.AirwaveRegistries;
import dev.radioheads.airwaves.api.util.RangedFloat;
import dev.radioheads.airwaves.network.RadioLinkStore;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RadioLinkPayload(
        UUID channelId,
        Kind kind,
        RangedFloat quality,
        Holder<Modulation> modulation,
        float gain,
        long serverTick
) implements CustomPacketPayload {
    public static final Type<RadioLinkPayload> TYPE = new Type<>(Airwaves.asResource("radio_link"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RadioLinkPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RadioLinkPayload::channelId,
            ByteBufCodecs.VAR_INT.map(i -> Kind.values()[i], Kind::ordinal), RadioLinkPayload::kind,
            ByteBufCodecs.FLOAT.map(v -> RangedFloat.clamped(v, 0.0F, 1.0F), RangedFloat::value), RadioLinkPayload::quality,
            ByteBufCodecs.holderRegistry(AirwaveRegistries.MODULATION), RadioLinkPayload::modulation,
            ByteBufCodecs.FLOAT, RadioLinkPayload::gain,
            ByteBufCodecs.VAR_LONG, RadioLinkPayload::serverTick,
            RadioLinkPayload::new
    );

    public enum Kind {
        SINK,
        SPEAKER,
        HANDHELD,
        WALKIE,
        WIRED
    }

    public RadioLinkPayload {
        if (!Float.isFinite(gain) || gain < 0.0F) throw new IllegalArgumentException("gain must be finite and >= 0: " + gain);
    }

    public void sendTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, this);
    }

    public static void onReceive(RadioLinkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> RadioLinkStore.accept(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
