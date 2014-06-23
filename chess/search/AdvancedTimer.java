package chess.search;

public class AdvancedTimer implements Timer {

	private int  increment;
	private long startTime;
	private int  allocated;
	private long endGameTime;
	private boolean noTimeup;
	private int moveNumber;
	private int timeRemaining;
	
	// When we're super low on time, force timeout so we don't autolose
	private static final int FORCE_TIMEOUT_TIME = 2000;
	
	// When we're running low on time, let searcher know to hurry up
	private static final int HURRY_UP_TIME = 10000;

	// > 85% of matches finish in this many moves. From:
	// http://facta.junis.ni.ac.rs/acar/acar200901/acar2009-07.pdf
	private static final int TOTAL_MOVES = 60;

	public AdvancedTimer(int initialTime, int increment) {
		this.increment = increment;
		moveNumber = 0;
	}

	public void start(int myTime, int opTime) {
		timeRemaining = myTime;
		startTime = System.currentTimeMillis();
		allocated = allocateTime();
		endGameTime = startTime + myTime;
		noTimeup = true;
		moveNumber++;
	}

	public boolean timeup() {

		// Force it to timeup when we're almost out of time
		if((endGameTime - System.currentTimeMillis()) <= FORCE_TIMEOUT_TIME) {
			return true;
		}
		else if (noTimeup) {
			return false;
		}
		else if ((System.currentTimeMillis() - startTime) > allocated) {
			return true;
		}
		return false;
	}

	// Custom allocate time function
	// Based on http://www.open-aurec.com/wbforum/viewtopic.php?f=4&t=53060
	// https://chessprogramming.wikispaces.com/Time+Management
	private int allocateTime() {
		int movesLeft = TOTAL_MOVES - moveNumber;
		
		if (movesLeft < 5) {
			movesLeft = 5;
		}
		
		int target = timeRemaining / movesLeft;
		
		// Add most of the increment, but leave some as cushion
		double smallerIncrement = 0.9 * increment;
		
		// Bias time at the start of the match
		return (int) (1.4 * target + smallerIncrement);
	}
	
	public boolean hurryUp() {
		return timeRemaining < HURRY_UP_TIME;
	}

	public void notOkToTimeup() {
		noTimeup = true;
	}

	public void okToTimeup() {
		noTimeup = false;
	}
}