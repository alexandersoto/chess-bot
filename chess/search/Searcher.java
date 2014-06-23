package chess.search;

import java.util.Observer;

import chess.board.Board;
import chess.board.Move;
import chess.evaluation.Evaluator;


public interface Searcher
<
	M extends Move<M>,
	B extends Board<M,B>
>
{
	/**
	 * Searches the game tree and selects the best move based
	 * on some hueristics and algorithms.
	 * 
	 * @param board the current board position.
	 * @param myTime the remaining time on your clock.
	 * @param opTime the remaining time on your opponent's clock.
	 * @return the best move from this current position.
	 */
	public M    getBestMove   (B board, int myTime, int opTime);
	
	/**
	 * Sets the minimum depth to search.
	 * 
	 * @param depth the min depth.
	 */
	public void setMinDepth   (int depth);
	
	/**
	 * Sets the maximum depth to search.
	 * 
	 * @param depth the max depth.
	 */
	public void setMaxDepth   (int depth);
	
	/**
	 * Sets both the minimum and maximum depth to search.
	 * 
	 * @param depth the min and max depth.
	 */
	public void setFixedDepth (int depth);
	
	/**
	 * Tells this Searcher to use a new timer.
	 * 
	 * @param t the new timer.
	 */
	public void setTimer      (Timer t);
	
	/**
	 * Tells this Searcher to use a new evaluator.
	 * 
	 * @param e the new evaluator.
	 */
	public void setEvaluator  (Evaluator<B> e);
	
	/**
	 * @return The number of nodes visited in the last search.
	 */
	public long nodeCount     ();
	/**
	 * @return The number of leaf nodes visited in the last search.
	 */
	public long leafCount     ();
	
	/**
	 * Add a new Observer to the list of Observers to notify
	 * when the Searcher's state changes (such as when a new best
	 * move is found during the search).  This is typically done
	 * by extending the Observable class and using its addObserver,
	 * setChanged, and notifyObservers methods.
	 * @param o the new Observer
	 */
	public void addBestMoveObserver(Observer o);
}
