package dev.radioheads.airwaves.network;

import dev.radioheads.airwaves.network.payload.RadioLinkPayload;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RadioLinkStore {
    public static final long TIMEOUT_MS = 5_000L;

    private static final Map<UUID, Entry> LINKS = new ConcurrentHashMap<>();

    private record Entry(RadioLinkPayload payload, long receivedAtMs) {}

    private RadioLinkStore() {}

    public static void accept(RadioLinkPayload payload) {
        long now = System.currentTimeMillis();
        LINKS.merge(payload.channelId(), new Entry(payload, now), (old, incoming) ->
                incoming.payload().serverTick() >= old.payload().serverTick()
                        ? incoming
                        : new Entry(old.payload(), incoming.receivedAtMs()));
    }

    public static Optional<RadioLinkPayload> get(UUID channelId) {
        Entry entry = LINKS.get(channelId);
        if (entry == null) return Optional.empty();
        if (System.currentTimeMillis() - entry.receivedAtMs() > TIMEOUT_MS) {
            LINKS.remove(channelId, entry);
            return Optional.empty();
        }
        return Optional.of(entry.payload());
    }

    public static void remove(UUID channelId) {
        LINKS.remove(channelId);
    }

    public static void clear() {
        LINKS.clear();
    }
}
