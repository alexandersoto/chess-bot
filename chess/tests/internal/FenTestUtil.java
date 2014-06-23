package chess.tests.internal;

import chess.board.Board;

import static org.junit.Assert.*;

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
public class FenTestUtil
{
	public static
	<
		B extends Board<?,B>
	>
	void roundTripAll(B b)
	{
		roundTripFens(b,PerftTestUtil.database.keySet().toArray(new String[0]));
	}
	
	public static
	<
		B extends Board<?,B>
	>
	void roundTripFens(B b, String... fens)
	{
		for(String fen : fens)
			roundTripFen(b, fen);
	}
	
	public static
	<
		B extends Board<?,B>
	>
	void roundTripFen(B b, String fen)
	{
		assertEquals(fen, b.init(fen).fen());
	}
}
