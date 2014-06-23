package chess.tests;

import static chess.tests.TestUtil.evaluatorTest;

import org.junit.Test;

import chess.board.ArrayBoard;
import chess.board.Board;
import chess.evaluation.AdvancedEvaluator;
import chess.evaluation.Evaluator;

public class EvaluatorTest {

	@Test (timeout = 1000)
	public void evaluatorTest0 () {
		evaluatorTest ("rn1k1bnr/p1q1p1p1/1pp2p2/P2p3p/2PP2b1/5PQ1/1P2P1PP/RNB1KBNR w KQ -",-7);
	}
	
	// Move Pawn e2e4
	@Test (timeout = 1000)
	public void evaluatorTest1 () {
		ArrayBoard board = ArrayBoard.FACTORY.create().init(Board.STARTING_POSITION);
		board.applyMove(board.createMoveFromString("e2e4"));
		System.out.println(board);
		evaluatorTest (board.fen(), -15);
	}

	// Move Knight b1a3
	@Test (timeout = 1000)
	public void evaluatorTest2 () {
		ArrayBoard board = ArrayBoard.FACTORY.create().init(Board.STARTING_POSITION);
		board.applyMove(board.createMoveFromString("b1a3"));
		System.out.println(board);
		evaluatorTest (board.fen(), 3);
	}
	
	@Test
	public void evaluatorTest3 () {
		ArrayBoard board = ArrayBoard.FACTORY.create().init(Board.STARTING_POSITION);
		System.out.println(board);
		Evaluator<ArrayBoard> theEvaluator = new AdvancedEvaluator();
		System.out.println("Evaluation: " + theEvaluator.eval(board));
	}

}
