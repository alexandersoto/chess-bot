package chess.engine;

import java.util.Observer;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.board.Board;
import chess.evaluation.Evaluator;
import chess.evaluation.SimpleEvaluator;
import chess.evaluation.AdvancedEvaluator;
import chess.search.AdvancedTimer;
import chess.search.Negamax;
import chess.search.Searcher;


/**
 * Chess Bot
 * A new Engine is created each time you start a game.
 */
public class Engine {	
	private ArrayBoard board;
	private Searcher <ArrayMove,ArrayBoard> searcher; 
	private Evaluator<ArrayBoard> eval;

	private int plyCount = 0;
	private int minDepth, maxDepth;	
	private int difficulty;

	public static final int EASY = 0;
	public static final int MEDIUM = 1;
	public static final int HARD = 2;
	public static final int ULTRA = 3;

	public String getName()	{
		String difficultyDescription = "";

		switch (difficulty) {
		case EASY:
			difficultyDescription = "Easy";
			break;
		case MEDIUM:			
			difficultyDescription = "Medium";
			break;
		case HARD:			
			difficultyDescription = "Hard";
			break;
		case ULTRA:			
			difficultyDescription = "Ultra Hard";
			break;		
		}

		return "Bot " + difficultyDescription;
	}

	public Engine(int time, int inc, int difficulty) {

		// Start with a fresh board
		board = ArrayBoard.FACTORY.create().init(Board.STARTING_POSITION);
		this.difficulty = difficulty;

		switch (difficulty) {
		case EASY:			
			minDepth = 2;
			maxDepth = 3;
			eval = new SimpleEvaluator();
			searcher = new Negamax<ArrayMove,ArrayBoard>();			
			break;
		case MEDIUM:
			minDepth = 3;
			maxDepth = 4;
			eval = new SimpleEvaluator();
			searcher = new Negamax<ArrayMove,ArrayBoard>();			
			break;
		case HARD:
			minDepth = 4;
			maxDepth = 6;
			eval = new AdvancedEvaluator();			
			searcher = new Negamax<ArrayMove,ArrayBoard>();			
			break;			
		case ULTRA:
			minDepth = 5;
			maxDepth = 20;
			eval = new AdvancedEvaluator();			
			searcher = new Negamax<ArrayMove,ArrayBoard>();				
			break;
		}

		searcher.setMinDepth(minDepth);
		searcher.setMaxDepth(maxDepth);
		searcher.setEvaluator(eval);
		searcher.setTimer(new AdvancedTimer(time, inc));
	}

	/**
	 * Applies move to the current board.
	 * 
	 * @param m the move
	 */
	public void applyMove(ArrayMove m) {
		if( board.plyCount() != plyCount++ ) {
			throw new IllegalStateException("Did you forget to call undoMove() somewhere?");
		}
		board.applyMove(m);
	}

	/**
	 * Return the player's board state
	 */
	public ArrayBoard getBoard() {
		return board;
	}

	/**
	 * Compute and return a move in the current position.
	 * 
	 * @param myTime number of seconds left on the player's clock
	 * @param opTime number of seconds left on the opponent's clock
	 */
	public ArrayMove computeMove(int myTime, int opTime) {
		return searcher.getBestMove(board, myTime, opTime);
	}

	/**
	 * Adds an Observer to the Searcher so that when a new best move
	 * is found, the Observer will be notified. 
	 * @param o the new Observer
	 */
	public void addBestMoveObserver(Observer o) {
		searcher.addBestMoveObserver(o);
	}
}