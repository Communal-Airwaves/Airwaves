package dev.radioheads.airwaves.api.registry.board.step;

import java.util.List;

public record BoardStepMode(Applier applier) {
    @FunctionalInterface
    public interface Applier {
        void apply(List<BoardStep> out, BoardStep step, boolean isSource, int count);
    }

    public static void addOnce(List<BoardStep> out, BoardStep step) {
        if (!out.contains(step)) {
            out.add(step);
        }
    }
}
