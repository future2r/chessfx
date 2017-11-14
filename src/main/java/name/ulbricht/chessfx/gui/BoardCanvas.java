package name.ulbricht.chessfx.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import name.ulbricht.chessfx.core.*;
import name.ulbricht.chessfx.gui.design.BoardRenderer;
import name.ulbricht.chessfx.gui.design.BoardRendererContext;

import java.util.List;
import java.util.stream.Collectors;

final class BoardCanvas extends Canvas {

    private static final class Dimensions {

        final double xOffset;
        final double yOffset;
        final double borderSize;
        final double squareSize;

        Dimensions(double width, double height, BoardRenderer renderer) {
            double prefSquareSize = renderer.getPrefSquareSize();
            double prefBorderSize = renderer.getPrefBorderSize();
            double prefBoardWidth = 2 * prefBorderSize + Coordinate.COLUMNS * prefSquareSize;
            double prefBoardHeight = 2 * prefBorderSize + Coordinate.ROWS * prefSquareSize;

            double widthScale = width / prefBoardWidth;
            double heightScale = height / prefBoardHeight;
            double scale = Math.min(widthScale, heightScale);

            this.xOffset = (width - (scale * prefBoardWidth)) / 2;
            this.yOffset = (height - (scale * prefBoardHeight)) / 2;

            this.borderSize = scale * prefBorderSize;
            this.squareSize = scale * prefSquareSize;
        }
    }

    private class RendererContextImpl implements BoardRendererContext {

        @Override
        public Board getBoard() {
            return BoardCanvas.this.gameProperty().get().getBoard();
        }

        @Override
        public boolean isBoardFocused() {
            return BoardCanvas.this.isFocused();
        }

        @Override
        public Coordinate getFocusedSquare() {
            return BoardCanvas.this.focusedSquareProperty().get();
        }

        @Override
        public Coordinate getSelectedSquare() {
            return BoardCanvas.this.selectedSquareProperty().get();
        }

        @Override
        public boolean isDisplayedToSquare(Coordinate coordinate) {
            return BoardCanvas.this.displayedMovesProperty().get().stream()
                    .anyMatch(m -> coordinate.equals(m.getTo()));
        }

        @Override
        public boolean isDisplayedCapturedSquare(Coordinate coordinate) {
            return BoardCanvas.this.displayedMovesProperty().get().stream()
                    .filter(m -> m.getCaptured().isPresent())
                    .anyMatch(m -> coordinate.equals(m.getCaptured().get()));
        }
    }

    private final BoardRendererContext rendererContext = new RendererContextImpl();
    private final ReadOnlyObjectWrapper<Game> gameProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Coordinate> selectedSquareProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Coordinate> focusedSquareProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ObservableList<Move>> displayedMovesProperty = new ReadOnlyObjectWrapper(FXCollections.observableArrayList());
    private final ObjectProperty<BoardRenderer> rendererProperty = new SimpleObjectProperty<>();

    BoardCanvas(Game game) {
        this.gameProperty.set(game);

        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());
        focusedProperty().addListener(e -> draw());

