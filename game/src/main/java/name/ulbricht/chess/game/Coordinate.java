package name.ulbricht.chess.game;

/**
 * Represents a coordinate on a board. These coordinates are based on a unique zero-based index (the ordinal value)
 * representing each coordinate. All other properties of a coordinate are derived from that index. The index 0
 * represents the lower left corner of the board. The coordinate objects are cached. Two objects representing the same
 * index will be identical.
 */
public enum Coordinate {

    a1, b1, c1, d1, e1, f1, g1, h1,
    a2, b2, c2, d2, e2, f2, g2, h2,
    a3, b3, c3, d3, e3, f3, g3, h3,
    a4, b4, c4, d4, e4, f4, g4, h4,
    a5, b5, c5, d5, e5, f5, g5, h5,
    a6, b6, c6, d6, e6, f6, g6, h6,
    a7, b7, c7, d7, e7, f7, g7, h7,
    a8, b8, c8, d8, e8, f8, g8, h8;

    public final int columnIndex;
    public final String columnName;
    public final int rowIndex;
    public final String rowName;

    Coordinate() {
        this.columnIndex = ordinal() % ROWS;
        this.columnName = toColumnName(this.columnIndex);
        this.rowIndex = ordinal() / COLUMNS;
        this.rowName = toRowName(this.rowIndex);
    }

    /**
     * Number of columns for this kind of coordinates.
     */
    public static final int COLUMNS = 8;

    /**
     * Number of rows for this kind of coordinates.
     */
    public static final int ROWS = 8;

    /**
     * Returns the coordinate that can be reached by moving the specified offset from this coordinate. If there is no
     * such square (because the board ends here) the return value will be {@code null}..
     *
     * @param columnOffset positive offset will move right, negative offset will move left
     * @param rowOffset    positive offset will move up, negative offset will move down
     * @return the coordinate or {@code null}.
     */
    public Coordinate move(int columnOffset, int rowOffset) {
        int newColumn = this.columnIndex + columnOffset;
        int newRow = this.rowIndex + rowOffset;

        if (newColumn >= 0 && newColumn < COLUMNS && newRow >= 0 && newRow < ROWS)
            return Coordinate.valueOf(newColumn, newRow);
        else return null;
    }

    public Coordinate move(MoveDirection direction) {
        return move(direction, 1);
    }

    public Coordinate move(MoveDirection direction, int steps) {
        return move(steps * direction.columnOffset, steps * direction.rowOffset);
    }

    /**
     * Returns the column name for the given column index. This method can be used by the application to display column
     * names when drawing a board.
     *
     * @param columnIndex the column index
     * @return a name for the column index
     * @throws IndexOutOfBoundsException if the index exceeds the lower or upper limit
     */
    public static String toColumnName(int columnIndex) throws IndexOutOfBoundsException {
        return Character.valueOf((char) (97 + checkColumnIndex(columnIndex))).toString();
    }

    /**
     * Returns the row name for the given row index. This method can be used by the application to display row names
     * when drawing a board.
     *
     * @param rowIndex the row index
     * @return a name for the row index
     * @throws IndexOutOfBoundsException if the index exceeds the lower or upper limit
     */
    public static String toRowName(int rowIndex) {
        return Character.valueOf((char) (49 + checkRowIndex(rowIndex))).toString();
    }

    /**
     * Returns thecoordinate object for the given index.
     *
     * @param index the index
     * @return a coordinate object
     * @throws IndexOutOfBoundsException if the index exceeds the lower or upper limit.
     */
    public static Coordinate valueOf(int index) throws IndexOutOfBoundsException {
        return values()[index];
    }

    /**
     * Returns a cached coordinate object for the given column index and row index.
     *
     * @param columnIndex the column index
     * @param rowIndex    the row index
     * @return a coordinate object
     * @throws IndexOutOfBoundsException if the index exceeds the lower or upper limit.
     */
    public static Coordinate valueOf(int columnIndex, int rowIndex) throws IndexOutOfBoundsException {
        return valueOf(checkRowIndex(rowIndex) * COLUMNS + checkColumnIndex(columnIndex));
    }

    private static int checkColumnIndex(int index) {
        if (index < 0 || index >= COLUMNS)
            throw new IndexOutOfBoundsException("Illegal value for column index " + index);
        return index;
    }

    private static int checkRowIndex(int index) {
        if (index < 0 || index >= ROWS)
            throw new IndexOutOfBoundsException("Illegal value for row index " + index);
        return index;
    }
}
