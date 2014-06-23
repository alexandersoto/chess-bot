package chess.search;

import java.util.List;

import chess.board.Board;
import chess.board.Move;


/**
 * An extremely basic Searcher that is used soley to count the nodes
 * visited in PerftTests.  You may want to look at this class as a
 * template for starting NegaMax.
 */
public class DFS
<
	M extends Move<M>,
	B extends Board<M,B>
>
	extends AbstractSearcher<M,B>
{
	public M getBestMove(B board, int myTime, int opTime)
	{
		nodeCount = 0;
		leafCount = 0;
		dfs(board, maxDepth);
		return null; // DFS doesn't look for a best move
	}

	private int dfs(B board, int pliesToGo)
	{
    ++nodeCount;
	  
		if (pliesToGo == 0)
		{
		  ++leafCount;
			return evaluator.eval(board);
		}

		List<M> moves = board.generateMoves();

		if (moves.isEmpty())
		{
			if (board.inCheck())
			{
				return evaluator.mate();
			}
			else
			{
				return evaluator.stalemate();
			}
		}
		
		for (M move : moves)
		{
			board.applyMove(move);
			 	dfs(board, pliesToGo - 1);
			board.undoMove();
		}

	  //we don't know what the value of the best move is in DFS
		return 0;
  }
}
