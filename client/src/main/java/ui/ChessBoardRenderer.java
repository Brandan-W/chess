package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessBoardRenderer {

    private static final String RESET = "\u001B[0m";

    // Background colors
    private static final String LIGHT = "\u001B[47m";
    private static final String DARK = "\u001B[100m";

    // Foreground colors
    private static final String WHITE_PIECE = "\u001B[97m";
    private static final String BLACK_PIECE = "\u001B[30m";

    public void drawBoard(
            ChessBoard board,
            ChessGame.TeamColor perspective) {

        boolean whitePerspective =
                perspective == ChessGame.TeamColor.WHITE;

        System.out.println();

        printFiles(whitePerspective);

        for (int displayRow = 0; displayRow < 8; displayRow++) {

            int row = whitePerspective
                    ? 8 - displayRow
                    : displayRow + 1;

            System.out.print(" " + row + " ");

            for (int displayCol = 0; displayCol < 8; displayCol++) {

                int col = whitePerspective
                        ? displayCol + 1
                        : 8 - displayCol;

                ChessPiece piece = board.getPiece(
                        new ChessPosition(row, col));

                // a8 and h1 must be light squares
                boolean lightSquare = (row + col) % 2 != 0;

                String background = lightSquare
                        ? LIGHT : DARK;

                String foreground = piece != null
                        && piece.getTeamColor()
                        == ChessGame.TeamColor.WHITE
                        ? WHITE_PIECE : BLACK_PIECE;

                System.out.print(
                        background
                                + foreground
                                + " "
                                + pieceSymbol(piece)
                                + " "
                                + RESET
                );
            }

            System.out.println(" " + row);
        }

        printFiles(whitePerspective);
        System.out.println();
    }

    private void printFiles(boolean whitePerspective) {

        System.out.print("    ");

        for (int i = 0; i < 8; i++) {

            char file = (char) (
                    'a' + (whitePerspective ? i : 7 - i));

            System.out.print(" " + file + "  ");
        }

        System.out.println();
    }

    private String pieceSymbol(ChessPiece piece) {

        if (piece == null) {
            return " ";
        }

        boolean white =
                piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        return switch (piece.getPieceType()) {
            case KING -> white ? "♔" : "♚";
            case QUEEN -> white ? "♕" : "♛";
            case ROOK -> white ? "♖" : "♜";
            case BISHOP -> white ? "♗" : "♝";
            case KNIGHT -> white ? "♘" : "♞";
            case PAWN -> white ? "♙" : "♟";
        };
    }
}
