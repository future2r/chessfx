package name.ulbricht.chess.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

final class CoordinateTest {

    @ParameterizedTest(name = "{index}: [{arguments}]")
    @CsvFileSource(resources = "coordinates.txt", delimiter = '\t' /* numLinesToSkip=1 (skip header comes in 5.1)*/)
    void testIndices(int fieldIndex, int columnIndex, char columnName, int rowIndex, char rowName, String fieldName) {

        // parse from name
        Coordinate coordinate = Coordinate.valueOf(fieldName);

        // check field index
        assertEquals(fieldIndex, coordinate.ordinal(), "fieldIndex");

        // check column
        assertEquals(columnIndex, coordinate.columnIndex, "columnIndex");
        assertEquals(columnName, coordinate.columnName, "columnName");

        // check row
        assertEquals(rowIndex, coordinate.rowIndex, "rowIndex");
        assertEquals(rowName, coordinate.rowName, "rowName");

        // check fieldName
        assertEquals(fieldName, coordinate.toString(), "fieldName");
    }

    @Test
    void testInvalidIndices() {

        int[] indices = new int[]{-10, -1, 64, 100};

        for (int index : indices) {
            try {
                Coordinate.valueOf(index);
                fail("IndexOutOfBoundsException expected");
            } catch (IndexOutOfBoundsException ex) {
                assertEquals(Integer.toString(index), ex.getMessage());
            }
        }
    }

    @Test
    void testNullNames() {
        try {
            Coordinate.valueOf(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertEquals("Name is null", ex.getMessage());
        }
    }

    @Test
    void testInvalidNames() {

        String[] names = {"", "a", "1", "a 1", "1a", "a9", "x1", "x9"};

        for (String name : names) {
            try {
                Coordinate.valueOf(name);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException ex) {
                assertEquals("No enum constant name.ulbricht.chess.game.Coordinate." + name, ex.getMessage());
            }
        }
    }

    @Test
    void testGoLeft() {
        assertEquals(Coordinate.g1, Coordinate.h1.go(MoveDirection.LEFT));
        assertEquals(Coordinate.d5, Coordinate.e5.go(MoveDirection.LEFT));
        assertEquals(Coordinate.a8, Coordinate.b8.go(MoveDirection.LEFT));

        assertNull(Coordinate.a5.go(MoveDirection.LEFT));
    }

    @Test
    void testGoRight() {
        assertEquals(Coordinate.b1, Coordinate.a1.go(MoveDirection.RIGHT));
        assertEquals(Coordinate.f5, Coordinate.e5.go(MoveDirection.RIGHT));
        assertEquals(Coordinate.h8, Coordinate.g8.go(MoveDirection.RIGHT));

        assertNull(Coordinate.h5.go(MoveDirection.RIGHT));
    }

    @Test
    void testGoUp() {
        assertEquals(Coordinate.a2, Coordinate.a1.go(MoveDirection.UP));
        assertEquals(Coordinate.e6, Coordinate.e5.go(MoveDirection.UP));
        assertEquals(Coordinate.h8, Coordinate.h7.go(MoveDirection.UP));

        assertNull(Coordinate.d8.go(MoveDirection.UP));
    }

    @Test
    void testMoveDown() {
        assertEquals(Coordinate.a7, Coordinate.a8.go(MoveDirection.DOWN));
        assertEquals(Coordinate.e4, Coordinate.e5.go(MoveDirection.DOWN));
        assertEquals(Coordinate.h1, Coordinate.h2.go(MoveDirection.DOWN));

        assertNull(Coordinate.e1.go(MoveDirection.DOWN));
    }
}
