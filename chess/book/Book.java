package chess.book;

import chess.board.Board;
import chess.board.Move;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Polyglot opening book support
 *
 * @author rui
 * 
 * Borrowed from: https://github.com/albertoruibal/carballo
 */
public class Book <M extends Move<M>, B extends Board<M,B>>{

	private String bookName;

	List<M> moves = new ArrayList<M>();
	List<Short> weights = new ArrayList<Short>();
	long totalWeight;

	private final Random random = new Random();

	public Book() {
		bookName = "book.bin";
	}

	/**
	 * "move" is a bit field with the following meaning (bit 0 is the least significant bit)
	 * <p/>
	 * bits                meaning
	 * ===================================
	 * 0,1,2               to file
	 * 3,4,5               to row
	 * 6,7,8               from file
	 * 9,10,11             from row
	 * 12,13,14            promotion piece
	 * "promotion piece" is encoded as follows
	 * none       0
	 * knight     1
	 * bishop     2
	 * rook       3
	 * queen      4
	 *
	 * @param move
	 * @return
	 */
	private String int2MoveString(short move) {
		StringBuilder sb = new StringBuilder();
		sb.append((char) ('a' + ((move >> 6) & 0x7)));
		sb.append(((move >> 9) & 0x7) + 1);
		sb.append((char) ('a' + (move & 0x7)));
		sb.append(((move >> 3) & 0x7) + 1);
		if (((move >> 12) & 0x7) != 0) sb.append("NBRQ".charAt(((move >> 12) & 0x7) - 1));
		return sb.toString();
	}
	
	public void generateMoves(B board) {
		totalWeight = 0;
		moves.clear();
		weights.clear();

		long key2Find = Fen2Polyglot.getKey(board.fen());	
		
		try {
			InputStream bookIs = getClass().getResourceAsStream(bookName);
			DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(bookIs));

			long key;
			short moveInt;
			short weight;
			while (true) {
				key = dataInputStream.readLong();

				if (key == key2Find) {
					moveInt = dataInputStream.readShort();
					weight = dataInputStream.readShort();
					dataInputStream.readInt(); // Unused learn field

					M move = board.createMoveFromString(int2MoveString(moveInt));
					
					// Add only if it is legal
					if (board.isLegalMove(move)) {
						moves.add(move);
						weights.add(weight);
						totalWeight += weight;
					}
				} 				
				else {
					dataInputStream.skipBytes(8);
				}
			}
		} catch (Exception ignored) {
		}
	}

	/**
	 * Gets a random move from the book taking care of weights
	 */
	public M getMove(B board) {
		generateMoves(board);
		long randomWeight = (new Float(random.nextFloat() * totalWeight)).longValue();
		for (int i = 0; i < moves.size(); i++) {
			randomWeight -= weights.get(i);
			if (randomWeight <= 0) {
				return moves.get(i);
			}
		}
		return null;
	}
}