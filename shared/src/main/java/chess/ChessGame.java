package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean whiteLeftRookMoved;
    private boolean whiteRightRookMoved;
    private boolean blackLeftRookMoved;
    private boolean blackRightRookMoved;
    private ChessMove lastMove;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> pieceMoves =
                piece.pieceMoves(board, startPosition);

        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : pieceMoves) {

            ChessPiece capturedPiece =
                    board.getPiece(move.getEndPosition());

            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(startPosition, null);

            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }

            board.addPiece(startPosition, piece);
            board.addPiece(move.getEndPosition(), capturedPiece);
        }

        // Add castling moves for a king when castling is allowed
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            addCastlingMoves(piece, startPosition, validMoves);
        }

        // Add en passant move for a pawn when allowed
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            addEnPassantMove(piece, startPosition, validMoves);
        }

        return validMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        // There must be a piece at the starting position
        if (piece == null) {
            throw new InvalidMoveException();
        }

        // The piece must belong to the team whose turn it is
        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }

        // The requested move must be one of the piece's legal moves
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());

        if (!legalMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        // A castling move is a king moving two columns
        boolean isCastling =
                piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(move.getEndPosition().getColumn()
                - move.getStartPosition().getColumn()) == 2;

        // Remove the captured pawn when performing en passant
        removeEnPassantPawn(piece, move);

        // Handle pawn promotion
        if (move.getPromotionPiece() != null) {
            ChessPiece promotedPiece =
                    new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());

            board.addPiece(move.getEndPosition(), promotedPiece);
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }

        // Remove the piece from its old position
        board.addPiece(move.getStartPosition(), null);

        // Castling also moves the rook
        if (isCastling) {
            moveCastlingRook(move.getEndPosition());
        }

        // Remember if a king or original rook has moved
        recordPieceMoved(piece, move.getStartPosition());

        // Remember this move for special moves such as en passant
        lastMove = move;

        // Change to the other team's turn
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // Find this team's king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null
                        && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = position;
                }
            }
        }

        // Check whether an opposing piece can attack the king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null
                        && piece.getTeamColor() != teamColor
                        && pieceCanAttackKing(piece, position, kingPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean pieceCanAttackKing(
            ChessPiece piece,
            ChessPosition piecePosition,
            ChessPosition kingPosition) {

        Collection<ChessMove> moves =
                piece.pieceMoves(board, piecePosition);

        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    private boolean hasNoValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);

                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void recordPieceMoved(ChessPiece piece, ChessPosition startPosition) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            int row = startPosition.getRow();
            int col = startPosition.getColumn();

            if (piece.getTeamColor() == TeamColor.WHITE && row == 1) {
                if (col == 1) {
                    whiteLeftRookMoved = true;
                } else if (col == 8) {
                    whiteRightRookMoved = true;
                }
            }

            if (piece.getTeamColor() == TeamColor.BLACK && row == 8) {
                if (col == 1) {
                    blackLeftRookMoved = true;
                } else if (col == 8) {
                    blackRightRookMoved = true;
                }
            }
        }
    }

    private boolean lastMoveWasDoublePawnMove() {
        if (lastMove == null) {
            return false;
        }

        ChessPosition start = lastMove.getStartPosition();
        ChessPosition end = lastMove.getEndPosition();

        ChessPiece movedPiece = board.getPiece(end);

        return movedPiece != null
                && movedPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && Math.abs(end.getRow() - start.getRow()) == 2;
    }

    private void addCastlingMoves(
            ChessPiece king,
            ChessPosition kingPosition,
            Collection<ChessMove> validMoves) {

        TeamColor color = king.getTeamColor();

        int row;
        boolean kingMoved;
        boolean leftRookMoved;
        boolean rightRookMoved;

        if (color == TeamColor.WHITE) {
            row = 1;
            kingMoved = whiteKingMoved;
            leftRookMoved = whiteLeftRookMoved;
            rightRookMoved = whiteRightRookMoved;
        } else {
            row = 8;
            kingMoved = blackKingMoved;
            leftRookMoved = blackLeftRookMoved;
            rightRookMoved = blackRightRookMoved;
        }

        // King must still be on its original square and must never have moved.
        if (kingMoved
                || kingPosition.getRow() != row
                || kingPosition.getColumn() != 5) {
            return;
        }

        // A king cannot castle while currently in check.
        if (isInCheck(color)) {
            return;
        }

        // Queen side castle: king moves from column 5 to column 3.
        ChessPosition leftRookPosition = new ChessPosition(row, 1);
        ChessPiece leftRook = board.getPiece(leftRookPosition);

        if (!leftRookMoved
                && isCorrectRook(leftRook, color)
                && board.getPiece(new ChessPosition(row, 2)) == null
                && board.getPiece(new ChessPosition(row, 3)) == null
                && board.getPiece(new ChessPosition(row, 4)) == null
                && kingSafeOnSquare(color, kingPosition,
                        new ChessPosition(row, 4))
                && kingSafeOnSquare(color, kingPosition,
                        new ChessPosition(row, 3))) {

            validMoves.add(new ChessMove(
                    kingPosition,
                    new ChessPosition(row, 3),
                    null));
        }

        // King side castle: king moves from column 5 to column 7.
        ChessPosition rightRookPosition = new ChessPosition(row, 8);
        ChessPiece rightRook = board.getPiece(rightRookPosition);

        if (!rightRookMoved
                && isCorrectRook(rightRook, color)
                && board.getPiece(new ChessPosition(row, 6)) == null
                && board.getPiece(new ChessPosition(row, 7)) == null
                && kingSafeOnSquare(color, kingPosition,
                        new ChessPosition(row, 6))
                && kingSafeOnSquare(color, kingPosition,
                        new ChessPosition(row, 7))) {

            validMoves.add(new ChessMove(
                    kingPosition,
                    new ChessPosition(row, 7),
                    null));
        }
    }

    private boolean isCorrectRook(ChessPiece piece, TeamColor color) {
            return piece != null
                    && piece.getTeamColor() == color
                    && piece.getPieceType() == ChessPiece.PieceType.ROOK;
        }

        private boolean kingSafeOnSquare(
            TeamColor color,
            ChessPosition start,
            ChessPosition destination) {

        ChessPiece king = board.getPiece(start);
        ChessPiece capturedPiece = board.getPiece(destination);

        board.addPiece(destination, king);
        board.addPiece(start, null);

        boolean safe = !isInCheck(color);

        board.addPiece(start, king);
        board.addPiece(destination, capturedPiece);

        return safe;
    }

    private void moveCastlingRook(ChessPosition kingEndPosition) {
        int row = kingEndPosition.getRow();
        int kingColumn = kingEndPosition.getColumn();

        // Queen side castle
        if (kingColumn == 3) {
            ChessPosition rookStart = new ChessPosition(row, 1);
            ChessPosition rookEnd = new ChessPosition(row, 4);

            ChessPiece rook = board.getPiece(rookStart);

            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
        }

        // King side castle
        if (kingColumn == 7) {
            ChessPosition rookStart = new ChessPosition(row, 8);
            ChessPosition rookEnd = new ChessPosition(row, 6);

            ChessPiece rook = board.getPiece(rookStart);

            board.addPiece(rookEnd, rook);
            board.addPiece(rookStart, null);
        }
    }

    private void addEnPassantMove(
            ChessPiece pawn,
            ChessPosition pawnPosition,
            Collection<ChessMove> validMoves) {

        if (!lastMoveWasDoublePawnMove()) {
            return;
        }

        ChessPosition lastEnd = lastMove.getEndPosition();

        ChessPiece lastPawn = board.getPiece(lastEnd);

        // The pawn that just moved must belong to the other team
        if (lastPawn == null
                || lastPawn.getTeamColor() == pawn.getTeamColor()) {
            return;
        }

        // The two pawns must now be beside each other
        if (lastEnd.getRow() != pawnPosition.getRow()
                || Math.abs(lastEnd.getColumn()
                - pawnPosition.getColumn()) != 1) {
            return;
        }

        int direction;

        if (pawn.getTeamColor() == TeamColor.WHITE) {
            direction = 1;
        } else {
            direction = -1;
        }

        ChessPosition destination = new ChessPosition(
                pawnPosition.getRow() + direction,
                lastEnd.getColumn());

        validMoves.add(new ChessMove(
                pawnPosition,
                destination,
                null));
    }

    private void removeEnPassantPawn(
            ChessPiece piece,
            ChessMove move) {

        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return;
        }

        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        // En passant is a diagonal pawn move into an empty square
        if (start.getColumn() == end.getColumn()
                || board.getPiece(end) != null) {
            return;
        }

        ChessPosition capturedPawnPosition =
                new ChessPosition(start.getRow(), end.getColumn());

        ChessPiece capturedPawn = board.getPiece(capturedPawnPosition);

        if (capturedPawn != null
                && capturedPawn.getPieceType() == ChessPiece.PieceType.PAWN
                && capturedPawn.getTeamColor() != piece.getTeamColor()) {

            board.addPiece(capturedPawnPosition, null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }

        return teamTurn == chessGame.teamTurn
                && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
