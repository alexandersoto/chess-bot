package chess.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.evaluation.Evaluator;
import chess.evaluation.SimpleEvaluator;
import chess.search.Searcher;
import chess.search.SimpleTimer;
import chess.search.AlphaBetaFixedDepth;


public class TestUtil {
	private static final Evaluator<ArrayBoard> stu_evaluator = new SimpleEvaluator();
	
	public static void evaluatorTest(String fen, int value) {
		ArrayBoard student = ArrayBoard.FACTORY.create().init(fen);
		assertEquals(
			"Evaluation of boards not equal (" + fen + ")",
			value,
			stu_evaluator.eval(student)
		);
	}

	public static void alphaBetaTest(String fen, int depth, String[] validMoves) {
		ArrayBoard student = ArrayBoard.FACTORY.create().init(fen);
		AlphaBetaFixedDepth<ArrayMove, ArrayBoard> ab = new AlphaBetaFixedDepth<ArrayMove, ArrayBoard>();
		ab.setEvaluator(stu_evaluator);
		ab.setFixedDepth(depth);
		
		String studMove = ab.getBestMove(student, 10000, 10000).serverString().substring(0,4);

		List<String> validMoveList = Arrays.asList(validMoves);

		assertTrue(student.toString() + "\n\nMove returned by depth " + depth + " search on [" + fen + "] was " + studMove
				+ " but we expected something from " + validMoveList, validMoveList.contains(studMove));
	}

	// Returns the best move given a searcher
	public static ArrayMove searcherOutput(Searcher<ArrayMove, ArrayBoard> searcher, ArrayBoard board, int depth) {
		searcher.setEvaluator(stu_evaluator);
		searcher.setFixedDepth(depth);
		searcher.setTimer(new SimpleTimer(20000000, 5000000));		
		return searcher.getBestMove(board, 1000000000, 10000);
	}
	
	// Makes sure two separate moves get the same evaluation value!
	public static void compareMoves(ArrayBoard board, ArrayMove move1, ArrayMove move2) {

		board.applyMove(move1);
		int score1 = stu_evaluator.eval(board);
		board.undoMove();
		
		board.applyMove(move2);
		int score2 = stu_evaluator.eval(board);
		board.undoMove();
		
		//System.out.println("Score 1: " + score1 + " Score 2: " + score2);
		assertEquals(score1, score2);
	}
}