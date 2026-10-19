package dev.radioheads.airwaves.api.board;

import dev.radioheads.airwaves.api.registry.board.BoardPart;

public sealed interface Cell permits Cell.Empty, Cell.Trace, Cell.Placed {
    record Empty() implements Cell {
    }

    record Trace() implements Cell {
    }

    record Placed(BoardPart part) implements Cell {

    }

    Cell EMPTY = new Empty();
    Cell TRACE = new Trace();

    static Cell placed(BoardPart part) {
        return new Placed(part);
    }
}
