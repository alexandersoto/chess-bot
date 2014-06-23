package chess.search;

import java.util.List;

import chess.board.Board;
import chess.board.Move;


/**
 * An implementation of Alpha Beta search.
 */
public class AlphaBetaFixedDepth
<
	M extends Move<M>,
	B extends Board<M,B>
>
	extends AbstractSearcher<M,B>
{
	
	public M getBestMove(B board, int myTime, int opTime) {

		// Need to do the first depth up here, so that we know which move to actually choose
		int infinity = evaluator.infty();
		int currentAlpha = -infinity;
		M bestMove = null;
		List<M> moves = board.generateMoves();
		
		for (M move : moves) {
			
			// Do negamax on it			
			board.applyMove(move);			
			int thisAlpha = -negamax(board, minDepth-1, -infinity, -currentAlpha);			
			board.undoMove();
			
			if (thisAlpha > currentAlpha) {
				bestMove = move;
				reportNewBestMove(bestMove);
				currentAlpha = thisAlpha;
			}
		}
		
		return bestMove;
	}
	
	/*
	 * This algorithm was adapted from the Wikipedia entry on negamax
	 */
	private int negamax(B board, int depth, int alpha, int beta) {
		if (depth == 0) {
			
			// Either we are at the desired depth, or there are no more moves
			return evaluator.eval(board);
		}
		else if (board.generateMoves().isEmpty()) {

			// No moves to make
			if (board.inCheck()) {
				return -evaluator.mate() - depth;
			}
			else {
				return -evaluator.stalemate();
			}
		}
		else {
			
			// Otherwise, we go through each move and do negamax on each		
			List<M> moves = board.generateMoves();
			for (M move : moves) {
											
				// Compute the new alpha value
				board.applyMove(move);
				alpha = Math.max(alpha, -negamax(board, depth-1, -beta, -alpha));				
				board.undoMove();
									
				// Do alpha-beta pruning
				if (alpha >= beta) {
					//break;
				}	
		
			}
			
			return alpha;
		}
	}
}