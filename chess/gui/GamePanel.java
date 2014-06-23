package chess.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import chess.board.ArrayMove;
import chess.board.ArrayPiece;
import chess.engine.Engine;
import chess.search.BoardCount;

public final class GamePanel extends JPanel {
	private static final long serialVersionUID = -6731471373081937898L;
	private Board board;	
	private ChessClock lowerClock, upperClock;
	private JLabel lowerName, upperName;
	private JComboBox<String> whitePlayerDropdown, blackPlayerDropdown;
	private JComboBox<Integer> timeDropdown, incrementDropdown;	
	private boolean lowersTurn;
	private int human;
	private int whiteDifficulty, blackDifficulty;
	private transient Engine lowerPlayer, upperPlayer;
	private int msPerPlayer, msPerIncrement;
	private char cPromote;

	// Store the information about repetitions for draw detection
	private BoardCount boardCount;
	private int noCaptureOrPawnCount;

	// Thread for chess searcher so GUI can update
	private SwingWorker<ArrayMove, ArrayMove> engineWorker;

	private static final int HUMAN_NONE  = 2;
	private static final int HUMAN_WHITE = 3;
	private static final int HUMAN_BLACK = 4;	
	private static final String BEST_MOVE = "bestMove";


	public GamePanel() {
		board = new Board(this);

		lowerClock = new ChessClock();
		upperClock = new ChessClock();

		// Make labels
		lowerName = new JLabel(" ");
		upperName = new JLabel(" ");
		JLabel whitePlayerLabel = new JLabel("White player:");
		JLabel blackPlayerLabel = new JLabel("Black player:");
		JLabel timeLabel = new JLabel("Minutes per player:");
		JLabel incrementLabel = new JLabel("Seconds per increment:");
		

		// Set fonts
		Font f = new Font("Helvetica", Font.PLAIN, 18);
		lowerName.setFont(f);
		upperName.setFont(f);

		f = new Font("Helvetica", Font.BOLD, 14);		
		whitePlayerLabel.setFont(f);
		blackPlayerLabel.setFont(f);
		timeLabel.setFont(f);
		incrementLabel.setFont(f);

		f = new Font("Helvetica", Font.BOLD, 26);
		lowerClock.setFont(f);
		upperClock.setFont(f);

		// Create combo boxes for difficulties
		String[] playerStrings = { "You", "Easy Bot", "Medium Bot", "Hard Bot", "Ultra Bot" };
		whitePlayerDropdown = new JComboBox<String>(playerStrings);
		whitePlayerDropdown.setSelectedIndex(0);
		blackPlayerDropdown = new JComboBox<String>(playerStrings);
		blackPlayerDropdown.setSelectedIndex(2);
		
		// Create combo boxes for time options
		Integer[] minutesPerPlayer = {2, 5, 10, 20, 30, 60, 90, 120};
		Integer[] incrementPerPlayer = {0, 2, 5, 10, 20, 30, 60, 120, 240};
		timeDropdown = new JComboBox<Integer>(minutesPerPlayer);
		timeDropdown.setSelectedIndex(2);
		incrementDropdown = new JComboBox<Integer>(incrementPerPlayer);
		incrementDropdown.setSelectedIndex(3);
		
		// Create new game button
		JButton buttonNewGame = new JButton("Start New Game");
		buttonNewGame.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {

				// Ask for confirmation before starting a new game
				if (gameOverResult() == null) {
					int restart = JOptionPane.showConfirmDialog(null,
							"Do you want to stop the current game and start a new one?",
							"Start New Game?", JOptionPane.YES_NO_OPTION);
					if (restart != JOptionPane.YES_OPTION) {
						return;
					}
				}
				endGame();
				start();
			}
		});

		// Draw the panel
		this.setBackground(Config.colorGame);		
		this.setLayout(new GamePanelLayout(getSize().width, getSize().height));		

		// Add components to the panel
		this.add("Board", board);
		this.add("UpperClock", upperClock);
		this.add("LowerClock", lowerClock);
		this.add("UpperName", upperName);		
		this.add("LowerName", lowerName);
		this.add("WhitePlayerLabel", whitePlayerLabel);
		this.add("BlackPlayerLabel", blackPlayerLabel);
		this.add("WhitePlayerDropdown", whitePlayerDropdown);
		this.add("BlackPlayerDropdown", blackPlayerDropdown);
		this.add("TimeLabel", timeLabel);
		this.add("TimeDropdown", timeDropdown);
		this.add("IncrementLabel", incrementLabel);
		this.add("IncrementDropdown", incrementDropdown);		
		this.add("ButtonNewGame", buttonNewGame);		

		this.setPreferredSize(new Dimension (500, 500));
	}

	// Read the game options the user selects
	public void initGameSettings() {
		cPromote = 'q';

		msPerPlayer = (Integer) timeDropdown.getSelectedItem() * 60 * 1000;
		msPerIncrement = (Integer) incrementDropdown.getSelectedItem() * 1000;

		int whitePlayerIndex = whitePlayerDropdown.getSelectedIndex();
		int blackPlayerIndex = blackPlayerDropdown.getSelectedIndex();

		// 2 people is not supported, so let the user know and make black a bot
		if (whitePlayerIndex == 0 && blackPlayerIndex == 0) {
			JOptionPane.showMessageDialog(null, "2 players is not supported.");
			blackPlayerDropdown.setSelectedIndex(1);
		}
		
		// Set if a person is playing or not		
		if (whitePlayerIndex == 0) {
			human = HUMAN_WHITE;			
		}
		else if (blackPlayerIndex == 0) {
			human = HUMAN_BLACK;
		}
		else {
			human = HUMAN_NONE;
		}

		// Set the engine difficulties
		whiteDifficulty = Engine.EASY;
		blackDifficulty = Engine.EASY;						

		// Case # corresponds to the indices of the playerStrings
		// array in the constructor
		// XXX - not ideal, but works for now
		switch(whitePlayerIndex) {
		case 1:
			whiteDifficulty = Engine.EASY;
			break;
		case 2:
			whiteDifficulty = Engine.MEDIUM;
			break;
		case 3:
			whiteDifficulty = Engine.HARD;
			break;
		case 4:
			whiteDifficulty = Engine.ULTRA;
			break;
		}

		switch(blackPlayerIndex) {
		case 1:
			blackDifficulty = Engine.EASY;
			break;
		case 2:
			blackDifficulty = Engine.MEDIUM;
			break;
		case 3:
			blackDifficulty = Engine.HARD;
			break;
		case 4:
			blackDifficulty = Engine.ULTRA;
			break;
		}
	}

	// Start a match
	public void start() {
		initGameSettings();

		String lowerPlayerName;			
		String upperPlayerName;

		// A human is always the lower player.
		// If it's two bots then white is the lower player.	
		if (human == HUMAN_WHITE) {
			lowerPlayer = null;
			upperPlayer = new Engine(msPerPlayer, msPerIncrement, blackDifficulty);		
			lowerPlayerName = "You";
			upperPlayerName = upperPlayer.getName();
		}
		else if (human == HUMAN_BLACK) {
			lowerPlayer = null;
			upperPlayer = new Engine(msPerPlayer, msPerIncrement, whiteDifficulty);
			lowerPlayerName = "You";
			upperPlayerName = upperPlayer.getName();
		}
		else {
			lowerPlayer = new Engine(msPerPlayer, msPerIncrement, whiteDifficulty);
			upperPlayer = new Engine(msPerPlayer, msPerIncrement, blackDifficulty);		
			lowerPlayerName = lowerPlayer.getName();
			upperPlayerName = upperPlayer.getName();
		}

		lowerName.setText(lowerPlayerName);
		upperName.setText(upperPlayerName);	

		// Set clocks
		lowerClock.setClock(msPerPlayer);
		lowerClock.startClock();		
		upperClock.setClock(msPerPlayer);
		upperClock.stopClock();

		// Set initial game state
		lowersTurn = true;
		boardCount = new BoardCount();
		noCaptureOrPawnCount = 0;

		// Set up the board
		board.setHumanPlaying(human != HUMAN_NONE);
		board.newGame();
		board.startGame();	

		// Paint everything
		board.repaint();
		lowerClock.repaint();
		upperClock.repaint();
		repaint();

		// White always goes first, so if a person is playing as black,
		// flip the board and the white engine computes a move
		if (human == HUMAN_BLACK) {
			board.setFlipped(true);
			lowersTurn = false;
			computeMove(upperPlayer, upperClock.msecleft, lowerClock.msecleft);
		}
		if (human == HUMAN_NONE) {
			computeMove(lowerPlayer, lowerClock.msecleft, upperClock.msecleft);			
		}
	}

	// Pawn promotion
	public void setPromote(char c) {
		cPromote = c;
	}

	public char getPromote() {
		return cPromote;
	}

	public void move(ArrayMove move) {
		boardCount.increment(board);

		if (move.isCapture() || move.isEnpassant() || (move.source.type() == ArrayPiece.PAWN)) {
			noCaptureOrPawnCount = 0;
		}
		else {
			noCaptureOrPawnCount++;
		}

		// Make sure move is legal
		if (!board.isLegalMove(move)) {
			throw new IllegalStateException("Attempted to call move() with an illegal move.");			
		}

		// Make your move on all the boards
		board.applyMove(move);
		upperPlayer.applyMove(move);		
		if (lowerPlayer != null) {
			lowerPlayer.applyMove(move);
		}

		// Check if game is over
		String result = gameOverResult();
		if(result != null) {
			endGame();
			showResult(result);
			return;
		}

		// Determine who goes next
		ChessClock playerJustMovedClock;
		ChessClock playerToMoveClock;
		Engine playerToMove;				
		if (lowersTurn) {

			// Lower player just went		
			playerJustMovedClock = lowerClock;
			playerToMoveClock = upperClock;
			playerToMove = upperPlayer;
		}
		else {

			// Upper Player just went
			playerJustMovedClock = upperClock;
			playerToMoveClock = lowerClock;
			playerToMove = lowerPlayer;						
		}

		// Switch turns
		lowersTurn = !lowersTurn;

		// Player just made a move, stop the clock and add the increment
		playerJustMovedClock.stopClock();
		playerJustMovedClock.setClock(playerJustMovedClock.msecleft + msPerIncrement);

		// Next player goes
		playerToMoveClock.startClock();
		computeMove(playerToMove, playerToMoveClock.msecleft, playerJustMovedClock.msecleft);

		board.repaint();
	}

	/**
	 * Checks if the game is over and returns the result of the match.
	 * Returns null if game is still in progress.
	 */
	private String gameOverResult() {

        // White is always lower unless a person is playing as black
		String lowerName = "White";
		String upperName = "Black";		
		if (human == HUMAN_BLACK) {
			lowerName = "Black";
			upperName = "White";
		}
		
		// Test for out of time
		if (lowersTurn && lowerClock.msecleft <= 0) {
			return lowerName + " ran out of time. " +
					upperName + " wins!";
		}

		if (!lowersTurn && upperClock.msecleft <= 0) {
			return upperName + " ran out of time. " +
					lowerName + " wins!";
		}		

		// No more moves, determine if it's a check mate or stale mate
		if(board.generateMoves().size() == 0 ) {
			if(board.inCheck()) {
				if (lowersTurn) {
					return "Checkmate. " + lowerName + " wins!";
				}
				else {
					return "Checkmate. " + upperName + " wins!";
				}
			}
			else {
				return "Stalemate!";				
			}
		}

		// Check for draws
		// http://en.wikipedia.org/wiki/Draw_(chess)
		if(boardCount.isDraw(board)) {			
			return "Draw by repetition.";
		}

		// http://en.wikipedia.org/wiki/Fifty-move_rule
		if(noCaptureOrPawnCount >= 100) {
			return "Draw by fifty move rule.";
		}

		return null;
	}

	private void endGame() {
		if (engineWorker != null) {
			engineWorker.cancel(true);
		}

		lowerClock.stopClock();
		upperClock.stopClock();

		board.endGame();
		board.repaint();
	}

	private void showResult(String result) {
		JOptionPane.showMessageDialog(null, "Game Over: " + result);
	}

	// Engines calculate the next move
	private void computeMove(Engine engine, long myTime, long opTime) {

		// Only create a new worker when we have an engine, we've never made
		// a worker before, or the existing worker is already completed
		if (engine != null && (engineWorker == null || engineWorker.isDone())) {
			engineWorker = new EngineWorker(engine, (int) myTime, (int) opTime);
			engineWorker.execute();

			// React to events that the engine sends out. This updates
			// the best move, and also knows when the search is complete
			engineWorker.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {

					// If we get a new best move, draw it on the board
					if(evt.getPropertyName().equals(BEST_MOVE)) {
						ArrayMove move = (ArrayMove) evt.getNewValue();
						String moveString = (move == null) ? null : move.smithString();						
						board.setBestMove(moveString);
						board.repaint();					
					}

					// If the computation is done, make the move
					if(evt.getPropertyName().equals("state") &&
							evt.getNewValue().equals(StateValue.DONE)) {
						try {
							if (engineWorker.isDone() && !engineWorker.isCancelled()) {
								ArrayMove computedMove = engineWorker.get();
								move(computedMove);
							}
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
					}
				}
			});
		}
	}

	// Thread that chess searcher will be run on
	private class EngineWorker extends SwingWorker<ArrayMove, ArrayMove> implements Observer {
		private Engine engine;
		private int myTime;
		private int opTime;
		private ArrayMove bestMove;

		public EngineWorker(Engine engine, int myTime, int opTime) {
			this.engine = engine;			
			this.myTime = myTime;
			this.opTime = opTime;
			this.bestMove = null;

			// Allows us to update the best moves the engine gives us as it runs
			engine.addBestMoveObserver(this);
		}

		protected ArrayMove doInBackground() {
			return engine.computeMove(myTime, opTime);		
		}

		@SuppressWarnings("deprecation")
		public void update(Observable o, Object arg) {

			// This lets us interrupt the thread, and ignore best moves that
			// are no longer valid
			// https://blogs.oracle.com/swinger/entry/swingworker_stop_that_train
			if(Thread.currentThread().isInterrupted()) {
				firePropertyChange(BEST_MOVE, bestMove, null);
				bestMove = null;

				// This is deprecated, but there's no good alternative to ending
				// the thread. By this point, a new game has started and this thread is useless
				// http://stackoverflow.com/questions/16504140/thread-stop-deprecated
				Thread.currentThread().stop();
			}

			if(arg instanceof ArrayMove) {
				ArrayMove newBestMove = (ArrayMove) arg;
				firePropertyChange(BEST_MOVE, bestMove, newBestMove);	
				bestMove = newBestMove;					
			}
		}
	}
}
