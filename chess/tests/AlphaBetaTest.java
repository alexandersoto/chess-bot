package chess.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.Locale;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.search.Negamax;
import chess.search.Searcher;
import chess.search.AlphaBetaFixedDepth;


public class AlphaBetaTest {

//	@Test (timeout = 1000)
	public void alphaBetaDepth2Test () {
		TestUtil.alphaBetaTest ("r1bq1b1r/pppkpppp/3p4/8/8/P2PP2P/1PP2PP1/RNB1KBNR b KQ -",2,
				new String[] {"e7e5"});
	}

//	@Test
	public void alphaBestDepth2Test2 () {
		TestUtil.alphaBetaTest ("rnbqk1n1/1pppb1p1/p6r/2N1PpBp/4P3/1P6/P1P1KPPP/R2Q1BNR b kq -",2,
				new String[] {"e7g5"});
	}

//	@Test
	public void alphaBestDepth2Test3 () {
		TestUtil.alphaBetaTest ("rnbqkbr1/pp1p1ppp/2p1p3/1N1n4/P3P3/5N1P/1PPPQPP1/R1B1KBR1 b KQ -",2,
				new String[] {"c6b5"});
	}

	// Compares two searchers
	//	@Test
	public void compareTest () {	
		Searcher<ArrayMove, ArrayBoard> ab = new AlphaBetaFixedDepth<ArrayMove, ArrayBoard>();
		Searcher<ArrayMove, ArrayBoard> id = new Negamax<ArrayMove, ArrayBoard>();
		
		ab.setFixedDepth(5);
		id.setFixedDepth(5);

		ArrayBoard board = ArrayBoard.FACTORY.create().init("rnbqkbr1/pp1p1ppp/2p1p3/1N1n4/P3P3/5N1P/1PPPQPP1/R1B1KBR1 b KQ -");

		ArrayMove moveAB = TestUtil.searcherOutput(ab, board, 3);
		ArrayMove moveID = TestUtil.searcherOutput(id, board, 3);
		TestUtil.compareMoves(board, moveAB, moveID);
	}

//	@Test
	public void checkMateTest() {
		Searcher<ArrayMove, ArrayBoard> noHash = new AlphaBetaFixedDepth<ArrayMove, ArrayBoard>();
//		Searcher<ArrayMove, ArrayBoard> noHash = new IDWithNoHash<ArrayMove, ArrayBoard>();
		//		Searcher<ArrayMove, ArrayBoard> hash = new IDwithAB<ArrayMove, ArrayBoard>();

		for(int i = 1; i < 5; i++) {
			noHash.setFixedDepth(i);
			ArrayBoard board = ArrayBoard.FACTORY.create().init("1Q3Q1Q/3k4/8/2QN4/2P5/8/P7/3K2R1 w - -");
			ArrayMove move = TestUtil.searcherOutput(noHash, board, i);
			
			System.out.println("Best move in tester is: " + move);
			

//			board.applyMove(move);
//			System.out.println(board);
//			board.undoMove();
		}
	}

	// Just testing the print statements out of the evaluator
//	@Test
	public void endGameTest() {
		String game = "1k1q4/5p2/8/8/8/8/P5P1/2QK4 w - -";
		Searcher<ArrayMove, ArrayBoard> noHash = new Negamax<ArrayMove, ArrayBoard>();
		
		ArrayBoard board = ArrayBoard.FACTORY.create().init(game);
		TestUtil.searcherOutput(noHash, board, 1);		
		System.out.println(board);
	}

	
	// Current speed order (depth 7)
	// IDWithHash2 ~1500ms
	// IDHashDoubleBest ~4000ms
	// IDWithAB ~5000ms
	// NegaScout ~8000ms
	
	@Test
	public void speedTest () {
		int depth = 6;
		int trials = 10;
		Searcher<ArrayMove, ArrayBoard> searcher1 = new Negamax<ArrayMove, ArrayBoard>();
		Searcher<ArrayMove, ArrayBoard> searcher2 = new Negamax<ArrayMove, ArrayBoard>();

		ArrayBoard board1 = ArrayBoard.FACTORY.create().init("rnbqkbr1/pp1p1ppp/2p1p3/1N1n4/P3P3/5N1P/1PPPQPP1/R1B1KBR1 b KQ -");
		ArrayBoard board2 = ArrayBoard.FACTORY.create().init("rnbqkbr1/pp1p1ppp/2p1p3/1N1n4/P3P3/5N1P/1PPPQPP1/R1B1KBR1 b KQ -");
		
		double searcher1Time, searcher2Time;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		for(int i = 0; i < trials; i++) {
			double startTime = bean.getCurrentThreadCpuTime() / 1000000000.0;;
			ArrayMove move1 = TestUtil.searcherOutput(searcher1, board1, depth);
			double endTime = bean.getCurrentThreadCpuTime() / 1000000000.0;;
			searcher1Time = endTime - startTime;			
			
			startTime = bean.getCurrentThreadCpuTime() / 1000000000.0;;
			//ArrayMove move2 = TestUtil.searcherOutput(searcher2, board2, depth);
			TestUtil.searcherOutput(searcher2, board2, depth);
			endTime = bean.getCurrentThreadCpuTime() / 1000000000.0;;	
			searcher2Time = endTime - startTime;
						
			System.out.println("*******************************************************");
			if (searcher1Time < searcher2Time) {
				double delta = searcher2Time - searcher1Time;
				double percentDecrease = delta / searcher2Time * 100;
				System.out.println("** Searcher1 is " + 
									NumberFormat.getNumberInstance(Locale.US).format(delta) + "s (" + 
									NumberFormat.getNumberInstance(Locale.US).format(percentDecrease) + "%) faster");
			}
			else {
				double delta = searcher1Time - searcher2Time;
				double percentDecrease = delta / searcher1Time * 100;
				System.out.println("** Searcher2 is " + 
									NumberFormat.getNumberInstance(Locale.US).format(delta) + "s (" + 
									NumberFormat.getNumberInstance(Locale.US).format(percentDecrease) + "%) faster");
			}
			System.out.println("** Searcher1 took " + NumberFormat.getNumberInstance(Locale.US).format(searcher1Time) + "s");
			System.out.println("** Searcher2 took " + NumberFormat.getNumberInstance(Locale.US).format(searcher2Time) + "s");
			System.out.println("*******************************************************");

			assertTrue(board1.equals(board2));
			//TestUtil.compareMoves(board1, move1, move2);
			board1.applyMove(move1);
			board2.applyMove(move1);			
		}
	}
}