        gameProperty().addListener(e -> draw());
        selectedSquareProperty().addListener(e -> draw());
        selectedSquareProperty().addListener(e -> updateDisplayedMoves());
        focusedSquareProperty().addListener(e -> draw());
        displayedMovesProperty().get().addListener((ListChangeListener<? super Move>) c -> draw());
        rendererProperty().addListener(e -> draw());
    }

    BoardRendererContext getRendererContext() {
        return this.rendererContext;
    }

    ReadOnlyObjectProperty<Game> gameProperty() {
        return this.gameProperty.getReadOnlyProperty();
    }

    public Game getGame() {
        return gameProperty().get();
    }

    ReadOnlyObjectProperty<Coordinate> selectedSquareProperty() {
        return this.selectedSquareProperty.getReadOnlyProperty();
    }

    public Coordinate getSelectedSquare() {
        return selectedSquareProperty().get();
    }

    void selectSquareAt(Coordinate coordinate) {
        this.selectedSquareProperty.set(coordinate);
    }

    void clearSquareSelection() {
        this.selectedSquareProperty.set(null);
    }

    ReadOnlyObjectProperty<Coordinate> focusedSquareProperty() {
        return this.focusedSquareProperty.getReadOnlyProperty();
    }

    public Coordinate getFocusedSquare() {
        return focusedSquareProperty().get();
    }

    void focusSquareAt(Coordinate coordinate) {
        this.focusedSquareProperty.set(coordinate);
    }

    void clearSquareFocus() {
        this.focusedSquareProperty.set(null);
    }

    ReadOnlyObjectProperty<ObservableList<Move>> displayedMovesProperty() {
        return this.displayedMovesProperty.getReadOnlyProperty();
    }

    ObservableList<Move> getDisplayedMoves() {
        return displayedMovesProperty.get();
    }

    ObjectProperty<BoardRenderer> rendererProperty() {
        return this.rendererProperty;
    }

    BoardRenderer getRenderer() {
        return rendererProperty().get();
    }

    void setRenderer(BoardRenderer renderer) {
        rendererProperty().set(renderer);
    }

    Coordinate getSquareAt(double x, double y) {
        if (rendererProperty().get() != null) {
            Dimensions dim = new Dimensions(getWidth(), getHeight(), rendererProperty().get());
            int columnIndex = (int) Math.floor((x - dim.xOffset - dim.borderSize) / dim.squareSize);
            int rowIndex = (int) Math.floor(Coordinate.ROWS - (y - dim.yOffset - dim.borderSize) / dim.squareSize);
            if (columnIndex >= 0 && columnIndex < Coordinate.COLUMNS && rowIndex >= 0 && rowIndex < Coordinate.ROWS) {
                return Coordinate.valueOf(columnIndex, rowIndex);
            }
        }
        return null;
    }

    void draw() {
        double width = getWidth();
        double height = getHeight();

        // clear the drawing
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        if (rendererProperty().get() == null) return;

        // calculate the dimensions
        Dimensions dim = new Dimensions(width, height, rendererProperty().get());

        // draw the canvas background
        rendererProperty().get().drawBackground(gc, width, height);

        // draw vertical borders
        double xLeftBorder = dim.xOffset;
        double xRightBorder = dim.xOffset + dim.borderSize + (Coordinate.COLUMNS * dim.squareSize);
        for (int rowIndex = 0; rowIndex < Coordinate.ROWS; rowIndex++) {
            double yBorder = dim.yOffset + dim.borderSize + ((Coordinate.ROWS - rowIndex - 1) * dim.squareSize);

            // left border
            gc.save();
            gc.translate(xLeftBorder, yBorder);
            rendererProperty().get().drawBorder(gc, dim.borderSize, dim.squareSize, BoardRenderer.Border.LEFT, rowIndex);
            gc.restore();

            // right border
            gc.save();
            gc.translate(xRightBorder, yBorder);
            rendererProperty().get().drawBorder(gc, dim.borderSize, dim.squareSize, BoardRenderer.Border.RIGHT, rowIndex);
            gc.restore();
        }

        // draw the horizontal borders
        double yTopBorder = dim.yOffset;
        double yBottomBorder = dim.yOffset + dim.borderSize + (Coordinate.ROWS * dim.squareSize);
        for (int columnIndex = 0; columnIndex < Coordinate.COLUMNS; columnIndex++) {
            double xBorder = dim.xOffset + dim.borderSize + (columnIndex * dim.squareSize);
            // top border
            gc.save();
            gc.translate(xBorder, yTopBorder);
            rendererProperty().get().drawBorder(gc, dim.squareSize, dim.borderSize, BoardRenderer.Border.TOP, columnIndex);
            gc.restore();

            // bottom border
            gc.save();
            gc.translate(xBorder, yBottomBorder);
            rendererProperty().get().drawBorder(gc, dim.squareSize, dim.borderSize, BoardRenderer.Border.BOTTOM, columnIndex);
            gc.restore();
        }

        // draw top-left corner
        gc.save();
        gc.translate(xLeftBorder, yTopBorder);
        rendererProperty().get().drawCorner(gc, dim.borderSize, BoardRenderer.Corner.TOP_LEFT);
        gc.restore();

        // draw top-right corner
        gc.save();
        gc.translate(xRightBorder, yTopBorder);
        rendererProperty().get().drawCorner(gc, dim.borderSize, BoardRenderer.Corner.TOP_RIGHT);
        gc.restore();

        // draw bottom-left corner
        gc.save();
        gc.translate(xLeftBorder, yBottomBorder);
        rendererProperty().get().drawCorner(gc, dim.borderSize, BoardRenderer.Corner.BOTTOM_LEFT);
        gc.restore();

        // draw bottom-right corner
        gc.save();
        gc.translate(xRightBorder, yBottomBorder);
        rendererProperty().get().drawCorner(gc, dim.borderSize, BoardRenderer.Corner.BOTTOM_RIGHT);
        gc.restore();

        // draw the squares
        for (Coordinate coordinate : Coordinate.values().collect(Collectors.toList())) {

            double squareXOffset = dim.borderSize + dim.xOffset + (coordinate.getColumnIndex() * dim.squareSize);
            double squareYOffset = dim.borderSize + (dim.yOffset + ((Coordinate.ROWS - 1) * dim.squareSize))
                    - (coordinate.getRowIndex() * dim.squareSize);

            gc.save();
            gc.translate(squareXOffset, squareYOffset);
            rendererProperty().get().drawSquare(gc, dim.squareSize, coordinate);
            gc.restore();
        }
    }

    private void updateDisplayedMoves() {
        displayedMovesProperty().get().clear();
        Coordinate selected = selectedSquareProperty().get();
        if (selected != null) {
            List<Move> legalMoves = gameProperty().get().getLegalMoves().get(selected);
            if (legalMoves != null) displayedMovesProperty().get().addAll(legalMoves);
        }
    }
}
