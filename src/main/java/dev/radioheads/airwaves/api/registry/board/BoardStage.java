package dev.radioheads.airwaves.api.registry.board;

import dev.radioheads.airwaves.api.registry.board.step.BoardStep;
import dev.radioheads.airwaves.api.registry.board.step.BoardStepMode;

import java.util.Optional;

public record BoardStage(
        int color,
        boolean halfCount,
        int clockBonus,
        Optional<BoardSource> source,
        Optional<BoardStep> step,
        BoardStepMode mode
) {}
