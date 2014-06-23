package chess.search;

import java.util.HashMap;
import java.util.Map;

import chess.board.Board;

@SuppressWarnings("rawtypes")
public class BoardCount {

	// Store the information about repetitions
	private Map<Long, Count> map;
	
	public BoardCount() {
		map = new HashMap<Long, Count>();
	}
	
	public int increment(Board board) {
		Count count = map.get(board.signature());
		if(count != null) {
			count.increment();	
		}
		else {
			count = new Count();
			map.put(board.signature(), count);
		}		
		return count.getValue();
	}
	
	public int decrement(Board board) {
		Count count = map.get(board.signature());
		if(count != null) {
			count.decrement();	
		}
		else {
			count = new Count();
			count.decrement();
			map.put(board.signature(), count);
		}		
		return count.getValue();
	}
	
	public boolean isRepetition(Board board) {
		if (map.containsKey(board.signature())) {
			return map.get(board.signature()).getValue() > 0;
		}
		return false;
	}
	
	
	// Seen a board 3 times, it's a draw
	public boolean isDraw(Board board) {
		if (map.containsKey(board.signature())) {
			return map.get(board.signature()).getValue() >= 3;
		}
		
		return false;
	}	
}