package chess.search;

import chess.board.Move;

/**
 * 
 * This class holds information about a move. It holds top two moves based on
 * depth. This assists in move ordering, as we are able to try two good moves
 * each time we get a hash hit
 * 
 */
public class BoardInfo<M extends Move<M>> {

	// Stores if we are a low, exact or upper bound
	public static final int LOWER = 0;
	public static final int EXACT = 1;
	public static final int UPPER = 2;

	private int value;
	private M bestMove;
	private M secondBestMove;
	private int type;
	private int depth;
	private int secondMoveDepth;

	public BoardInfo(int value, M move, int type, int depth) {
		this.value = value;
		this.bestMove = move;
		this.type = type;
		this.depth = depth;
		this.secondMoveDepth = -1;
		this.secondBestMove = null;		
	}

	// Accessors
	public int getValue() {
		return value;
	}

	public M getSecondBestMove() {
		return secondBestMove;
	}

	public M getBestMove() {
		return bestMove;
	}

	public int getDepth() {
		return depth;
	}

	public int getType() {
		return type;
	}

	// Replace when we have a better value (bigger depth)
	// Only replace the second best move if it's different from
	// the best move.
	public void updateInfo(int value, M move, int type, int depth) {
		
		// Replacing a better value!
		if(depth > this.depth) {
			this.value = value;
			this.type = type;
			
			// Only replace if it's unique			
			if (!move.equals(this.bestMove)) {
				this.secondBestMove = this.bestMove;
				this.secondMoveDepth = this.depth;
			}
			
			this.bestMove = move;
			this.depth = depth;			
		}
		else if (this.secondBestMove == null || depth >= this.secondMoveDepth) {
			if (!move.equals(this.bestMove)) {
				this.secondBestMove = move;
				this.secondMoveDepth = depth;
			}
		}
	}
}