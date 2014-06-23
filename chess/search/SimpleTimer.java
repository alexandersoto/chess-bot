package chess.search;

public class SimpleTimer implements Timer {

	private int  increment;
	private long startTime;
	private int  allocated;
	private long endGameTime;
	private boolean noTimeup = false;

	public SimpleTimer(int initialTime, int increment) {
		this.increment = increment;
	}

	public void start(int myTime, int opTime) {
		startTime = System.currentTimeMillis();
		allocated = myAllocateTime(myTime, opTime);
		endGameTime = startTime + myTime;
	}

	public boolean timeup() {
		
		// Force it to timeup when time gets < 5 seconds left
		if((endGameTime - System.currentTimeMillis()) <= 5000) {
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
	private int myAllocateTime(int timeLeft, int opTimeLeft) {

		final int ASAP_TIME     = 30000;
		final int HURRY_UP_TIME = 45000;

		// Give ourselves the ability to regain time
		// Allocate just 1/3 of the time given each turn
		if(timeLeft <= ASAP_TIME) {			
			return (int) increment / 3;
		}

		// Reduce the time given
		else if(timeLeft <= HURRY_UP_TIME) {
			return (int) 2 * increment;
		}

		// Reduce time slightly since opponent has more time
		else if(timeLeft <= opTimeLeft) {
			return (int) 4 * increment;
		}

		// Take it easy, we're ahead of our opponents
		else {
			return 5 * increment;
		}
	}

	public void notOkToTimeup() {
		noTimeup = true;
	}

	public void okToTimeup() {
		noTimeup = false;
	}
	
	public boolean hurryUp() {
		return false;
	}
}