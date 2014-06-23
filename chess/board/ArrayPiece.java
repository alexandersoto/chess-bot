package chess.board;

import chess.util.Creatable;
import chess.util.Predicate;
import static chess.board.ArrayBoard.*;

/**
 * Pieces are represented by three things
 *  - color  (white or black)
 *  - type   (king,queen,rook,knight,bishop, or pawn)
 *  - square (square occupied on the board)
 *  
 * The color and type of a piece are compressed into a 4-bit word called a piece
 *  - The highest bit is the color of the piece
 *  - The lower 3 bits are the type of piece
 *  
 *  @author Owen Durni (opd@andrew.cmu.edu)
 */
public class ArrayPiece implements Creatable<ArrayPiece>
{
    public static final ArrayPiece FACTORY  = new ArrayPiece();
    
    public int              square          = REMOVED;
    public int              piece           = REMOVED;
    
    public static final int REMOVED         = -42;
    public static final int EMPTY           = 0;
    public static final int PAWN            = 1;
    public static final int KNIGHT          = 2;
    public static final int KING            = 3;
    public static final int BISHOP          = 5;
    public static final int ROOK            = 6;
    public static final int QUEEN           = 7;

    public static final int TYPE_MASK       = 7;
    
    public static final int WHITE_PAWN      = makePieceCode(WHITE,PAWN);
    public static final int WHITE_KNIGHT    = makePieceCode(WHITE,KNIGHT);
    public static final int WHITE_KING      = makePieceCode(WHITE,KING);
    public static final int WHITE_BISHOP    = makePieceCode(WHITE,BISHOP);
    public static final int WHITE_ROOK      = makePieceCode(WHITE,ROOK);
    public static final int WHITE_QUEEN     = makePieceCode(WHITE,QUEEN);
    
    public static final int BLACK_PAWN      = makePieceCode(BLACK,PAWN);
    public static final int BLACK_KNIGHT    = makePieceCode(BLACK,KNIGHT);
    public static final int BLACK_KING      = makePieceCode(BLACK,KING);
    public static final int BLACK_BISHOP    = makePieceCode(BLACK,BISHOP);
    public static final int BLACK_ROOK      = makePieceCode(BLACK,ROOK);
    public static final int BLACK_QUEEN     = makePieceCode(BLACK,QUEEN);

    public static final String PIECE_STRING = "-pnk_brq-PNK_BRQ";
    public static final int[]  PIECE_TYPES = {
        KNIGHT,
        BISHOP,
        ROOK,
        QUEEN,
        KING,
        PAWN
    };
    public static final int[] WHITE_PIECES;
    public static final int[] BLACK_PIECES;
    static {
        int[] ws = new int[PIECE_TYPES.length];
        int[] bs = new int[PIECE_TYPES.length];
        for(int i = 0; i < PIECE_TYPES.length; ++i)
        {
            ws[i] = makePieceCode(WHITE,PIECE_TYPES[i]);
            bs[i] = makePieceCode(BLACK,PIECE_TYPES[i]);
        }
        
        WHITE_PIECES = ws;
        BLACK_PIECES = bs;
    }
    public static final int[][] PIECES_OF_COLOR = new int[2][];
    static {
        PIECES_OF_COLOR[WHITE] = WHITE_PIECES;
        PIECES_OF_COLOR[BLACK] = BLACK_PIECES;
    }
    public static final int[][] PROMOTED_PIECES = new int[2][];
    static {
        int[] ws = {WHITE_QUEEN, WHITE_KNIGHT, WHITE_ROOK, WHITE_BISHOP};
        int[] bs = {BLACK_QUEEN, BLACK_KNIGHT, BLACK_ROOK, BLACK_BISHOP};
        
        PROMOTED_PIECES[WHITE] = ws;
        PROMOTED_PIECES[BLACK] = bs;
    }
    public static final int[]   ALL_PIECES = merge(
        WHITE_PIECES,
        BLACK_PIECES
    );
    
    @SuppressWarnings("unchecked")
    public static final Predicate<ArrayPiece>[] P_IS_COLOR        = new Predicate[2];
    static {
    	P_IS_COLOR[WHITE] = new Predicate<ArrayPiece> () {
    		public boolean check(ArrayPiece t) { return (t.isOccupied() && t.color() == WHITE); }
    	};
    	P_IS_COLOR[BLACK] = new Predicate<ArrayPiece> () {
    		public boolean check(ArrayPiece t) { return (t.isOccupied() && t.color() == BLACK); }
    	};
    }
    @SuppressWarnings("unchecked")
    public static final Predicate<ArrayPiece>[] P_IS_NOT_COLOR    = new Predicate[2];
    static {
    	P_IS_NOT_COLOR[WHITE] = new Predicate<ArrayPiece> () {
    		public boolean check(ArrayPiece t) { return t.isEmpty() || (t.isOccupied() && t.color() != WHITE); }
    	};
    	P_IS_NOT_COLOR[BLACK] = new Predicate<ArrayPiece> () {
    		public boolean check(ArrayPiece t) { return t.isEmpty() || (t.isOccupied() && t.color() != BLACK); }
    	};
    }
    public static final Predicate<ArrayPiece>   P_IS_LINE_SLIDING = new Predicate<ArrayPiece> () {
		public boolean check(ArrayPiece t) { return t.isOccupied() && t.isSliding() && t.isLineSliding(); }
    };
    public static final Predicate<ArrayPiece>   P_IS_DIAG_SLIDING = new Predicate<ArrayPiece> () {
		public boolean check(ArrayPiece t) { return t.isOccupied() && t.isSliding() && t.isDiagonalSliding(); }
    };
    
