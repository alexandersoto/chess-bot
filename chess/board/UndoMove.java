package chess.board;

import chess.util.Creatable;

/**
 * Stores enough information about the state of the board and
 * the move being applied to undo that move later.
 * 
 * This class exists for the sole purpose of speeding up the
 * search.  We don't want to be copying the entire board
 * each time we make a move.
 * 
 * @author Owen Durni (opd@andrew.cmu.edu)
 */
public class UndoMove implements Creatable<UndoMove>
{
	public static final UndoMove FACTORY = new UndoMove();
	
	public ArrayMove  move;
	public int        enpassantSquare;
	public boolean[]  canCastleKingside  = new boolean[2];
	public boolean[]  canCastleQueenside = new boolean[2];
	public boolean[]  hasCastled         = new boolean[2];
	public ArrayPiece enpassantCapture;
	
	public UndoMove()
	{
		//don't initialize, probably just going to overwrite
	}
	
	public UndoMove init(ArrayBoard currentBoard, ArrayMove move)
	{
		this.move               = move.copy();
		this.enpassantSquare    = currentBoard.enpassantSquare;
		
		System.arraycopy(currentBoard.canCastleKingside, 0, this.canCastleKingside, 0, currentBoard.canCastleKingside.length);
		System.arraycopy(currentBoard.canCastleQueenside, 0, this.canCastleQueenside, 0, currentBoard.canCastleQueenside.length);
		System.arraycopy(currentBoard.hasCastled, 0, this.hasCastled, 0, currentBoard.hasCastled.length);
		this.enpassantCapture   = null;
		
		return this;
	}
	
	public UndoMove copy()
	{
		UndoMove copy = create();
		
		copy.move               = move.copy();
		copy.enpassantSquare    = enpassantSquare;
		
		System.arraycopy(canCastleKingside, 0, copy.canCastleKingside, 0, canCastleKingside.length);
		System.arraycopy(canCastleQueenside, 0, copy.canCastleQueenside, 0, canCastleQueenside.length);
		System.arraycopy(hasCastled, 0, copy.hasCastled, 0, hasCastled.length);
		
		copy.enpassantCapture   = (enpassantCapture == null ? null : enpassantCapture.copy());
		
		return copy;
	}
	
	public UndoMove create()
	{
        return new UndoMove();
	}
}
