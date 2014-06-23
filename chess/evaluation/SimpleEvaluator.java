package chess.evaluation;

import chess.board.ArrayBoard;
import chess.board.ArrayPiece;
import chess.util.Iteratorable;

public class SimpleEvaluator implements Evaluator<ArrayBoard> {
	
	private static final int INFINITY  = 1000000;
	private static final int MATE      = 300000;
	private static final int STALEMATE = 0;
	
	/* Material value of a piece */
	private static final int kingval      = 350;
	private static final int queenval     = 900;
	private static final int rookval      = 500;
	private static final int bishopval    = 300;
	private static final int knightval    = 300;
	private static final int pawnval      = 100;
	
	/* The bonus for castling */
	private static final int CASTLE_BONUS = 10;
	
	/* Player color */
	private static final int WHITE = ArrayBoard.WHITE;
	private static final int BLACK = ArrayBoard.BLACK;
	
	/*
	 * Piece value tables modify the value of each piece according to where it
	 * is on the board.
	 * 
	 * To orient these tables, each row of 8 represents one row (rank) of the
	 * chessboard.
	 * 
	 * !!! The first row is where white's pieces start !!!
	 * 
	 * So, for example
	 * having a pawn at d2 is worth -5 for white. Having it at d7 is worth
	 * 20. Note that these have to be flipped over to evaluate black's pawns
	 * since pawn values are not symmetric.
	 */
	private static int bishoppos[][] =
	{
	 {-5, -5, -5, -5, -5, -5, -5, -5},
     {-5, 10,  5,  8,  8,  5, 10, -5},
     {-5,  5,  3,  8,  8,  3,  5, -5},
     {-5,  3, 10,  3,  3, 10,  3, -5},
     {-5,  3, 10,  3,  3, 10,  3, -5},
     {-5,  5,  3,  8,  8,  3,  5, -5},
     {-5, 10,  5,  8,  8,  5, 10, -5},
     {-5, -5, -5, -5, -5, -5, -5, -5}
	};
	private static int knightpos[][] =
	{
	 {-10, -5, -5, -5, -5, -5, -5,-10},
	 { -8,  0,  0,  3,  3,  0,  0, -8},
	 { -8,  0, 10,  8,  8, 10,  0, -8},
	 { -8,  0,  8, 10, 10,  8,  0, -8},
	 { -8,  0,  8, 10, 10,  8,  0, -8},
	 { -8,  0, 10,  8,  8, 10,  0, -8},
	 { -8,  0,  0,  3,  3,  0,  0, -8},
	 {-10, -5, -5, -5, -5, -5, -5,-10}
	};
	
	private static int pawnposWhite[][] =
	{
	 {0,  0,  0,  0,  0,  0,  0,  0},
     {0,  0,  0, -5, -5,  0,  0,  0},
     {0,  2,  3,  4,  4,  3,  2,  0},
     {0,  4,  6, 10, 10,  6,  4,  0},
     {0,  6,  9, 10, 10,  9,  6,  0},
     {4,  8, 12, 16, 16, 12,  8,  4},
     {5, 10, 15, 20, 20, 15, 10,  5},
     {0,  0,  0,  0,  0,  0,  0,  0}
	};
	
	// I created this, should be just a flipped
	// version of the white array
	private static int pawnposBlack[][] =
	{
	 {0,  0,  0,  0,  0,  0,  0,  0},
	 {5, 10, 15, 20, 20, 15, 10,  5},
	 {4,  8, 12, 16, 16, 12,  8,  4},
	 {0,  6,  9, 10, 10,  9,  6,  0},
	 {0,  4,  6, 10, 10,  6,  4,  0},
	 {0,  2,  3,  4,  4,  3,  2,  0},
	 {0,  0,  0, -5, -5,  0,  0,  0},
	 {0,  0,  0,  0,  0,  0,  0,  0}
	 };
		

	/*
	 * This is the evaluator. It simply returns a score for the board position
	 * with respect to the player to move. It must function precisely as
	 * described here in order to pass the unit tests. [If you want to use a
	 * different evaluation function for a more advanced version of your
	 * program, you can do that, but the eval() method here must function as
	 * described.]
	 * 
	 * The evaluation function gives a score for each piece according to the
	 * pieceValue array below, and an additional amount for each piece depending
	 * on where it is (see comment below). A bonus of 10 points should be given
	 * if the current player has castled (and -10 for the opponent castling)
	 * 
	 * The eval of a position is the value of the pieces of the player whose
	 * turn it is, minus the value of the pieces of the other player (plus the
	 * castling points thrown in).
	 * 
	 * If it's WHITE's turn, and white is up a queen, then the value will be
	 * roughly 900. If it's BLACK's turn and white is up a queen, then the value
	 * returned should be about -900.
	 */

	public int eval(ArrayBoard board) {
		// Get current player
		int player = board.toPlay();
		int playerValue = 0;
		
		// Opponent is opposite of player
		int opponent = (player == WHITE) ? BLACK : WHITE;		
		int opponentValue = 0;
	
		Iteratorable<ArrayPiece> pieces = board.allPiecesOfColor(player);
		playerValue = getValueOfPieces(pieces, player);
		
		pieces = board.allPiecesOfColor(opponent);
		opponentValue = getValueOfPieces(pieces, opponent);

		// Updates score based on castling
		if(board.hasCastled[player]) {
			playerValue += CASTLE_BONUS;
		}
		if(board.hasCastled[opponent]) {
			opponentValue += CASTLE_BONUS;
		}
				
		// Return the difference between our current score and opponents
		return playerValue - opponentValue;
	}	

	// Perhaps can speed this up. We can multiply number of given piece * value of that piece (?)
	private int getValueOfPieces(Iteratorable<ArrayPiece> pieces, int player) {
		
		// Determine pawn array to use
		int[][] pawnpos = (player == WHITE) ? pawnposWhite : pawnposBlack;
		
		int value = 0;
		for(ArrayPiece piece : pieces) {
			switch(piece.type()) {
				case ArrayPiece.PAWN:
					value += pawnval + pawnpos  [piece.row()][piece.col()];
					break;				
				case ArrayPiece.BISHOP:
					value += bishopval + bishoppos[piece.row()][piece.col()];
					break;				
				case ArrayPiece.KNIGHT:
					value += knightval + knightpos[piece.row()][piece.col()];
					break;
				case ArrayPiece.ROOK:
					value += rookval;                             
					break;
				case ArrayPiece.QUEEN:
					value += queenval;
					break;
				case ArrayPiece.KING:
					value += kingval;	
					break;
			}
		}
		return value;
	}
	
	// Accessors
	public int infty() {
		return INFINITY;
	}

	public int mate() {
		return MATE;
	}
	
	public int stalemate() {
	  return STALEMATE;
	}

	public int weightOfPawn() {
		return pawnval;
	}
}
