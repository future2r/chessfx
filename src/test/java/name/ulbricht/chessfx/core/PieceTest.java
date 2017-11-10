package name.ulbricht.chessfx.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class PieceTest {

    @Test
    public void testCreate() {
        Piece piece = new Piece(Piece.Type.KING, Player.WHITE);

        assertEquals(Piece.Type.KING, piece.getType());
        assertEquals(Player.WHITE, piece.getPlayer());
    }

    @Test
    public void testCreate_TypeNull() {
        try {
            new Piece(null, Player.WHITE);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertEquals("type cannot be null", ex.getMessage());
        }
    }

    @Test
    public void testCreate_PlayerNull() {
        try {
            new Piece(Piece.Type.KING, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertEquals("player cannot be null", ex.getMessage());
        }
    }

    @Test
    public void testToString() {
        Piece piece = new Piece(Piece.Type.KING, Player.WHITE);

        assertEquals(String.format("%s (%s)", Piece.Type.KING.getDisplayName(), Player.WHITE.getDisplayName()), piece.toString());
    }
}