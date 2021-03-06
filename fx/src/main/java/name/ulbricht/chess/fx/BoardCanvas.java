package name.ulbricht.chess.fx;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import name.ulbricht.chess.fx.design.BoardDesign;
import name.ulbricht.chess.fx.design.BoardRenderer;
import name.ulbricht.chess.game.*;

import java.util.List;
import java.util.Optional;

final class BoardCanvas extends Canvas {

    private static final class Dimensions {

        final double xOffset;
        final double yOffset;
        final double borderSize;
        final double squareSize;

        Dimensions(double width, double height) {
            double prefSquareSize = 100;
            double prefBorderSize = 30;
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

    private final Tooltip tooltip;

    private Game game;
    private BoardRenderer renderer;

    private final ReadOnlyObjectWrapper<Player> activePlayerProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<CheckState> checkStateProperty = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyBooleanWrapper undoAvailable = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper redoAvailable = new ReadOnlyBooleanWrapper();
    private final ObjectProperty<Coordinate> selectedSquareProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Coordinate> focusedSquareProperty = new SimpleObjectProperty<>();
    private final ReadOnlyListWrapper<Ply> displayedPliesProperty = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    BoardCanvas() {
        this.game = new Game();

        setFocusTraversable(true);

        this.tooltip = new Tooltip();
        this.tooltip.setShowDelay(Duration.ZERO);
        this.tooltip.setOnShowing(e -> tooltipShowing());

        setOnMouseMoved(this::mouseMoved);
        setOnMousePressed(this::mousePressed);
        setOnKeyPressed(this::keyPressed);

        widthProperty().addListener((observable, oldValue, newValue) -> draw());
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
        focusedProperty().addListener((observable, oldValue, newValue) -> draw());

        this.selectedSquareProperty.addListener((observable, oldValue, newValue) -> draw());
        this.selectedSquareProperty.addListener((observable, oldValue, newValue) -> updateDisplayedPlies());
        this.focusedSquareProperty.addListener((observable, oldValue, newValue) -> draw());
        this.displayedPliesProperty.addListener((observable, oldValue, newValue) -> draw());

        updateProperties();
    }

    Game getGame() {
        return this.game;
    }

    void setGame(Game game) {
        this.game = game;
        this.focusedSquareProperty.set(null);
        this.selectedSquareProperty.set(null);
        updateProperties();
        draw();
    }

    void setDesign(BoardDesign design) {
        this.renderer = design.createRenderer();
        draw();
    }

    ReadOnlyObjectProperty<Player> activePlayerProperty() {
        return this.activePlayerProperty.getReadOnlyProperty();
    }

    ReadOnlyObjectProperty<CheckState> checkStateProperty() {
        return this.checkStateProperty.getReadOnlyProperty();
    }

    ReadOnlyBooleanProperty undoAvailableProperty() {
        return this.undoAvailable.getReadOnlyProperty();
    }

    ReadOnlyBooleanProperty redoAvailableProperty() {
        return this.redoAvailable.getReadOnlyProperty();
    }

    ObjectProperty<Coordinate> selectedSquareProperty() {
        return this.selectedSquareProperty;
    }

    void undo(){
        this.game.undo();
        this.selectedSquareProperty.set(null);
        updateProperties();
        draw();
    }

    void redo(){
        this.game.redo();
        this.selectedSquareProperty.set(null);
        updateProperties();
        draw();
    }

    private void tooltipShowing() {
        Point2D pos = localToScreen(0, 0);
        this.tooltip.setX(pos.getX());
        this.tooltip.setY(pos.getY());
    }

    private void mouseMoved(MouseEvent e) {
        Coordinate coordinate = getSquareAt(e.getX(), e.getY());
        if (coordinate != null) {
            Optional<Ply> ply = this.displayedPliesProperty.stream()
                    .filter(m -> coordinate.equals(m.target) || coordinate.equals(m.captures))
                    .findFirst();
            if (ply.isPresent()) {
                tooltip.setText(ply.get().getDisplayName());
                Tooltip.install(this, tooltip);
            } else {
                this.tooltip.setText("");
                Tooltip.uninstall(this, tooltip);
            }
            this.focusedSquareProperty.set(coordinate);
        } else {
            this.tooltip.setText("");
            Tooltip.uninstall(this, tooltip);
            this.focusedSquareProperty.set(null);
        }
    }

    private void mousePressed(MouseEvent e) {
        if (!isFocused()) requestFocus();

        Coordinate square = getSquareAt(e.getX(), e.getY());
        if (square != null) selectSquare(square);
    }

    private void keyPressed(KeyEvent e) {
        Coordinate focused = this.focusedSquareProperty.get();
        Coordinate selected = this.selectedSquareProperty.get();

        switch (e.getCode()) {
            case LEFT:
                if (focused != null) {
                    Coordinate left = focused.go(MoveDirection.LEFT);
                    if (left != null) this.focusedSquareProperty.set(left);
                    else
                        this.focusedSquareProperty.set(Coordinate.valueOf(Coordinate.COLUMNS - 1, focused.rowIndex));
                } else this.focusedSquareProperty.set(Coordinate.a1);
                e.consume();
                break;
            case RIGHT:
                if (focused != null) {
                    Coordinate right = focused.go(MoveDirection.RIGHT);
                    if (right != null) this.focusedSquareProperty.set(right);
                    else this.focusedSquareProperty.set(Coordinate.valueOf(0, focused.rowIndex));
                } else this.focusedSquareProperty.set(Coordinate.a1);
                e.consume();
                break;
            case UP:
                if (focused != null) {
                    Coordinate up = focused.go(MoveDirection.UP);
                    if (up != null) this.focusedSquareProperty.set(up);
                    else this.focusedSquareProperty.set(Coordinate.valueOf(focused.columnIndex, 0));
                } else this.focusedSquareProperty.set(Coordinate.a1);
                e.consume();
                break;
            case DOWN:
                if (focused != null) {
                    Coordinate down = focused.go(MoveDirection.DOWN);
                    if (down != null) this.focusedSquareProperty.set(down);
                    else
                        this.focusedSquareProperty.set(Coordinate.valueOf(focused.columnIndex, Coordinate.ROWS - 1));
                } else this.focusedSquareProperty.set(Coordinate.a1);
                e.consume();
                break;
            case ENTER:
                if (focused != null) {
                    selectSquare(focused);
                }
                break;
            case ESCAPE:
                if (selected != null) this.selectedSquareProperty.set(null);
                else if (focused != null) this.focusedSquareProperty.set(null);
                e.consume();
                break;
        }
    }

    private void selectSquare(Coordinate coordinate) {
        // check if we can execute a go
        Optional<Ply> ply = this.displayedPliesProperty.stream()
                .filter(m -> coordinate.equals(m.target) || coordinate.equals(m.captures))
                .findFirst();

        if (ply.isPresent()) {
            performPly(ply.get());
        } else {
            this.selectedSquareProperty.set(coordinate);
        }
    }

    private void performPly(Ply ply) {
        if (ply.type == PlyType.PAWN_PROMOTION) {
            PieceType promotion = PromotionController.showDialog(this, this.renderer, ply);
            if (promotion != null) {
                ply.promotion = promotion;
            } else {
                return;
            }
        }

        this.game.perform(ply);

        this.selectedSquareProperty.set(null);
        this.focusedSquareProperty.set(ply.target);
        updateProperties();
    }

    private void updateProperties(){
        this.activePlayerProperty.set(this.game.getActivePlayer());
        this.checkStateProperty.set(this.game.getCheckState());
        this.undoAvailable.set(this.game.isUndoAvailable());
        this.redoAvailable.set(this.game.isRedoAvailable());
    }

    private Coordinate getSquareAt(double x, double y) {
        if (this.renderer != null) {
            Dimensions dim = new Dimensions(getWidth(), getHeight());
            int columnIndex = (int) Math.floor((x - dim.xOffset - dim.borderSize) / dim.squareSize);
            int rowIndex = (int) Math.floor(Coordinate.ROWS - (y - dim.yOffset - dim.borderSize) / dim.squareSize);
            if (columnIndex >= 0 && columnIndex < Coordinate.COLUMNS && rowIndex >= 0 && rowIndex < Coordinate.ROWS) {
                return Coordinate.valueOf(columnIndex, rowIndex);
            }
        }
        return null;
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();

        // clear the drawing
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        if (this.renderer == null) return;

        // calculate the dimensions
        Dimensions dim = new Dimensions(width, height);

        // draw the canvas background
        this.renderer.drawBackground(gc, width, height);

        // draw vertical borders
        double xLeftBorder = dim.xOffset;
        double xRightBorder = dim.xOffset + dim.borderSize + (Coordinate.COLUMNS * dim.squareSize);
        for (int rowIndex = 0; rowIndex < Coordinate.ROWS; rowIndex++) {
            double yBorder = dim.yOffset + dim.borderSize + ((Coordinate.ROWS - rowIndex - 1) * dim.squareSize);

            // left border
            gc.save();
            gc.translate(xLeftBorder, yBorder);
            this.renderer.drawBorder(gc, dim.borderSize, dim.squareSize, BoardRenderer.Border.LEFT, rowIndex, isFocused());
            gc.restore();

            // right border
            gc.save();
            gc.translate(xRightBorder, yBorder);
            this.renderer.drawBorder(gc, dim.borderSize, dim.squareSize, BoardRenderer.Border.RIGHT, rowIndex, isFocused());
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
            this.renderer.drawBorder(gc, dim.squareSize, dim.borderSize, BoardRenderer.Border.TOP, columnIndex, isFocused());
            gc.restore();

            // bottom border
            gc.save();
            gc.translate(xBorder, yBottomBorder);
            this.renderer.drawBorder(gc, dim.squareSize, dim.borderSize, BoardRenderer.Border.BOTTOM, columnIndex, isFocused());
            gc.restore();
        }

        // draw top-left corner
        gc.save();
        gc.translate(xLeftBorder, yTopBorder);
        this.renderer.drawCorner(gc, dim.borderSize, BoardRenderer.Corner.TOP_LEFT, isFocused());
        gc.restore();

        // draw top-right corner
        gc.save();
        gc.translate(xRightBorder, yTopBorder);
        this.renderer.drawCorner(gc, dim.borderSize, BoardRenderer.Corner.TOP_RIGHT, isFocused());
        gc.restore();

        // draw bottom-left corner
        gc.save();
        gc.translate(xLeftBorder, yBottomBorder);
        this.renderer.drawCorner(gc, dim.borderSize, BoardRenderer.Corner.BOTTOM_LEFT, isFocused());
        gc.restore();

        // draw bottom-right corner
        gc.save();
        gc.translate(xRightBorder, yBottomBorder);
        this.renderer.drawCorner(gc, dim.borderSize, BoardRenderer.Corner.BOTTOM_RIGHT, isFocused());
        gc.restore();

        // draw the squares
        for (Coordinate coordinate : Coordinate.values()) {

            double squareXOffset = dim.borderSize + dim.xOffset + (coordinate.columnIndex * dim.squareSize);
            double squareYOffset = dim.borderSize + (dim.yOffset + ((Coordinate.ROWS - 1) * dim.squareSize))
                    - (coordinate.rowIndex * dim.squareSize);

            gc.save();
            gc.translate(squareXOffset, squareYOffset);

            BoardRenderer.SquareIndicator squareIndicator;
            if (this.displayedPliesProperty.stream().anyMatch(m -> coordinate.equals(m.captures))) {
                squareIndicator = BoardRenderer.SquareIndicator.CAPTURED;
            } else if (this.displayedPliesProperty.stream().anyMatch(m -> coordinate.equals(m.target))) {
                squareIndicator = BoardRenderer.SquareIndicator.TARGET;
            } else {
                squareIndicator = null;
            }

            this.renderer.drawSquare(gc, dim.squareSize, coordinate, this.game.getPiece(coordinate), isFocused(),
                    coordinate.equals(this.focusedSquareProperty.get()),
                    coordinate.equals(this.selectedSquareProperty.get()),
                    squareIndicator);
            gc.restore();
        }
    }

    private void updateDisplayedPlies() {
        this.displayedPliesProperty.clear();
        Coordinate selected = this.selectedSquareProperty.get();
        if (selected != null) {
            List<Ply> plies = this.game.getValidPlies(selected);
            if (!plies.isEmpty()) this.displayedPliesProperty.addAll(plies);
        }
    }
}
