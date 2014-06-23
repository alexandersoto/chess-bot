package chess.evaluation;

import chess.board.Board;

public interface Evaluator
<
    B extends Board<?,B>
>
{
	/**
	 * @return the constant value that is used to represent mate
	 * for this evaluator.
	 */
	public int    mate     ();
  /**
   * @return the constant value that is used to represent stalemate
   * for this evaluator.
   */
  public int    stalemate();
	/**
	 * @return the constant value that is used to represent infinity
	 * for this evaluator.
	 */
	public int    infty    ();
	
	/**
	 * Evaluates a board, assigning it a score based on how much the
	 * current player to play is winning.
	 * 
	 * @param board the board to evaluate.
	 * 
	 * @return the value of the specified board.
	 */
	public int    eval     (B board);
	
	/**
	 * This method is here soley for debugging purposes.
	 * Typically used to see which evaluator you are using
	 * if your program changes based on the state of the game.
	 * 
	 * @return a String representation of this evaluator's state.
	 */
	public String toString ();
	
	/**
	 * This is so we can query evaluators for our aspiration window sizes
	 * @return the value of a pawn
	 */
	public int weightOfPawn();
}
