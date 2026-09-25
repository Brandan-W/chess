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

        if (type == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        }

        if (type == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        }

        if (type == PieceType.KING) {
            return kingMoves(board, myPosition);
        }

        if (type == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
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
     * Calculates all valid queen moves.
     */
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
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

    /**
     * Calculates all valid knight moves.
     */
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] jumps = {
                {2, 1},
                {2, -1},
                {-2, 1},
                {-2, -1},
                {1, 2},
                {1, -2},
                {-1, 2},
                {-1, -2}
        };

        for (int[] jump : jumps) {
            int row = myPosition.getRow() + jump[0];
            int col = myPosition.getColumn() + jump[1];

            if (!isOnBoard(row, col)) {
                continue;
            }

            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece pieceAtPosition = board.getPiece(endPosition);

            if (pieceAtPosition == null
                    || pieceAtPosition.getTeamColor() != pieceColor) {

                moves.add(new ChessMove(myPosition, endPosition, null));
            }
        }

        return moves;
    }

    /**
     * Calculates all valid king moves.
     */
    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int[][] directions = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] direction : directions) {
            int row = myPosition.getRow() + direction[0];
            int col = myPosition.getColumn() + direction[1];

            if (!isOnBoard(row, col)) {
                continue;
            }

            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece pieceAtPosition = board.getPiece(endPosition);

            if (pieceAtPosition == null
                    || pieceAtPosition.getTeamColor() != pieceColor) {

                moves.add(new ChessMove(myPosition, endPosition, null));
            }
        }

        return moves;
    }

    /**
     * Calculates all valid king moves.
     */
    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        int direction;
        int startingRow;
        int promotionRow;

        if (pieceColor == ChessGame.TeamColor.WHITE) {
            direction = 1;
            startingRow = 2;
            promotionRow = 8;
        } else {
            direction = -1;
            startingRow = 7;
            promotionRow = 1;
        }

        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();

        // Move forward one square
        int oneStepRow = currentRow + direction;

        if (isOnBoard(oneStepRow, currentCol)) {
            ChessPosition oneStepPosition =
                    new ChessPosition(oneStepRow, currentCol);

            if (board.getPiece(oneStepPosition) == null) {

                addPawnMove(
                        moves,
                        myPosition,
                        oneStepPosition,
                        promotionRow
                );

                // Move forward two squares from starting row
                if (currentRow == startingRow) {
                    int twoStepRow = currentRow + (2 * direction);

                    ChessPosition twoStepPosition =
                            new ChessPosition(twoStepRow, currentCol);

                    if (board.getPiece(twoStepPosition) == null) {
                        moves.add(
                                new ChessMove(
                                        myPosition,
                                        twoStepPosition,
                                        null
                                )
                        );
                    }
                }
            }
        }

        // Capture diagonally left
        int captureLeftCol = currentCol - 1;
        int captureRow = currentRow + direction;

        if (isOnBoard(captureRow, captureLeftCol)) {
            ChessPosition capturePosition =
                    new ChessPosition(captureRow, captureLeftCol);

            ChessPiece pieceAtPosition =
                    board.getPiece(capturePosition);

            if (pieceAtPosition != null
                    && pieceAtPosition.getTeamColor() != pieceColor) {

                addPawnMove(
                        moves,
                        myPosition,
                        capturePosition,
                        promotionRow
                );
            }
        }

        // Capture diagonally right
        int captureRightCol = currentCol + 1;

        if (isOnBoard(captureRow, captureRightCol)) {
            ChessPosition capturePosition =
                    new ChessPosition(captureRow, captureRightCol);

            ChessPiece pieceAtPosition =
                    board.getPiece(capturePosition);

            if (pieceAtPosition != null
                    && pieceAtPosition.getTeamColor() != pieceColor) {

                addPawnMove(
                        moves,
                        myPosition,
                        capturePosition,
                        promotionRow
                );
            }
        }

        return moves;
    }

    /**
     * Adds a normal pawn move or all possible promotion moves.
     */
    private void addPawnMove(
            Collection<ChessMove> moves,
            ChessPosition startPosition,
            ChessPosition endPosition,
            int promotionRow) {

        if (endPosition.getRow() == promotionRow) {

            moves.add(new ChessMove(
                    startPosition,
                    endPosition,
                    PieceType.QUEEN
            ));

            moves.add(new ChessMove(
                    startPosition,
                    endPosition,
                    PieceType.ROOK
            ));

            moves.add(new ChessMove(
                    startPosition,
                    endPosition,
                    PieceType.BISHOP
            ));

            moves.add(new ChessMove(
                    startPosition,
                    endPosition,
                    PieceType.KNIGHT
            ));

        } else {
            moves.add(new ChessMove(
                    startPosition,
                    endPosition,
                    null
            ));
        }
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