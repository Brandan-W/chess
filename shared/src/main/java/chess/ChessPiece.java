package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (type == PieceType.ROOK) {
            return rookMoves(board, myPosition);
        }

        if (type == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        }

        throw new RuntimeException("Not implemented");
    }

    /**
     * Calculates all valid rook moves.
     */
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        return slidingMoves(board, myPosition, directions);
    }

    /**
     * Calculates all valid bishop moves.
     */
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        return slidingMoves(board, myPosition, directions);
    }

    /**
     * Calculates movement for pieces that move repeatedly
     * in a straight direction until blocked.
     */
    private Collection<ChessMove> slidingMoves(
            ChessBoard board,
            ChessPosition myPosition,
            int[][] directions) {

        Collection<ChessMove> moves = new ArrayList<>();

        for (int[] direction : directions) {
            int row = myPosition.getRow() + direction[0];
            int col = myPosition.getColumn() + direction[1];

            while (isOnBoard(row, col)) {
                ChessPosition endPosition = new ChessPosition(row, col);
                ChessPiece pieceAtPosition = board.getPiece(endPosition);

                if (pieceAtPosition == null) {
                    moves.add(new ChessMove(myPosition, endPosition, null));
                } else {
                    if (pieceAtPosition.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition, endPosition, null));
                    }

                    break;
                }

                row += direction[0];
                col += direction[1];
            }
        }

        return moves;
    }

    /**
     * Checks whether a row and column are inside the chess board.
     */
    private boolean isOnBoard(int row, int col) {
        return row >= 1 && row <= 8
                && col >= 1 && col <= 8;
    }
















    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece that)){
            return false;
        }

        return pieceColor == that.pieceColor && type ==  that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return pieceColor + " " + type;
    }
}
