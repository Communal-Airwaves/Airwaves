package dev.radioheads.airwaves.api.antenna;

import net.minecraft.network.chat.Component;

public enum CompassPoint {
    NORTH("north"),
    NORTH_EAST("north_east"),
    EAST("east"),
    SOUTH_EAST("south_east"),
    SOUTH("south"),
    SOUTH_WEST("south_west"),
    WEST("west"),
    NORTH_WEST("north_west");

    private final String key;

    CompassPoint(String key) {
        this.key = key;
    }

    public Component component() {
        return Component.translatable("airwaves.compass." + key);
    }

    public static CompassPoint nearest(int degrees) {
        return values()[Math.floorMod(Math.round(degrees / 45.0f), 8)];
    }
}
