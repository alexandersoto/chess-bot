package chess.tests.internal;

import org.junit.Test;

import chess.board.ArrayBoard;


import static org.junit.Assert.*;

/**
 * This class tests the ArrayBoard provided to you.
 * 
 * You do not need to edit this file.  It is provided
 * in case you want to write your own board and use
 * similar tests on it.
 * 
 * FrontDesk will ignore any changes you make to this
 * file.
 */
public class ArrayBoardTest
{
	                                               // !!! WARNING !!!
	private static final int MAX_PERFT_DEPTH = 4;  // above 4 = long time
	private static final int MAX_SIG_DEPTH   = 3;  // above 3 = long time
	private static int       sigCollisions   = 0;
	
	/*
	 * You will have to enable promotion to other pieces for these tests
	 * to be correct.
	 * 
	 * You can do this by uncommenting the blocked out stuff in the
	 * initializer for ArrayPiece.PROMOTED_PIECES
	 */
	
	//@Test
	public void perftTest()
	{
	  checkAssertionsEnabled();
	  PerftTestUtil.perftAll(ArrayBoard.FACTORY,MAX_PERFT_DEPTH);
	}

	//@Test
	public void fenTest()
	{
		checkAssertionsEnabled();
		FenTestUtil.roundTripAll(ArrayBoard.FACTORY);
	}
	
	@Test
	public void signatureTest()
	{
		checkAssertionsEnabled();
		sigCollisions = SignatureTestUtil.checkSigsAll(ArrayBoard.FACTORY, MAX_SIG_DEPTH);
		System.out.println("Sig Collisions: " + sigCollisions);
	}
  
  //@Test
  public void legalMoveTest()
  {
	checkAssertionsEnabled();
    LegalMoveTestUtil.checkAll(ArrayBoard.FACTORY);
  }
  
	private void checkAssertionsEnabled()
	{	
		boolean enabled = true;
		assert enabled = true;
		assertTrue("This test should be run with assert statements enabled in the JVM",enabled);
	}
}
