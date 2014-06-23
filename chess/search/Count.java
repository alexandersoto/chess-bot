package chess.search;

// Stores a count of a move to be used in a hashMap
public class Count {	
	private int count;
	
	public Count() {
		count = 1;
	}
	
	// Accessors
	public int getValue() {
		return count;
	}

	// Mutators
	public void increment() {
		count++;
	}
		
	public void decrement() {
		count--;
	}
}