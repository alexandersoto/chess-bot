package chess.tests.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chess.board.Board;
import chess.board.Move;


/**
 * This class aids in testing the Board interface.
 * 
 * You do not need to edit this file.  It is provided
 * in case you want to write your own board and use
 * similar tests on it.
 * 
 * FrontDesk will ignore any changes you make to this
 * file.
 */
public class SignatureTestUtil
{	
	public static
	<
		M extends Move<M>,
		B extends Board<M,B>
	>
	int checkSigsAll(B b, int depth)
	{
		return checkSigs(b,depth,PerftTestUtil.database.keySet().toArray(new String[0]));
	}
	
	public static
	<
		M extends Move<M>,
		B extends Board<M,B>
	>
	int checkSigs(B b, int depth, String... fens)
	{
		ArrayList<B> seen = new ArrayList<B>();
		Map<Long,B>  sigs = new HashMap<Long,B>();
		
		int collisions = 0;
		
		for(String fen : fens)
		{
		  List<M> moves     = b.generatePseudoMoves();
		  
		  if( moves.size() == 0 )
		  {
		    continue;
		  }
		  
			collisions += checkSig(moves.get(0),b.create().init(fen),depth,seen,sigs);
		}
		
		return collisions;
	}
	
	private static
	<
		M extends Move<M>,
		B extends Board<M,B>
	>
	int checkSig(M m, B b, int depth, List<B> seen, Map<Long,B> sigs)
	{
		if(depth == 0)
			return 0;
		
		int  collisions = 0;
		long origSig    = b.signature();
		B    mapBoard   = sigs.get(origSig);
		
		if( mapBoard != null && !mapBoard.equals(b) )
		{
			++collisions;
		}
		else
			sigs.put(origSig, b.copy());
		
		int index = seen.indexOf(b);
		if(index == -1)
			seen.add(b.copy());
		else
			assertEquals(b.signature(), seen.get(index).signature());
		
		for(M move : b.generateMoves())
		{
			b.applyMove(move);
				collisions += checkSig(m,b,depth-1,seen,sigs);
			b.undoMove();
			
			assertEquals(origSig, b.signature());
		}
		
		return collisions;
	}
}
