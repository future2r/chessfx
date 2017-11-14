package name.ulbricht.chessfx.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class MoveTest {

    @Test
    public void testSimpleNoCpature() {
        Board board = new Board();

        Coordinate from = Coordinate.valueOf("d4");
        board.setPiece(from, new Piece(Piece.Type.PAWN, Player.WHITE));

        Coordinate to = Coordinate.valueOf("d5");

        Move move = Move.simple(board, from, to);

        assertEquals(from, move.getFrom());
        assertEquals(to, move.getTo());
        assertFalse(move.getCaptures().isPresent());
    }

    @Test
    public void testSimpleCaptures() {
        Board board = new Board();

        Coordinate from = Coordinate.valueOf("d4");
        board.setPiece(from, new Piece(Piece.Type.QUEEN, Player.WHITE));

        Coordinate to = Coordinate.valueOf("d5");
        board.setPiece(to, new Piece(Piece.Type.PAWN, Player.BLACK));

        Move move = Move.simple(board, from, to);

        assertEquals(from, move.getFrom());
        assertEquals(to, move.getTo());
        assertTrue(move.getCaptures().isPresent());
        assertEquals(to, move.getCaptures().get());
    }
}