    private ArrayPiece()
    {}
    
    public ArrayPiece create()
    {
        return new ArrayPiece();
    }

    public ArrayPiece copy()
    {
        return create().init(this.piece, this.square);
    }
    
    public ArrayPiece init(int piece, int square)
    {
        this.piece  = piece;
        this.square = square;
        
        return this;
    }
    
    public ArrayPiece init(char type, int square)
    {
        return init(PIECE_STRING.indexOf(type), square);
    }
    
    /**
     * @return the color of this piece.
     */
    public int color()
    {
        return colorOfPiece(this.piece);
    }
    
    /**
     * @return the type of this piece.
     */
    public int type()
    {
        return typeOfPiece(this.piece);
    }
    
    /**
     * @return the row of this piece.
     */
    public int row()
    {
    	return rowOfSquare(square);
    }
    
    /**
     * @return the col of this piece.
     */
    public int col()
    {
    	return colOfSquare(square);
    }
    
    /**
     * @return true iff this piece is empty.
     */
    public boolean isEmpty()
    {
        return ((piece & TYPE_MASK) == EMPTY);
    }
    
    /**
     * @return true iff this piece is not empty.
     */
    public boolean isOccupied()
    {
        return ((piece & TYPE_MASK) != EMPTY);
    }
    
    /**
     * @return true if this is a sliding piece.
     */
    public boolean isSliding()
    {
        return ((piece & 4) != 0);
    }
    
    /**
     * @return Assuming this piece is a sliding piece, returns true iff
     * this piece can slide horizontally and vertically.
     */
    public boolean isLineSliding()
    {
        return ((piece & 2) != 0);
    }
    
    /**
     * @return Assuming this piece is a sliding piece, returns true iff
     * this piece can slide diagonally.
     */
    public boolean isDiagonalSliding()
    {
        return ((piece & 1) != 0);
    }
    
    private static final boolean MIGHT_ATTACK[][] = new boolean[16][];
    static {
    	for(int p = 0; p < 16; ++p)
    	{
    		ArrayPiece piece = new ArrayPiece();
    		boolean[] matt = new boolean[256];
	    	for(int source = 0; source <= UP_RIGHT_CORNER; ++source)
	    	{
	    		if(!onboard(source)) continue;
	    		
	    		for(int dest = 0; dest <= UP_RIGHT_CORNER; ++dest)
	    		{
	    			if(!onboard(dest)) continue;
	    			if(source == dest) continue;
	    			
	    			int delta    = deltaBetween(source,dest);
	    			int index    = delta + 128;
	    			int absdelta = Math.abs(delta);
	    			
	    			piece.square = source;
	    			piece.piece  = p;
	    			
	    			if(piece.type() == QUEEN || piece.type() == ROOK)
	    			{
	    				if(absdelta < 8)       matt[index] = true; //on same row
	    				if(absdelta % UP == 0) matt[index] = true; //on same col
	    			}
	    			
	    			if(piece.type() == QUEEN || piece.type() == BISHOP)
	    			{
	    				if(absdelta % UP_LEFT  == 0) matt[index] = true; //up-left  or down-left
	    				if(absdelta % UP_RIGHT == 0) matt[index] = true; //up-right or down-right
	    			}
	    			
	    			if(piece.type() == KING)
	    			{
	    				for(int dt : KING_DELTAS)
	    				{
	    					if(delta == dt) matt[index] = true;
	    				}
	    			}
	    			
	    			if(piece.type() == KNIGHT)
	    			{
	    				for(int dt : KNIGHT_DELTAS)
	    				{
	    					if(delta == dt) matt[index] = true;
	    				}
	    			}
	    			
	    			if(piece.type() == PAWN)
	    			{
	    				for(int dt : PAWN_ATTACK_DELTAS[piece.color()])
	    				{
	    					if(delta == dt) matt[index] = true;
	    				}
	    			}
	    		}
	    	}
	    	
	    	MIGHT_ATTACK[p] = matt;
    	}
    }
    public boolean mightAttack(int dest)
    {
    	return MIGHT_ATTACK[this.piece][dest-this.square+128];
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(!(o instanceof ArrayPiece)) return false;
        
        ArrayPiece p = (ArrayPiece)o;
        
        return (p.square == this.square && p.piece == this.piece);
    }
    
    @Override
    public int hashCode()
    {
        return (square << 4) | piece;
    }
    
    @Override
    public String toString()
    {
        return "" + PIECE_STRING.charAt(piece);
    }
    
    /**
     * Given a color and a type, returns the encoding for a piece of the
     * specified color and type.
     * 
     * @param color the color.
     * @param type the type.
     * @return the encoding.
     */
    public static int makePieceCode(int color, int type)
    {
        return ((color << 3) | type);
    }
    
    /**
     * @param piece the code of the piece.
     * @return the color of the piece.
     */
    public static int colorOfPiece(int piece)
    {
        return piece >> 3;
    }
    
    /**
     * @param piece the code of the piece.
     * @return the type of the piece.
     */
    public static int typeOfPiece(int piece)
    {
        return piece & TYPE_MASK;
    }
}
