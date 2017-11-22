package name.ulbricht.chess.pgn;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SANMove {

    static final String ROOK = "R";
    static final String KNIGHT = "N";
    static final String BISHOP = "B";
    static final String QUEEN = "Q";
    static final String KING = "K";

    private static final String PIECE = "[" + ROOK + KNIGHT + BISHOP + QUEEN + KING + "]";
    private static final String COLUMN = "[a-h]";
    private static final String ROW = "[1-8]";
    private static final String CAPTURE = "([x:])?";
    private static final String PROMOTION = "(=(" + PIECE + "))?";
    private static final String CHECK = "(\\+)?";

    private static final String SOURCE = "(" + COLUMN + ")?(" + ROW + ")?";
    private static final String DESTINATION = "(" + COLUMN + ")(" + ROW + ")";

    private static final Pattern SIMPLE = Pattern.compile("^(" + PIECE + ")?" + SOURCE + CAPTURE + DESTINATION + PROMOTION + CHECK + "$");
    private static final Pattern KINGSIDE_CASTLING = Pattern.compile("^(0-0|O-O)" + CHECK + "$");
    private static final Pattern QUEENSIDE_CASTLING = Pattern.compile("^(0-0-0|O-O-O)" + CHECK + "$");

    public enum Type {
        DEFAULT,
        KINGSIDE_CASTLING,
        QUEENSIDE_CASTLING
    }

    public static SANMove of(String s) {
        Matcher matcher = KINGSIDE_CASTLING.matcher(s);
        if (matcher.matches()) {
            boolean check = matcher.group(2) != null;
            return new SANMove(Type.KINGSIDE_CASTLING, KING, null, null,
                    false, null, null, check);
        }

        matcher = QUEENSIDE_CASTLING.matcher(s);
        if (matcher.matches()) {
            boolean check = matcher.group(2) != null;
            return new SANMove(Type.QUEENSIDE_CASTLING, KING, null, null,
                    false, null, null, check);
        }

        matcher = SIMPLE.matcher(s);
        if (matcher.matches()) {
            final int PIECE_GROUP = 1;
            final int FROM_COLUMN_GROUP = 2;
            final int FROM_ROW_GROUP = 3;
            final int CAPTURE_GROUP = 4;
            final int TO_COLUMN_GROUP = 5;
            final int TO_ROW_GROUP = 6;
            final int PROMOTION_GROUP = 8;
            final int CHECK_GROUP = 9;

            String piece = matcher.group(PIECE_GROUP);
            String fromColumn = matcher.group(FROM_COLUMN_GROUP);
            String fromRow = matcher.group(FROM_ROW_GROUP);
            boolean capture = matcher.group(CAPTURE_GROUP) != null;
            String to = matcher.group(TO_COLUMN_GROUP) + matcher.group(TO_ROW_GROUP);
            String promotion = matcher.group(PROMOTION_GROUP);
            boolean check = matcher.group(CHECK_GROUP) != null;

            return new SANMove(Type.DEFAULT, piece, fromColumn, fromRow, capture, to, promotion, check);
        }

        throw new IllegalArgumentException("Cannot parse SAN move " + s);
    }

    private final Type type;
    private final String piece;
    private final String fromColumn;
    private final String fromRow;
    private final boolean capture;
    private final String to;
    private final String promotion;
    private final boolean check;

    private SANMove(Type type, String piece, String fromColumn, String fromRow, boolean capture,
                    String to, String promotion, boolean check) {
        this.type = Objects.requireNonNull(type);

        if (type == Type.KINGSIDE_CASTLING || type == Type.QUEENSIDE_CASTLING) {
            if (!piece.equals(KING))
                throw new IllegalArgumentException(String.format("Move type %s cannot used with piece type %s",
                        type, piece));
            if (to != null || fromColumn != null || fromRow != null)
                throw new IllegalArgumentException(String.format("Move type %s does not support coordinates",
                        type));
            if (capture)
                throw new IllegalArgumentException(String.format("Move type %s does not support capture",
                        type));
        }

        if (promotion != null && piece != null) {
            throw new IllegalArgumentException("Only pawns can be promoted.");
        }
        if (promotion!=null && promotion.equals(KING)) {
            throw new IllegalArgumentException("Pawns cannot be promoted to " + promotion);
        }

        this.piece = piece;
        this.fromColumn = fromColumn;
        this.fromRow = fromRow;
        this.capture = capture;
        this.to = to;
        this.promotion = promotion;
        this.check = check;
    }

    public Type getType() {
        return this.type;
    }

    public String getPiece() {
        return piece;
    }

    public String getFromColumn() {
        return this.fromColumn;
    }

    public String getFromRow() {
        return this.fromRow;
    }

    public boolean isCapture() {
        return this.capture;
    }

    public String getTo() {
        return this.to;
    }

    public String getPromotion() {
        return this.promotion;
    }

    public boolean isCheck() {
        return this.check;
    }
}
