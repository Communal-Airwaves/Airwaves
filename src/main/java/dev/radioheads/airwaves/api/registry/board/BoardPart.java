package dev.radioheads.airwaves.api.registry.board;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public record BoardPart(Supplier<Item> item, boolean standalone) {}