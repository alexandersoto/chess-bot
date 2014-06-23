package chess.search;

public interface Timer
{
	/**
	 * Starts the timer and allocates the appropriate amount of time.
	 * 
	 * @param myTime amount of time left on your game clock.
	 * @param opTime amount of time left on your opponent's game clock.
	 */
	public void    start  (int myTime, int opTime);
	
	/**
	 * @return true if the search should end because the allocated time
	 * has been used up.
	 */
	public boolean timeup ();
	
	/*
	 * These method deactivate and activate the timer. This is useful, for
	 * example, if you wanted to make sure that at least a full 1-ply search is
	 * completed without interruption, you'd set it to notOk for that search.
	 */
	/**
	 * Deactivates the timer and will make timeup() always return fales.
	 */
	public void notOkToTimeup();
	
	/**
	 * Reactivates the timer and will make timeup() function normally.
	 */
	public void okToTimeup   ();
	
	public boolean hurryUp();
}
