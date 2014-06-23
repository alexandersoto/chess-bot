package chess.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import chess.util.Iteratorable;
import chess.util.Predicate;


import static chess.board.ArrayPiece.*;

/**
 * @author Owen Durni (opd@andrew.cmu.edu)
 * 
 * 0x88 Board Representation
 * 
 * The coordinate of each square on the board is represented
 * as two 4-bit words.  The upper 4 bits are the row, and the lower
 * 4 bits are the col.
 * 
 * It should be noted that the upper bit in both of the words is not
 * actually used for any valid squares on the board.  It is set, however
 * in all squares which are off the board and is used to detect the
 * end of a sliding piece's ray of movement.
 * 
 * The 0x88 Board gets its name from the above property.  We can test
 * if a square is off the board by bitwise anding its index with 0x88.
 * 
 * Piece lists are maintained for each color and type of piece.
 * You can iterate over any subset of piece lists by passing in
 * an array of the pieces which you want to hit.
 * 
 * Zobrist hashing is used to maintain a signature for the board.
 * The zobrist gets updated whenever the state of the board changes.
 */
public class ArrayBoard implements Board<ArrayMove,ArrayBoard>
{
	//An instance of the board so that you can create new boards without
	//using reflection if all you have is a generic type.
  public static final ArrayBoard FACTORY = new ArrayBoard();
  
  //The state of the board.
  protected ArrayPiece[]         board              = new ArrayPiece[128];
  protected ArrayPieceList       pieceLists         = new ArrayPieceList();
  protected LinkedList<UndoMove> undoStack          = new LinkedList<UndoMove>();
  protected int                  colorToPlay        = WHITE;
  protected int                  ply                = 0;
  protected int                  enpassantSquare    = NO_SQUARE;
  protected long                 signature          = 0;
  public    boolean[]            canCastleKingside  = new boolean[2];
  public    boolean[]            canCastleQueenside = new boolean[2];
  public    boolean[]            hasCastled         = new boolean[2];
  
  //A random number for each piece at each board location.
  protected static long[]        zobrist            = new long[16 * 128];
  
  //Arbitrary bits that affect the zobrist.
  protected static final int     TURN_BITS          = 0x1;
  protected static final int[]   KING_CASTLE_BITS   = { 0x2, 0x4 };
  protected static final int[]   QUEEN_CASTLE_BITS  = { 0x8, 0x10 };
  protected static final int[]   HAS_CASTLED_BITS   = { 0x20, 0x40 };
  protected static final int     NO_SQUARE          = -42;
  
  //Delta directions on the board.
  protected static final int     UP                 = 16;
  protected static final int     DOWN               = -16;
  protected static final int     LEFT               = -1;
  protected static final int     RIGHT              = 1;
  protected static final int     UP_LEFT            = UP   + LEFT;
  protected static final int     UP_RIGHT           = UP   + RIGHT;
  protected static final int     DOWN_LEFT          = DOWN + LEFT;
  protected static final int     DOWN_RIGHT         = DOWN + RIGHT;
  protected static final int     UP_LEFT_CORNER     = indexOfSquare("a8");
  protected static final int     UP_RIGHT_CORNER    = indexOfSquare("h8");
  protected static final int     DOWN_LEFT_CORNER   = indexOfSquare("a1");
  protected static final int     DOWN_RIGHT_CORNER  = indexOfSquare("h1");
  protected static final int     ROW_BITS           = 0xF0;
  protected static final int     COL_BITS           = 0x0F;
  
  //Colors of the pieces on the board.
  public static final int        WHITE              = 1;
  public static final int        BLACK              = 0;
  
  //Delta's for the various pieces.
  public static final int[]      KNIGHT_DELTAS      = {
    UP    * 2 + RIGHT,
    RIGHT * 2 + UP,
    RIGHT * 2 + DOWN,
    DOWN  * 2 + RIGHT,
    DOWN  * 2 + LEFT,
    LEFT  * 2 + DOWN,
    LEFT  * 2 + UP,
    UP    * 2 + LEFT
  };
  public static final int[]      ROOK_DELTAS        = {
    UP,
    RIGHT,
    DOWN,
    LEFT
  };
  public static final int[]      BISHOP_DELTAS      = {
    UP_RIGHT,
    DOWN_RIGHT,
    DOWN_LEFT,
    UP_LEFT
  };
  public static final int[]      QUEEN_DELTAS       = merge(
    BISHOP_DELTAS,
    ROOK_DELTAS
  );
  public static final int[]      KING_DELTAS        = merge(
    BISHOP_DELTAS,
    ROOK_DELTAS
  );
  public static final int[][]    PAWN_ATTACK_DELTAS = new int[2][];
  static {
    int[] ws = { UP+LEFT  , UP+RIGHT   };
    int[] bs = { DOWN+LEFT, DOWN+RIGHT };
    
    PAWN_ATTACK_DELTAS[WHITE] = ws;
    PAWN_ATTACK_DELTAS[BLACK] = bs;
  }
  private static int[] PAWN_DIRECTIONS = new int[2];
  static {
  	PAWN_DIRECTIONS[WHITE] = UP;
  	PAWN_DIRECTIONS[BLACK] = DOWN;
  }
  private static int[] DOUBLE_PUSH_ROW = new int[2];
  static {
  	DOUBLE_PUSH_ROW[WHITE] = 1;
  	DOUBLE_PUSH_ROW[BLACK] = 6;
  }
  public static final int[]      FIXED_DELTAS       = merge(
  	KING_DELTAS,
  	KNIGHT_DELTAS
  );
  public static final int[]      SLIDE_DELTAS       = QUEEN_DELTAS;
  
  static {
    Random r = new Random(133927);
    for (int i = 0; i < zobrist.length; ++i)
      zobrist[i] = r.nextLong();
  }
  
  //The 0x88 representation of every valid square on the board (for convenience)
  public static final int A1 = indexOfSquare('a','1');
  public static final int A2 = indexOfSquare('a','2');
  public static final int A3 = indexOfSquare('a','3');
  public static final int A4 = indexOfSquare('a','4');
  public static final int A5 = indexOfSquare('a','5');
  public static final int A6 = indexOfSquare('a','6');
  public static final int A7 = indexOfSquare('a','7');
  public static final int A8 = indexOfSquare('a','8');
  public static final int B1 = indexOfSquare('b','1');
  public static final int B2 = indexOfSquare('b','2');
  public static final int B3 = indexOfSquare('b','3');
  public static final int B4 = indexOfSquare('b','4');
  public static final int B5 = indexOfSquare('b','5');
  public static final int B6 = indexOfSquare('b','6');
  public static final int B7 = indexOfSquare('b','7');
  public static final int B8 = indexOfSquare('b','8');
  public static final int C1 = indexOfSquare('c','1');
  public static final int C2 = indexOfSquare('c','2');
  public static final int C3 = indexOfSquare('c','3');
  public static final int C4 = indexOfSquare('c','4');
  public static final int C5 = indexOfSquare('c','5');
  public static final int C6 = indexOfSquare('c','6');
  public static final int C7 = indexOfSquare('c','7');
  public static final int C8 = indexOfSquare('c','8');
  public static final int D1 = indexOfSquare('d','1');
  public static final int D2 = indexOfSquare('d','2');
  public static final int D3 = indexOfSquare('d','3');
  public static final int D4 = indexOfSquare('d','4');
  public static final int D5 = indexOfSquare('d','5');
  public static final int D6 = indexOfSquare('d','6');
  public static final int D7 = indexOfSquare('d','7');
  public static final int D8 = indexOfSquare('d','8');
  public static final int E1 = indexOfSquare('e','1');
  public static final int E2 = indexOfSquare('e','2');
  public static final int E3 = indexOfSquare('e','3');
  public static final int E4 = indexOfSquare('e','4');
  public static final int E5 = indexOfSquare('e','5');
  public static final int E6 = indexOfSquare('e','6');
  public static final int E7 = indexOfSquare('e','7');
  public static final int E8 = indexOfSquare('e','8');
  public static final int F1 = indexOfSquare('f','1');
  public static final int F2 = indexOfSquare('f','2');
  public static final int F3 = indexOfSquare('f','3');
  public static final int F4 = indexOfSquare('f','4');
  public static final int F5 = indexOfSquare('f','5');
  public static final int F6 = indexOfSquare('f','6');
  public static final int F7 = indexOfSquare('f','7');
  public static final int F8 = indexOfSquare('f','8');
  public static final int G1 = indexOfSquare('g','1');
  public static final int G2 = indexOfSquare('g','2');
  public static final int G3 = indexOfSquare('g','3');
  public static final int G4 = indexOfSquare('g','4');
  public static final int G5 = indexOfSquare('g','5');
  public static final int G6 = indexOfSquare('g','6');
  public static final int G7 = indexOfSquare('g','7');
  public static final int G8 = indexOfSquare('g','8');
  public static final int H1 = indexOfSquare('h','1');
  public static final int H2 = indexOfSquare('h','2');
  public static final int H3 = indexOfSquare('h','3');
  public static final int H4 = indexOfSquare('h','4');
  public static final int H5 = indexOfSquare('h','5');
  public static final int H6 = indexOfSquare('h','6');
  public static final int H7 = indexOfSquare('h','7');
  public static final int H8 = indexOfSquare('h','8');
  
  private ArrayBoard()
  {}

  public ArrayBoard create  ()
  {
    return new ArrayBoard();
  }

  public ArrayBoard init  (String fen)
  {
  	//The current board is in some garbage state, so we need
  	//to reset to the starting state first
  	pieceLists.clear();
  	undoStack.clear();
  	colorToPlay         = WHITE;
  	enpassantSquare     = NO_SQUARE;
    signature           = 0;
    Arrays.fill(board,              null );
    Arrays.fill(canCastleKingside,  false);
    Arrays.fill(canCastleQueenside, false);
    Arrays.fill(hasCastled,         false);
  	
  	String[] fenparts  = fen.split(" ");
    String   pieces    = fenparts[0];
    String   turn      = fenparts[1];
    String   castling  = fenparts[2];
    String   enpassant = fenparts[3];
      
    /*
     * Place all of the pieces onto the board.
     */
    int square = A8;
    for (char c : pieces.toCharArray())
    {
      switch (c)
      {
      case '8':
      case '7':
      case '6':
      case '5':
      case '4':
      case '3':
      case '2':
      case '1':
        int spaces = RIGHT*(c-'0');
        while( spaces > 0 )
        {
        	assert(onboard(square));
          addPiece(ArrayPiece.FACTORY.create().init(EMPTY, square));
          ++square;
          --spaces;
        }
      break;
      case '/':
        square &= ~COL_BITS;
        square += DOWN;
      break;
      default:
        addPiece(ArrayPiece.FACTORY.create().init(c,square));
        square += RIGHT;
      break;
      }
    }
    
    /*
     * Set Turn
     */
    if( turn.equals("w") )
    {
      setTurn(WHITE);
    }
    else if ( turn.equals("b") )
    {
    	setTurn(BLACK);
    }
    else
    {
      throw new IllegalArgumentException("Malformed fen: turn not white or black");
    }
    
    /*
     * Set Castle Bits
     */
    setCastleKingside (WHITE, castling.contains("K"));
    setCastleKingside (BLACK, castling.contains("k"));
    setCastleQueenside(WHITE, castling.contains("Q"));
    setCastleQueenside(BLACK, castling.contains("q"));
    setHasCastled     (WHITE, castling.contains("H"));
    setHasCastled     (BLACK, castling.contains("h"));
    
    /*
     * Set Enpassant Square
     * 
     * This should be the square "behind" where a pawn just double pushed.
     */
    if( enpassant.equals("-") )
    {
      setEnpassantSquare(NO_SQUARE);
    }
    else
    {
    	int esqr = indexOfSquare(enpassant);
    	
    	if(board[esqr].isOccupied())
    		throw new IllegalArgumentException("Malformed fen: impossible enpassant square");
    	
    	if(toPlay() == WHITE)
    	{
    		if(board[esqr-DOWN].isOccupied())
    			throw new IllegalArgumentException("Malformed fen: impossible enpassant square");
    		if(board[esqr+DOWN].piece != BLACK_PAWN)
    			throw new IllegalArgumentException("Malformed fen: impossible enpassant square");
    	}
    	else // toPlay() == BLACK
    	{
    		if(board[esqr-UP].isOccupied())
    			throw new IllegalArgumentException("Malformed fen: impossible enpassant square");
    		if(board[esqr+UP].piece != WHITE_PAWN)
    			throw new IllegalArgumentException("Malformed fen: impossible enpassant square");
    	}
    	
      setEnpassantSquare(esqr);
    }
	
    assert(noNullsOnBoard());
    
    return this;
  }

  public ArrayBoard copy    ()
  {
    ArrayBoard copy = create();
    
    //make a deep copy of the board.
    for( int i = 0; i < board.length; ++i )
    {
        if(onboard(i))
        {
        	ArrayPiece pc = board[i].copy();
        	
          copy.board[i] = pc;
          copy.pieceLists.add(pc);
        }
    }
    
    //make a deep copy of the undo stack
    for(UndoMove u : undoStack)
    {
    	copy.undoStack.addLast(u.copy());
    }
    
    copy.colorToPlay     = colorToPlay;
    copy.enpassantSquare = enpassantSquare;
    copy.signature       = signature;
    
		System.arraycopy(this.canCastleKingside, 0, copy.canCastleKingside, 0, copy.canCastleKingside.length);
		System.arraycopy(this.canCastleQueenside, 0, copy.canCastleQueenside, 0, copy.canCastleQueenside.length);
		System.arraycopy(this.hasCastled, 0, copy.hasCastled, 0, copy.hasCastled.length);
    
    return copy;
  }
  
  @Override
  public boolean equals        (Object o)
  {
      if(this == o) return true;
      if(o == null) return false;
      if(!(o instanceof ArrayBoard)) return false;
      
      ArrayBoard b = (ArrayBoard)o;
      
      if( this.signature != b.signature ) return false;
      if( this.colorToPlay != b.colorToPlay ) return false;
      if( this.enpassantSquare != b.enpassantSquare ) return false;
      if( !Arrays.equals(this.canCastleKingside , b.canCastleKingside ) ) return false;
      if( !Arrays.equals(this.canCastleQueenside, b.canCastleQueenside) ) return false;
      if( !Arrays.equals(this.hasCastled        , b.hasCastled        ) ) return false;
      
      return this.pieceLists.equals(b.pieceLists);
  }
  
  @Override
  public int     hashCode      ()
  {
      return (int)signature;
  }
  
  public long    signature     ()
  {
      return signature;
  }
  
  public int     toPlay()
  {
      return colorToPlay;
  }
  
  public void    applyMove(ArrayMove move)
  {
  	ArrayPiece source       = move.source;
  	ArrayPiece dest         = move.dest;
  	int        s            = source.square;
  	int        d            = dest.square;
  	boolean    moved        = false;
  	boolean    doublepushed = false;
  	UndoMove   undo         = new UndoMove().init(this, move);
  	
    //assert(noNullsOnBoard());
    //assert(isLegalMove(move));
  	
  	undoStack.addFirst(undo);
  	
  	//assert(onboard(s));
  	//assert(onboard(d));
  	
  	if(source.type() == KING)
  	{
  		int scolor = source.color();
  		setCastleKingside (scolor,false);
  		setCastleQueenside(scolor,false);
  		
  		if(s == E1)
  		{
  			if(d == G1)
  			{
  				//white kingside castle
  				movePiece(E1,G1);
  				movePiece(H1,F1);
  				setHasCastled(WHITE,true);
  				moved = true;
  			}
  			else if (d == C1)
  			{
  				//white queenside castle
  				movePiece(E1,C1);
  				movePiece(A1,D1);
  				setHasCastled(WHITE,true);
  				moved = true;
  			}
  		}
  		else if(s == E8)
  		{
  			if(d == G8)
  			{
  				//black kingside castle
  				movePiece(E8,G8);
  				movePiece(H8,F8);
  				setHasCastled(BLACK,true);
  				moved = true;
  			}
  			else if (d == C8)
  			{
  				//black queenside castle
  				movePiece(E8,C8);
  				movePiece(A8,D8);
  				setHasCastled(BLACK,true);
  				moved = true;
  			}
  		}
  		//else normal king move; wait till later.
  	}
  	
  	else if(source.type() == PAWN)
  	{
  		//if enpassant
		if( enpassantSquare == d )
  		{
			int ecapIndex         = indexOfSquare(rowOfSquare(s),colOfSquare(d));
  			undo.enpassantCapture = board[ecapIndex].copy();
  			removePiece(ecapIndex);
  			movePiece(s,d);
  			moved = true;
  		}
  		//if double push
		else if( d-s == 2*UP )
  		{
  			setEnpassantSquare(s+UP);
  			doublepushed = true;
  		}
  		else if (d-s == 2*DOWN)
  		{
  			setEnpassantSquare(s+DOWN);
  			doublepushed = true;
  		}
  		else
  		{    			
			//if promotion
  			if(move.isPromotion())
  			{
  				movePiece(s,d);
  				removePiece(d);
  				move.promote.square = d;
  				deleteThenAddPiece(d,move.promote);
  				moved = true;
  			}
  			//else regular move; wait till later
  		}
  	}
  	
  	//castling bits
  	     if(d == A1) setCastleQueenside(WHITE,false);
  	else if(d == H1) setCastleKingside (WHITE,false);
  	else if(d == A8) setCastleQueenside(BLACK,false);
  	else if(d == H8) setCastleKingside (BLACK,false);
  	
  	//otherwise
  	if(!moved)
  		movePiece(s,d);
  	
  	if(!doublepushed)
		setEnpassantSquare(NO_SQUARE);
  	
  	flipTurn();
  	
    //assert(noNullsOnBoard());
  }
  
  public void    undoMove()
  {
  	UndoMove u = undoStack.removeFirst();
  	
  	ArrayMove  move   = u.move;
  	ArrayPiece source = move.source;
  	ArrayPiece dest   = move.dest;
  	int        s      = source.square;
  	int        d      = dest.square;
  	boolean    moved  = false;
  	
  	assert(onboard(s));
  	assert(onboard(d));
      assert(noNullsOnBoard());
  	
  	if(source.type() == KING)
  	{	
  		if(s == E1)
  		{
  			if(d == G1)
  			{
  				//white kingside castle
  				movePiece(G1,E1);
  				movePiece(F1,H1);
  				moved = true;
  			}
  			else if (d == C1)
  			{
  				//white queenside castle
  				movePiece(C1,E1);
  				movePiece(D1,A1);
  				moved = true;
  			}
  		}
  		else if(s == E8)
  		{
  			if(d == G8)
  			{
  				//black kingside castle
  				movePiece(G8,E8);
  				movePiece(F8,H8);
  				moved = true;
  			}
  			else if (d == C8)
  			{
  				//black queenside castle
  				movePiece(C8,E8);
  				movePiece(D8,A8);
  				moved = true;
  			}
  		}
  		//else normal king move; wait till later.
  	}
  	else if(source.type() == PAWN)
  	{
  		ArrayPiece cap = u.enpassantCapture;
  		
  		//if enpassant
  		if( cap != null )
  		{
  			deleteThenAddPiece(cap.square, cap);
  		}
  		else if(u.move.isPromotion())
  		{
  			deleteThenAddPiece(source.square, source);
  			moved = true;
  		}
  	}
  	
  	if(!moved)
  		movePiece(d,s);
  	
  	if(dest.isOccupied() || u.move.isPromotion())
  		deleteThenAddPiece(dest.square, dest);
  	
  	setEnpassantSquare (u.enpassantSquare);
      setCastleKingside  (WHITE, u.canCastleKingside[WHITE]);
      setCastleKingside  (BLACK, u.canCastleKingside[BLACK]);
      setCastleQueenside (WHITE, u.canCastleQueenside[WHITE]);
      setCastleQueenside (BLACK, u.canCastleQueenside[BLACK]);
      setHasCastled      (WHITE, u.hasCastled[WHITE]);
      setHasCastled      (BLACK, u.hasCastled[BLACK]);
  	
  	flipTurn();
  	
      assert(noNullsOnBoard());
  }
  
  public List<ArrayMove> generatePseudoMoves()
  {
  	List<ArrayPiece> piece_accum     = new ArrayList<ArrayPiece>(128);
  	List<ArrayMove>  move_accum      = new ArrayList<ArrayMove>(256);
  	
  	// Fill in the tables with moves
  	final int                   ME             = toPlay();
  	final int                   YOU            = 1 - ME;
  	final int                   MY_KNIGHT      = makePieceCode(ME,KNIGHT);
  	final int                   MY_BISHOP      = makePieceCode(ME,BISHOP);
  	final int                   MY_QUEEN       = makePieceCode(ME,QUEEN);
  	final int                   MY_ROOK        = makePieceCode(ME,ROOK);
  	final int                   MY_KING        = makePieceCode(ME,KING);
  	final int                   MY_PAWN        = makePieceCode(ME,PAWN);
  	final Predicate<ArrayPiece> P_NOT_MY_PIECE = P_IS_NOT_COLOR[ME];
  	final Predicate<ArrayPiece> P_YOUR_PIECE   = P_IS_COLOR[YOU];
  	
  	for(ArrayPiece p : allPiecesMatching(MY_KNIGHT))
  	{
  		assert(p != null);
  		assert(p == board[p.square]);
  		getAllFixedAttacksBy(p.square,KNIGHT_DELTAS,P_NOT_MY_PIECE,piece_accum);
  		makeMoves(p,piece_accum,move_accum);
          piece_accum.clear();
  	}
  	
  	for(ArrayPiece p : allPiecesMatching(MY_BISHOP,MY_QUEEN))
  	{
  		assert(p != null);
  		assert(p == board[p.square]);
  		getAllSlidingAttacksBy(p.square,BISHOP_DELTAS,P_NOT_MY_PIECE,piece_accum);
  		makeMoves(p,piece_accum,move_accum);
          piece_accum.clear();
  	}
  	
  	for(ArrayPiece p : allPiecesMatching(MY_ROOK,MY_QUEEN))
  	{
  		assert(p != null);
  		assert(p == board[p.square]);
  		getAllSlidingAttacksBy(p.square,ROOK_DELTAS,P_NOT_MY_PIECE,piece_accum);
  		makeMoves(p,piece_accum,move_accum);
          piece_accum.clear();
  	}
  	
  	for(ArrayPiece p : allPiecesMatching(MY_KING))
  	{
  		assert(p != null);
  		assert(p == board[p.square]);
  		getAllFixedAttacksBy(p.square,KING_DELTAS,P_NOT_MY_PIECE,piece_accum);
  		makeCastlingMoves(p,ME,move_accum);
  		makeMoves(p,piece_accum,move_accum);
          piece_accum.clear();
  	}
  	
  	for(ArrayPiece p : allPiecesMatching(MY_PAWN))
  	{
  		assert(p != null);
  		assert(p == board[p.square]);
  		getAllPawnMovesBy(p.square,ME,piece_accum);
  		getAllFixedAttacksBy(p.square,PAWN_ATTACK_DELTAS[ME],P_YOUR_PIECE,piece_accum);
  		makeEnpassants(p,move_accum);
  		makePromotions(p,piece_accum,move_accum);
  		makeMoves(p,piece_accum,move_accum);
          piece_accum.clear();
  	}
  	
  	return move_accum;
  }
  
  /**
   * Fills an accumulator of moves for a given piece.
   * 
   * @param source the piece to move.
   * @param dests a list of places the source can move to.
   * @param accumulator the list to accumulate moves in.
   */
  private void makeMoves(ArrayPiece source, List<ArrayPiece> dests, List<ArrayMove> accumulator)
  {
  	for(ArrayPiece dest : dests)
  	{
  		ArrayMove m = ArrayMove.FACTORY.create().init(source, dest, dest);
  		accumulator.add(m);
  	}
  }
  
  /**
   * Fills an accumulator of castling moves for the given king.
   * 
   * @param source the king.
   * @param color the color of the king.
   * @param accumulator the list to accumulate moves in.
   */
  private void makeCastlingMoves(ArrayPiece source, int color, List<ArrayMove> accumulator)
  {
      assert(source.type() == KING);
      assert(color == source.color());
      
      if(color == WHITE)
      {
          if( canCastleKingside[WHITE] && source.square == E1
                  && board[F1].isEmpty() && board[G1].isEmpty()
                  && board[H1].piece == WHITE_ROOK)
          {
              ArrayMove m = ArrayMove.FACTORY.create().init(source,board[G1],board[G1]);
              accumulator.add(m);
          }
          
          if( canCastleQueenside[WHITE] && source.square == E1
                  && board[D1].isEmpty() && board[C1].isEmpty() && board[B1].isEmpty()
                  && board[A1].piece == WHITE_ROOK)
          {
              ArrayMove m = ArrayMove.FACTORY.create().init(source,board[C1],board[C1]);
              accumulator.add(m);
          }
      }
      else
      {
          assert(color == BLACK);
          
          if( canCastleKingside[BLACK] && source.square == E8
                  && board[F8].isEmpty() && board[G8].isEmpty()
                  && board[H8].piece == BLACK_ROOK)
          {
              ArrayMove m = ArrayMove.FACTORY.create().init(source,board[G8],board[G8]);
              accumulator.add(m);
          }
          
          if( canCastleQueenside[BLACK] && source.square == E8
                  && board[D8].isEmpty() && board[C8].isEmpty() && board[B8].isEmpty()
                  && board[A8].piece == BLACK_ROOK)
          {
              ArrayMove m = ArrayMove.FACTORY.create().init(source,board[C8],board[C8]);
              accumulator.add(m);
          }
      }
  }
  
  /**
   * Fills an accumulator of enpassant moves for the given pawn.
   * 
   * @param source the pawn.
   * @param accumulator the list to accumulate moves in.
   */
  private void makeEnpassants(ArrayPiece source, List<ArrayMove> accumulator)
  {
      List<ArrayPiece> pacc = new ArrayList<ArrayPiece>(2);
      getAllEnpassantAttackBy(source.square,pacc);
      
      for(ArrayPiece dest : pacc)
      {
    	  //the location of the piece captured by the enpassant
    	  int captured = dest.square + (source.color() == WHITE ? DOWN : UP);
          ArrayMove m = ArrayMove.FACTORY.create().init(source,dest,board[captured]);
          accumulator.add(m);
      }
  }
  
  /**
   * Fills an accumulator with all of the pieces which are enpassant attacked
   * by the given square.
   * 
   * @param square the location of the pawn making the enpassant
   * @param accumulator the list to accumulate pieces in.
   */
  private void getAllEnpassantAttackBy(int square, List<ArrayPiece> accumulator)
  {
  	assert(onboard(square));
  	assert(board[square].type() == PAWN);
  	
  	if(onboard(enpassantSquare) && board[square].mightAttack(enpassantSquare))
  	{
  		ArrayPiece dest = board[enpassantSquare];
  		
  		//if it is your piece, you can't capture.
  		//it can't be opponent's piece because they just double pushed.
  		if(dest.isEmpty())
  			accumulator.add(dest);
  	}
  }
  
  /**
   * Fills an accumulator with moves which are promotions for the given pawn.
   * 
   * The destinations list will have all of the promotion moves removed from it.
   * 
   * @param source the pawn to move.
   * @param dests all of the locations where this pawn can move.
   * @param accumulator the list to accumulate moves in.
   */
  private void makePromotions(ArrayPiece source, List<ArrayPiece> dests, List<ArrayMove> accumulator)
  {
      assert(source.type() == PAWN);
      
      ArrayList<ArrayPiece> promotions = new ArrayList<ArrayPiece>(16);
      
      for(ArrayPiece dest : dests)
      {
          int drow = dest.row();
          
          if(drow == 0 || drow == 7)
          {
              for(int promote : PROMOTED_PIECES[source.color()])
              {
                  ArrayPiece pr = ArrayPiece.FACTORY.create().init(promote, dest.square);
                  ArrayMove  m  = ArrayMove.FACTORY.create().init(source, dest, pr, dest);
                  accumulator.add(m);
              }
              promotions.add(dest);
          }
      }
      
      dests.removeAll(promotions);
  }
  
  public List<ArrayMove> generateMoves() {
	  
    List<ArrayMove> psmoves  = generatePseudoMoves();
  	List<ArrayMove> moves    = new ArrayList<ArrayMove>(psmoves.size());
  	Set<ArrayMove>  setmoves = new HashSet<ArrayMove>(256);
  	
  	for(ArrayMove m : psmoves) {
  		if( setmoves.add(m) && isLegalPseudoMove(m) ) {
  				moves.add(m);
  		}
  	}
  	
  	return moves;
  }
  
  public boolean isLegalMove(ArrayMove move)
  {
    /*
     * To determine if a move is legal we must have that
     * 
     * - the src and dest squares must be on the board
     * - the src, dest, and (promote) pieces must be chess pieces
     * - the src piece must be at the src square
     * - if any, the dest piece must be at the dest square
     * - if non-enpassant capture, the capture piece must be
     *     at the dest square
     * - if enpassant capture, the capture piece must be at the
     *     enpassant square
     * - if any, the promote piece must be at the dest square
     * - the src piece must be able to attack the dest square
     *     (pawn pushes and enpassants are a special case)
     * - the src piece must be of the color toPlay
     * - for castling, the player must be able to castle and
     *    the src piece must be a king
     * - for promoting, the dest must be the appropriate row,
     *    the src piece must be a pawn, and the promote piece
     *    must be a QRBN of the same color as the promoting pawn
     * - for capturing, the dest piece must be of the opposite
     *    color.
     * 
     * (*) If all of these are true, then the move is a pseudomove
     *  so we can just call isLegalPseudoMove to take care of
     *  check and castling through check.
     */
    
    ArrayPiece srcpiece  = move.source;
    ArrayPiece destpiece = move.dest;
    ArrayPiece prompiece = move.promote;
    ArrayPiece cappiece  = move.capture;
    int        srcsq     = srcpiece.square;
    int        destsq    = destpiece.square;
    int        delta     = deltaBetween(srcsq,destsq);
    int        dt        = unitDeltaOf(delta);
    
    // the src and dest squares must be on the board
    if( !onboard(srcsq) || !onboard(destsq) )
    {
      return false;
    }
    
    // src cannot be dest
    if( srcsq == destsq )
    {
      return false;
    }
    
    // the src, dest, promote and capture pieces must be chess pieces
    // this should always be true...impossible to make an ArrayPiece
    // from an invalid movestring
    
    // the src piece must be at the src square
    if( !board[srcsq].equals(srcpiece) )
    {
      return false;
    }
    
    // if any, the dest piece must be at the dest square
    if( !board[destsq].equals(destpiece) )
    {
      return false;
    }
    
    // the src piece must be of the color toPlay
    if( srcpiece.color() != toPlay() )
    {
      return false;
    }
    
    if( move.isCapture() )
    {
      // the cap piece must be on the board
      if( !onboard(cappiece.square) )
      {
    	return false;
      }
    	
	  // if any, the cap piece must be at the cap square
	  if( !board[cappiece.square].equals(cappiece) )
	  {
	    return false;
	  }
    	
      // can't capture your own piece
      if( srcpiece.color() == cappiece.color() )
      {
        return false;
      }
      
      if( move.isEnpassant() )
      {
          // the capture piece must be at the correct location
    	  int capsq = enpassantSquare + (srcpiece.color() == WHITE ? DOWN : UP);
    	  if( !board[capsq].equals(cappiece) )
    	  {
    		  return false;
    	  }
      }
      else
      {
          // the capture piece must be at dest if non-enpassant
    	  if( !board[destsq].equals(cappiece) )
    	  {
    		  return false;
    	  }
      }
    }
    
    // for promoting, the dest must be the appropriate row,
    //  and the src piece must be a pawn
    if( srcpiece.piece == WHITE_PAWN && rowOfSquare(srcsq) == 6 )
    {
      if( prompiece == null )
      {
        return false;
      }
      
      if
      (
         prompiece.piece != WHITE_QUEEN
      && prompiece.piece != WHITE_ROOK
      && prompiece.piece != WHITE_BISHOP
      && prompiece.piece != WHITE_KNIGHT
      )
      {
        return false;
      }
      
      if( prompiece.square != destpiece.square )
      {
        return false;
      }
    }
    else if( srcpiece.piece == BLACK_PAWN && rowOfSquare(srcsq) == 1 )
    {
      if( prompiece == null )
      {
        return false;
      }
      
      if
      (
         prompiece.piece != BLACK_QUEEN
      && prompiece.piece != BLACK_ROOK
      && prompiece.piece != BLACK_BISHOP
      && prompiece.piece != BLACK_KNIGHT
      )
      {
        return false;
      }
      
      if( prompiece.square != destpiece.square )
      {
        return false;
      }
    }
    else
    {
      //not a promotion
      if( prompiece != null )
        return false;
    }
    
    // for castling, the player must be able to castle and
    //   the src piece must be a king
    if( srcpiece.piece == WHITE_KING && srcsq == E1 )
    {    
      //white kingside
      if( destsq == G1 )
      {
        return (
           canCastleKingside[WHITE]
        && board[F1].isEmpty()
        && board[G1].isEmpty()
        && board[H1].piece == WHITE_ROOK
        && isLegalPseudoMove(move)
        );
      }
    
      //white queenside
      if( destsq == C1 )
      {
        return (
           canCastleQueenside[WHITE]
        && board[B1].isEmpty()
        && board[C1].isEmpty()
        && board[D1].isEmpty()
        && board[A1].piece == WHITE_ROOK
        && isLegalPseudoMove(move)
        );
      }
    }
    
    if( srcpiece.piece == BLACK_KING && srcsq == E8 )
    {   
      //black kingside
      if( destsq == G8 )
      {
        return (
           canCastleKingside[BLACK]
        && board[F8].isEmpty()
        && board[G8].isEmpty()
        && board[H8].piece == BLACK_ROOK
        && isLegalPseudoMove(move)
        );
      }    
    
      //black queenside
      if( destsq == C8 )
      {
        return (
           canCastleQueenside[BLACK]
        && board[B8].isEmpty()
        && board[C8].isEmpty()
        && board[D8].isEmpty()
        && board[A8].piece == BLACK_ROOK
        && isLegalPseudoMove(move)
        );
      }
    }
    
    // the src piece must be able to attack the dest square
    
    //special case for pawns moving foward, because it is
    //not actually an attack.
    if( srcpiece.type() == PAWN )
    {
      if( move.isCapture() )
      {
        // make sure pawn is moving in ok direction for a capture
        if( !srcpiece.mightAttack(destsq) )
        {
          return false;
        }
        
        //pawn can always capture the enpassantSquare
        if( destsq != enpassantSquare )
        {
          //pawn must capture a piece
          if( !board[destsq].isOccupied() )
            return false;
          //pawn cannot capture same color piece
          if( board[destsq].color() == srcpiece.color() )
            return false;
        }
      }
      else //pawn push
      {
        if( srcpiece.color() == WHITE )
        {
          if( delta == 2*UP )
          {
            if( rowOfSquare(srcsq) != 1 )
              return false;
            if( board[srcsq+UP].isOccupied() )
              return false;
            if( board[srcsq+2*UP].isOccupied() )
              return false;
            //else valid double-push
          }
          else if( delta == UP )
          {
            if( board[srcsq+UP].isOccupied() )
              return false;
            //else valid single-push
          }
          else
          {
            //delta not valid for non-capture move
            return false;
          }
        }
        else //srcpiece.color() == BLACK
        {
          if( delta == 2*DOWN )
          {
            if( rowOfSquare(srcsq) != 6 )
              return false;
            if( board[srcsq+DOWN].isOccupied() )
              return false;
            if( board[srcsq+2*DOWN].isOccupied() )
              return false;
            //else valid double-push
          }
          else if( delta == DOWN )
          {
            if( board[srcsq+DOWN].isOccupied() )
              return false;
            //else valid single-push
          }
          else
          {
            //delta not valid for non-capture move
            return false;
          }
        }
      }
    }
    // srcsq + k*dt = destsq for some k
    else if( !srcpiece.mightAttack(destsq) )
    {
      return false;
    }
    else
    {
      // sliding pieces can't slide through something
      for( int pathsq = srcsq + dt; pathsq != destsq; pathsq += dt )
      {
        if( board[pathsq].isOccupied() )
          return false;
      }
      
      //sliding AND fixed pieces can't capture same color piece
      if
      (
         board[destsq].isOccupied()
      && board[destsq].color() == srcpiece.color()
      )
      {
        return false;
      }
    }
    
    //move cannot leave king in check and castling moves cannot
    //castle through check.
    return isLegalPseudoMove(move);
  }
  
  public boolean isLegalPseudoMove(ArrayMove move)
  {
  	ArrayPiece source = move.source;
  	ArrayPiece dest   = move.dest;
  	int        s      = source.square;
  	int        d      = dest.square;
  	
  	final int  ME     = toPlay();
  	
  	//if castle
  	if(source.type() == KING)
  	{
  		if(s == E1)
  		{
  			if(d == G1)
  			{
  				//white kingside castle
  				if(inCheck(ME) || isAttacked(F1,P_IS_COLOR[BLACK]))
  					return false;
  			}
  			else if (d == C1)
  			{
  				//white queenside castle
  				if(inCheck(ME) || isAttacked(D1,P_IS_COLOR[BLACK]))
  					return false;
  			}
  		}
  		else if(s == E8)
  		{
  			if(d == G8)
  			{
  				//black kingside castle
  				if(inCheck(ME) || isAttacked(F8,P_IS_COLOR[WHITE]))
  					return false;
  			}
  			else if (d == C8)
  			{
  				//black queenside castle
  				if(inCheck(ME) || isAttacked(D8,P_IS_COLOR[WHITE]))
  					return false;
  			}
  		}
  	}
  	
  	applyMove(move);
  	boolean legal = !inCheck(ME);
  	undoMove();
  	
  	return legal;
  }
  
  public boolean inCheck()
  {
  	return inCheck(toPlay());
  }
  
  /**
   * Checks whether or not the specified color is currently in check.
   * 
   * @param color the color to check.
   * @return true if the specified color is in check.
   */
  protected boolean inCheck(int color)
  {
    final int ME  = color;
    final int YOU = 1 - ME;
    
    for(ArrayPiece p : allPiecesMatching(makePieceCode(ME,KING)))
    {
      if( isAttacked(p.square, P_IS_COLOR[YOU]) )
      {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * @param square the square to check.
   * @param pred the predicate.
   * @return true if the specified square is attacked by a piece
   * which satisfies the given predicate.
   */
  public boolean isAttacked(int square, Predicate<ArrayPiece> pred)
  {
  	assert(onboard(square));
  	
    return (
         isSlidingAttacked   (square,SLIDE_DELTAS,pred)
      || isFixedAttacked     (square,FIXED_DELTAS,pred)
      || isEnpassantAttacked (square,             pred)
    );
  }
  
  /**
   * Fills an accumulator with all of the attacking pieces (which satisfy the predicate)
   * on a specified square.
   * 
   * @param square the attacked square.
   * @param pred the predicate.
   * @param accumulator the list to accumulate pieces in.
   */
  public void getAllAttacksOn(int square, Predicate<ArrayPiece> pred, List<ArrayPiece> accumulator)
  {
  	assert(onboard(square));
  	
  	getAllSlidingAttacksOn   (square,SLIDE_DELTAS,pred,accumulator);
  	getAllFixedAttacksOn     (square,FIXED_DELTAS,pred,accumulator);
  	getAllEnpassantAttacksOn (square,             pred,accumulator);
  }
  
  /**
   * Fills an accumulator with all of the enpassant attacking pieces(which satisfy the predicate)
   * on the specified square.
   * 
   * @param square the attacked square.
   * @param pred the predicate.
   * @param accumulator the list to accumulate pieces in.
   */
  private void getAllEnpassantAttacksOn(int square, Predicate<ArrayPiece> pred, List<ArrayPiece> accumulator)
  {
  	assert(onboard(square));
  	
  	if(square != enpassantSquare) return;
  	
  	if(rowOfSquare(enpassantSquare) == 2)
  	{
  		int sul = enpassantSquare+UP+LEFT;
  		int sur = enpassantSquare+UP+RIGHT;
  		
  		if(onboard(sul) && board[sul].piece == BLACK_PAWN && pred.check(board[sul]))
  			accumulator.add(board[sul]);
  		if(onboard(sur) && board[sur].piece == BLACK_PAWN && pred.check(board[sur]))
  			accumulator.add(board[sur]);
  	}
  	else if(rowOfSquare(enpassantSquare) == 5)
  	{
  		int sdl = enpassantSquare+DOWN+LEFT;
  		int sdr = enpassantSquare+DOWN+RIGHT;
  		
  		if(onboard(sdl) && board[sdl].piece == WHITE_PAWN && pred.check(board[sdl]))
  			accumulator.add(board[sdl]);
  		if(onboard(sdr) && board[sdr].piece == WHITE_PAWN && pred.check(board[sdr]))
  			accumulator.add(board[sdr]);
  	}
  }
  
  /** 
   * @param square the square to check.
   * @param pred the predicate.
   * @return true if there is an attack by a piece which satisfies the predicate on
   * the given square.
   */
  private boolean isEnpassantAttacked(int square, Predicate<ArrayPiece> pred)
  {
  	assert(onboard(square));
  	
  	if(square != enpassantSquare) return false;
  	
  	if(rowOfSquare(enpassantSquare) == 2)
  	{
  		int sul = enpassantSquare+UP+LEFT;
  		int sur = enpassantSquare+UP+RIGHT;
  		
  		if(onboard(sul) && board[sul].piece == BLACK_PAWN && pred.check(board[sul]))
  			return true;
  		if(onboard(sur) && board[sur].piece == BLACK_PAWN && pred.check(board[sur]))
  			return true;
  	}
  	else if(rowOfSquare(enpassantSquare) == 5)
  	{
  		int sdl = enpassantSquare+DOWN+LEFT;
  		int sdr = enpassantSquare+DOWN+RIGHT;
  		
  		if(onboard(sdl) && board[sdl].piece == WHITE_PAWN && pred.check(board[sdl]))
  			return true;
  		if(onboard(sdr) && board[sdr].piece == WHITE_PAWN && pred.check(board[sdr]))
  			return true;
  	}
  	
  	return false;
  }
  
  /** 
   * @param square the square to check for attacks against.
   * @param deltas the directions to check.
   * @param pred the predicate.
   * @return true if there is an attacking piece (which satisfies the predicate) on
   * any of the deltas. 
   */
  private boolean isFixedAttacked(int square, int[] deltas, Predicate<ArrayPiece> pred)
  {
  	assert(onboard(square));
  	
    for(int dt : deltas)
    {
      int s = square-dt;
      
      if(!onboard(s)) continue;
      
      ArrayPiece p = board[s];
      
      if( p.mightAttack(square) && pred.check(p) )
      {
        return true;
      }
    }
    
    return false;
  }

  /** 
   * @param square the square to check for attacks against.
   * @param directions the directions to check.
   * @param pred the predicate.
   * @return true if there is an attacking piece (which satisfies the predicate) in
   * any of the specified directions. 
   */
  private boolean isSlidingAttacked(int square, int[] directions, Predicate<ArrayPiece> pred)
  {
  	//assert(onboard(square));
  	
    for(int direction : directions)
    {
      int s = square;
      
      while(true)
      {
        s -= direction;
        
        if( !onboard(s) ) break;
        
        ArrayPiece p = board[s];
        
        if( p.mightAttack(square) && pred.check(p) )
        {
          return true;
        }
        
        if( p.isOccupied() ) break;
      }
    }
    
    return false;
  }
  
  /**
   * Fills an accumulator with all of the fixed delta attacks (which satisfy the predicate)
   * by the specified square.
   * 
   * @param square the attacking square.
   * @param deltas the deltas to check.
   * @param pred the predicate.
   * @param accumulator the list to fill with attacked pieces. 
   */
  private void getAllFixedAttacksBy(int square, int[] deltas, Predicate<ArrayPiece> pred, List<ArrayPiece> accumulator)
  {
  	//assert(onboard(square));
  	
  	for(int dt : deltas)
    {
      int s = square+dt;
      
      if(!onboard(s)) continue;
      
      ArrayPiece p = board[s];
      
      //assert(board[square].mightAttack(s));
      
      if( pred.check(p) )
      {
        accumulator.add(p);
      }
    }
  }
  
  private void getAllSlidingAttacksBy(int square, int[] directions, Predicate<ArrayPiece> pred, List<ArrayPiece> accumulator)
  {
  	assert(onboard(square));
  	assert(board[square] != null);
  	
  	for(int direction : directions)
    {
      int s = square;
      
      while(true)
      {
        s += direction;
        
        if( !onboard(s) ) break;
        
        ArrayPiece p = board[s];
        
        assert(p != null);
        assert(board[square].mightAttack(p.square));
        
        if( pred.check(p) )
        {
          accumulator.add(p);
        }
        
        if( p.isOccupied() ) break;
      }
    }
  }
  
  /**
   * Fills an accumulator with all of the sliding attacks (which satisfy the predicate)
   * on a given square.
   * 
   * @param square the defending square.
   * @param directions the directions to check.
   * @param pred the predicate.
   * @param accumulator the list to fill with attacking pieces.
   */
  private void getAllSlidingAttacksOn(int square, int[] directions, Predicate<ArrayPiece> pred, List<ArrayPiece> accumulator)
  {
  	assert(onboard(square));
  	
  	for(int direction : directions)
    {
      int s = square;
      
      while(true)
      {
        s -= direction;
        
        if( !onboard(s) ) break;
        
        ArrayPiece p = board[s];
        
        if( p.mightAttack(square) && pred.check(p) )
        {
          accumulator.add(p);
          break;
        }
        
        if( p.isOccupied() ) break;
      }
    }
  }
  
  /**
   * Fills an accumulator with all of the fixed attacks (which satisfy the predicate)
   * on the specified square.
   * 
   * @param square the defending square.
   * @param deltas the deltas.
   * @param pred the predicate.
   * @param accumulator the list to fill with attacking pieces.
   */
  private void getAllFixedAttacksOn(int square, int[] deltas, Predicate<ArrayPiece> pred, List<ArrayPiece> accumulator)
  {
  	assert(onboard(square));
  	
  	for(int dt : deltas)
    {
      int s = square-dt;
      
      if(!onboard(s)) continue;
      
      ArrayPiece p = board[s];
      
      if( p.mightAttack(square) && pred.check(p) )
      {
        accumulator.add(p);
      }
    }
  }
  
  /**
   * Fills an accumulator with all of the destinations of a given moving pawn.
   * 
   * @param square the location of the pawn.
   * @param color the color of the movement.
   * @param accumulator the list to fill with the destinations.
   */
  private void getAllPawnMovesBy(int square, int color, List<ArrayPiece> accumulator)
  {
  	assert(board[square].type() == PAWN);
  	
  	int direction = PAWN_DIRECTIONS[color];
  	
  	ArrayPiece p = board[square+direction];
  	
  	if(p.isEmpty())
  	{
  		accumulator.add(p);
  	
    	if(rowOfSquare(square) == DOUBLE_PUSH_ROW[color])
    	{
	    	p = board[square+2*direction];
	    	if(p.isEmpty())
	    		accumulator.add(p);
    	}
  	}
  }
  
  public ArrayMove createMoveFromString(String t)
  {
    assert( 4          <= t.length() );
    assert( t.length() <= 6          );
    
  	String     ts      = t.substring(0,2);
  	String     td      = t.substring(2,4);
  	
  	int        s       = indexOfSquare(ts);
  	int        d       = indexOfSquare(td);
  	int        c       = d;
  	ArrayPiece source  = board[s];
  	ArrayPiece dest    = board[d];
  	
  	Character promoteChar;
  	
    //no capture, promote, castle, or enpassant
    if( t.length() == 4 )
    {
      assert( !(d == enpassantSquare && !dest.isOccupied() && source.type() == PAWN) ) : "Smith string `E` suffix missing";
      assert( dest.isEmpty() ) : "Smith string missing suffix of captured piece";
      promoteChar = null;
    }
    else
    {
      assert( t.length() > 4 );
      char c4 = t.charAt(4);
      
      switch( c4 )
      {
      /* Castling */
      case 'C':
      case 'c':
        assert( t.length() == 5 );
        assert( source.type() == KING );
        assert( dest.isEmpty() );
        promoteChar = null; //can't promote on a castle
      break;
        
      /* En passant */
      case 'E':
        assert( t.length() == 5 );
        assert( source.type() == PAWN );
        assert( enpassantSquare == dest.square );
        assert( dest.isEmpty() );
        assert( dest.row() == 2 || dest.row() == 5 );
        c = enpassantSquare + (source.color() == WHITE ? DOWN : UP);
        promoteChar = null; //can't promote on an enpassant
      break;
        
      /* Promotion, but not capture */
      case 'Q':
      case 'R':
      case 'B':
      case 'N':
        assert( source.type() == PAWN );
        assert( dest.isEmpty() );
        promoteChar = c4;
      break;
      
      /* Capture */
      case 'k':
      case 'q':
      case 'r':
      case 'b':
      case 'n':
      case 'p':
        assert( dest.isOccupied() );
        assert( dest.toString().toLowerCase().equals( ""+c4 ));
        
        /* Capture, no Promotion */
        if( t.length() == 5 )
        {
          promoteChar = null;
        }
        else /* Capture and Promotion */
        {
          char c5 = t.charAt(5);
          
          switch( c5 )
          {
          case 'Q':
          case 'R':
          case 'B':
          case 'N':
            promoteChar = c5;
          break;
          
          default:
            throw new IllegalArgumentException(
              "Bad Smith move string; unrecognized 6th character");
          }
        }
      break; //end capture
      
      default:
        throw new IllegalArgumentException(
          "Bad Smith move string; unrecognized 5th character");
      }
    }  	
  	
    //
    // Actually make the move
    //
    ArrayPiece promote = null;
    ArrayPiece capture = board[c];
    
  	//if the move is a promotion
  	if( promoteChar != null )
  	{  	  
  		if(this.toPlay() == WHITE)
  			promoteChar = Character.toUpperCase(promoteChar);
  		else
  		  promoteChar = Character.toLowerCase(promoteChar);
  		
  		promote = ArrayPiece.FACTORY.create().init(promoteChar, dest.square);
  	}
  	
  	ArrayMove move = ArrayMove.FACTORY.create().init(source,dest,promote,capture);
  	
  	return move;
  }
  
  /**
   * Returns the Standard Algebraic Notation string for the
   * specified move.  The string returned will be in a format
   * suitable for use in a PGN.
   * 
   * WARNING: This method was created this semester
   *  (Fall 08) and so it may have bugs.
   * (ie. I don't know of a good way to unit test this method)
   * For example, there was a bug found for King moves in Spring 09.
   * 
   * @return the SAN notation for a move
   */
  public String moveToSANString(ArrayMove move, List<ArrayMove> possibleMoves)
  {
    assert( isLegalMove(move) );
    
    StringBuffer moveString = new StringBuffer();
    
    ArrayPiece srcpiece  = move.source;
    ArrayPiece destpiece = move.dest;
    int        srcsq     = srcpiece.square;
    int        destsq    = destpiece.square;
    boolean    castle    = false;
    
    //castle moves are a special case
    if( srcpiece.type() == KING )
    {
      if( colOfSquare(srcsq) == 4 )
      {
        if( colOfSquare(destsq) == 6 )
        {
          //kingside castle
          moveString.append("O-O");
          castle = true;
        }
        else if( colOfSquare(destsq) == 2 )
        {
          //queenside castle
          moveString.append("O-O-O");
          castle = true;
        }
      }
    }
    if( !castle )
    {
      //moving piece letter
      if( srcpiece.type() == PAWN )
      {
        if( move.isCapture() )
        {
          //file of attacking pawn
          moveString.append(colToString(srcpiece.col()));
        }
        else
        {
          //moving piece letter omitted for non-attacking pawn
        }
      }
      else
      {
        //KQRBN (moving piece letter)
        moveString.append(srcpiece.toString().toUpperCase());
        
        //source disambiguation      
        boolean needRow = false;
        boolean needCol = false;
        
        for( ArrayMove m : possibleMoves )
        {        
          if
          (
             !move.equals(m)
          && destsq == m.dest.square
          && srcpiece.type() == m.source.type()
          )
          {
            assert( srcsq != m.source.square );
            
            if( srcpiece.row() == m.source.row() )
            {
              needCol = true;
            }
            
            if( srcpiece.col() == m.source.col() )
            {
              needRow = true;
            }
          }
        }
        
        if( needCol )
        {
          moveString.append(colToString(srcpiece.col()));
        }
        if( needRow )
        {
          moveString.append(rowToString(srcpiece.row()));
        }
      }
      
      // x for captures
      if( move.isCapture() )
      {
        moveString.append('x');
      }
      
      //dest square
      moveString.append(squareToString(destsq));
      
      if( move.isPromotion() )
      {
        moveString.append('=');
        moveString.append(move.promote.toString().toUpperCase());
      }
    }
    
    //checkmate and check
    applyMove(move);
      if( inCheck() )
      {
        if( generateMoves().size() == 0 )
        {
          //checkmate
          moveString.append('#');
        }
        else
        {
          moveString.append('+');
        }
      }
    undoMove();
    
    return moveString.toString();
  }
  
  public int plyCount()
  {
    return undoStack.size();
  }
  
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();

    s.append("   a b c d e f g h  \n");
    s.append("  +---------------+ \n");
    
    for(int row = 7; row >= 0; --row)
    {
    	s.append(row+1);
      s.append(" |");
      
      for(int col = 0; col <= 7; ++col)
      {
          s.append(board[indexOfSquare(row,col)]);
          if(col != 7)
          	s.append(" ");
      }
      
      s.append("| ");
    	s.append(row+1);
    	s.append("\n");
    }
    
    s.append("  +---------------+ \n");
    s.append("   a b c d e f g h  \n");
    
    return s.toString();
  }
  
  public String fen()
  {
    StringBuilder s = new StringBuilder();
    
    for(int row = 7, blanks = 0; row >= 0; --row)
    {
      for(int col = 0; col <= 7; ++col)
      {
        ArrayPiece p = board[indexOfSquare(row,col)];
        
        if( p.isEmpty() )
        {
          ++blanks;
        }
        else
        {
          if( blanks > 0 )
          {
            s.append(blanks);
            blanks = 0;
          }
          s.append(p);
        }
      }
      
      if( blanks > 0 )
      {
        s.append(blanks);
        blanks = 0;
      }
      if( row > 0 )
      {
        s.append("/");
      }
    }
    
    s.append(" ");
    
    s.append(toPlay() == WHITE ? "w" : "b");
    
    s.append(" ");
    
    s.append(canCastleKingside[WHITE]  ? "K" : "");
    s.append(canCastleQueenside[WHITE] ? "Q" : "");
    s.append(hasCastled[WHITE]         ? "H" : "");
    s.append(canCastleKingside[BLACK]  ? "k" : "");
    s.append(canCastleQueenside[BLACK] ? "q" : "");
    s.append(hasCastled[BLACK]         ? "h" : "");
    
    //if no castling bits
    if(s.charAt(s.length()-1) == ' ')
    	s.append("-");
    
    s.append(" ");
    
    s.append(enpassantSquare == NO_SQUARE  ? "-" : squareToString(enpassantSquare));
    
    return s.toString();
  }
  
  /**
   * Checks if a square is occupied.
   * 
   * @param square the square.
   * @return true iff the specified square is occupied.
   */
  public boolean occupied(int square)
  {
    assert(onboard(square));
    
    return board[square].isOccupied();
  }
  
  /**
   * Checks if a square is empty.
   * 
   * @param square the square.
   * @return true iff the specified square is empty.
   */
  public boolean empty(int square)
  {
    assert(onboard(square));
    
    return board[square].isEmpty();
  }
  
  /**
   * @return an iterable iterator over all non-empty pieces.
   */
  public Iteratorable<ArrayPiece> allPieces()
  {
    return allPiecesMatching(ALL_PIECES);
  }
  
  /**
   * @param color the color of the pieces to iterate over.
   * @return an iterable iterator over all pieces of the specified color.
   */
  public Iteratorable<ArrayPiece> allPiecesOfColor(int color)
  {
    return allPiecesMatching(PIECES_OF_COLOR[color]);
  }
  
  /**
   * @param type the type of the pieces to iterate over
   * @return an iterable iterator over all pieces of the specified type.
   */
  public Iteratorable<ArrayPiece> allPiecesOfType(int type)
  {
    return allPiecesMatching(new int[] {
       makePieceCode(WHITE,type),
       makePieceCode(BLACK,type)
    });
  }
  
  /**
   * @param pieces the piece codes to iterate over.
   * @return an iterable iterator over all pieces of the specified code.
   */
  public Iteratorable<ArrayPiece> allPiecesMatching(int... pieces)
  {
    return pieceLists.iterateOver(pieces);
  }
  
  /**
   * @return the number of pieces on the board.
   */
  public int countOfAllPieces()
  {
  	return pieceLists.countOfAllPieces();
  }
  
  /**
   * Counts the number of pieces of the specified type
   * 
   * @param piece the code for the color and type of piece
   * @return the count.
   */
  public int countOfPiece(int piece)
  {
    return pieceLists.countOfPiece(piece);
  }
  
  /**
   * Counts the number of equivalent pieces of the specified type.
   * Square piece is on is ignored.
   * 
   * @param p the specified piece.
   * @return the count.
   */
  public int countOfPiece(ArrayPiece p)
  {
    return pieceLists.countOfPiece(p);
  }
  
  /**
   * Counts the number of pieces of the specified color.
   * 
   * @param color the color.
   * @return the count.
   */
  public int countOfColor(int color)
  {
    return pieceLists.countOfColor(color);
  }
  
  /**
   * Counts the number of pieces of the specified type.
   * Color is ignored.
   * 
   * @param type the type.
   * @return the count.
   */
  public int countOfType(int type)
  {
    return pieceLists.countOfType(type);
  }
  
  /**
   * Adds a fresh piece to the board at the square
   * indicated by the piece.
   * 
   * @param p the piece.
   */
  protected void addPiece(ArrayPiece p)
  {
    assert(p != null);
    
    addPiece(p, p.square);
  }
  
  /**
   * Creates and adds a piece of the specified code at the
   * specified square.
   * 
   * @param piece the code of the piece.
   * @param square the square to put the piece.
   */
  protected void addPiece(int piece, int square)
  {
  	assert(onboard(square));
  	
  	ArrayPiece p = ArrayPiece.FACTORY.create().init(piece, square);
  	
  	addPiece(p,square);
  }
  
  /**
   * Adds a fresh piece to the board at the specified square,
   * updating the square of the specified piece.
   * 
   * @param p the piece.
   * @param square the square to add the piece to.
   */
  protected void addPiece(ArrayPiece piece, int square)
  {
    assert(piece != null);
    assert(board[square] == null);
    assert(onboard(square));
    
    ArrayPiece p  = piece.copy();
    
    p.square      = square;
    board[square] = p;
    
    updateZobrist(p);
    pieceLists.add(p);
  }
  
  /**
   * Removes the piece at the specified square.
   * 
   * @param square the square of the piece to remove.
   */
  protected void removePiece(int square)
  {
    //assert(board[square] != null);
    //assert(onboard(square));
    
    ArrayPiece p = pickupPiece(square);
    pieceLists.remove(p);
  }
  
  /**
   * Removes the specified piece.
   * 
   * @param p the piece to remove.
   */
  protected void removePiece(ArrayPiece p)
  {
    assert(p != null);
    
    removePiece(p.square);
  }
  
  /**
   * Removes the piece at the destination and moves the piece at the
   * source to the destination.
   * 
   * @param srcSquare the source.
   * @param destSquare the destination.
   */
  protected void movePiece(int srcSquare, int destSquare)
  {
    assert(onboard(srcSquare));
    assert(onboard(destSquare));
    
    removePiece(destSquare);
    replacePiece(destSquare, pickupPiece(srcSquare));
  }
  
  /**
   * Removes the piece at the destination and moves the specified piece
   * to the destination.
   * 
   * @param p the piece to move.
   * @param destSquare the destination.
   */
  protected void movePiece(ArrayPiece p, int destSquare)
  {
    assert(p != null);
    assert(onboard(destSquare));
    
    movePiece(p.square, destSquare);
  }
  
  /**
   * Performs a move where the src piece captures the dest piece,
   * removing the dest piece form the board.
   * 
   * @param src the source piece.
   * @param dest the dest piece.
   */
  protected void movePiece(ArrayPiece src, ArrayPiece dest)
  {
    assert(src != null);
    assert(dest != null);
    
    movePiece(src.square, dest.square);
  }
  
  /**
   * Deletes and replaces the piece in the specified square,
   * replacing it with null the specified piece.
   * 
   * Updates the zobrist signature for both removing the old
   * piece and placing the new piece at the SPECIFIED SQUARE ONLY.
   * 
   * Should only be called by undoMove().
   * 
   * @param square the square.
   * @param p the piece to place.
   */
  private void deleteThenAddPiece(int square, ArrayPiece p)
  {
    assert(onboard(square));
    assert(p != null);
    
    ArrayPiece old = board[square];
    board[square]  = null;        
    updateZobrist(old);
    pieceLists.remove(old);
    
    p.square       = square;
    addPiece(p);
  }
  
  /**
   * Overwrites the piece in the specified square with the
   * specified piece.  Ignores the square of the specified
   * piece.
   * 
   * Updates the zobrist signature for both removing the old
   * piece and placing the new piece at the SPECIFIED SQUARE ONLY.
   * 
   * Should only be called by pickupPiece() and movePiece().
   * 
   * @param square the square.
   * @param p the piece to place.
   */
  private void replacePiece(int square, ArrayPiece p)
  {
    assert(onboard(square));
    assert(p != null);
    
    ArrayPiece old = board[square];
    p.square       = square;
    board[square]  = p;
    
    updateZobrist(old);
    updateZobrist(p);
  }
  
  /**
   * Picks a piece up from the specified square and returns it.
   * 
   * Should only be called by movePiece() and removePiece()
   * 
   * @param square the square of the piece to pickup.
   * @return the piece.
   */
  private ArrayPiece pickupPiece(int square)
  {
    assert(onboard(square));
    
    ArrayPiece p     = board[square];
    ArrayPiece empty = ArrayPiece.FACTORY.create().init(EMPTY, square);

    replacePiece(square, empty);
    
    return p;
  }
  
  /**
   * Updates the zobrist signature when a piece is placed or picked
   * up from the board.
   * 
   * Should only be called by replacePiece() and addPiece()
   * 
   * @param p the piece.
   */
  private void updateZobrist(ArrayPiece p)
  {
    signature ^= zobrist[16 * p.square + p.piece];
  }
  
  /**
   * Flips whose turn it is.
   */
  protected void flipTurn()
  {
    signature   ^= TURN_BITS;
    colorToPlay  = 1 - colorToPlay;
  }
  
  /**
   * Sets the turn to the specified color
   * 
   * @param color the color.
   */
  protected void setTurn(int color)
  {
  	if(colorToPlay != color)
  		flipTurn();
  }
  
  /**
   * Updates the file where a pawn was double pushed last move.
   * 
   * @param file the file.
   */
  protected void setEnpassantSquare(int square)
  {
    signature       ^= enpassantSquare ^ square;
    enpassantSquare  = square;
  }
  
  /**
   * Updates whether or not a player can castle kingside
   * 
   * @param color the player
   * @param state true iff player can castle.
   */
  protected void setCastleKingside(int color, boolean state)
  {
    if( state != canCastleKingside[color] )
    {
        signature                ^= KING_CASTLE_BITS[color];
        canCastleKingside[color]  = state;
    }
  }
  
  /**
   * Updates whether or not a player can castle queenside
   * 
   * @param color the player
   * @param state true iff player can castle.
   */
  protected void setCastleQueenside(int color, boolean state)
  {
    if( state != canCastleQueenside[color] )
    {
        signature                 ^= QUEEN_CASTLE_BITS[color];
        canCastleQueenside[color]  = state;
    }
  }
  
  /**
   * Updates whether or not a player has castled
   * 
   * @param color the player
   * @param state true iff player can castle.
   */
  protected void setHasCastled(int color, boolean state)
  {
    if( state != hasCastled[color] )
    {
        signature         ^= HAS_CASTLED_BITS[color]; 
        hasCastled[color]  = state;
    }
  }
  
  /**
   * Checks whether a square is on the board.
   * 
   * @param square the square.
   * @return true iff the square is on the board.
   */
  public static boolean onboard(int square)
  {
    return ((square & 0x88) == 0);
  }
  
  /**
   * Checks whether a piece is on the board.
   * 
   * @param p the piece.
   * @return true iff the square is on the board.
   */
  public static boolean onboard(ArrayPiece p)
  {
  	return onboard(p.square);
  }
  
  /**
   * @param square the square.
   * @return the row of the specified square.
   */
  public static int rowOfSquare(int square)
  {
    return ((square & ROW_BITS) >> 4);
  }
  
  /**
   * @param square the square.
   * @return the col of the specified square.
   */
  public static int colOfSquare(int square)
  {
    return (square & COL_BITS);
  }
  
  /**
   * Takes a row and col, and converts to the 0x88 encoded index.
   * 
   * @param row the row.
   * @param col the col.
   * @return the encoded index.
   */
  public static int indexOfSquare(int row, int col)
  {
    return (UP*row + RIGHT*col);
  }

  public static int indexOfSquare(char row, int col)
  {
    throw new UnsupportedOperationException("Use the char/char or the int/int version");
  }
  
  /**
   * Takes a row and col, and converts to the 0x88 encoded index.
   * 
   * @param file the col.
   * @param rank the row.
   * @return the encoded index.
   */
  public static int indexOfSquare(char file, char rank)
  {
    return indexOfSquare(rank - '1',file - 'a');
  }
  
  /**
   * Takes the String representation of a square on a chess board and
   * converts to the 0x88 encoded index.
   * 
   * @param square the square.
   * @return the encoded index.
   */
  public static int indexOfSquare(String square)
  {
    assert(square.length() == 2);
    
    return indexOfSquare(square.charAt(1) - '1',square.charAt(0) - 'a');
  }
  
  /**
   * A toString method for an encoded square.
   * 
   * @param square the square.
   * @return the String representation of the square.
   */
  public static String squareToString(int square)
  {
  	assert(onboard(square));
  	
    return squareToString(rowOfSquare(square), colOfSquare(square));
  }
  
  /**
   * A toString method for a row and a col on a chessboard.
   * 
   * @param row the row.
   * @param col the col.
   * @return the String representation of the row/col.
   */
  public static String squareToString(int row, int col)
  {
  	assert(onboard(indexOfSquare(row,col)));
  	
    return colToString(col) + rowToString(row);
  }
  
  /**
   * A toString method for a row on a chessboard.
   * 
   * @param row the row.
   * @return the String representation of the row.
   */
  public static String rowToString( int row )
  {
    return "" + ((char)('1'+row));
  }
  
  /**
   * A toString method for a col on a chessboard.
   * 
   * @param col the col.
   * @return the String representation of the col.
   */
  public static String colToString( int col )
  {
    return "" + ((char)('a'+col));
  }
  
  /**
   * Returns the delta between two squares
   * 
   * @param src the source square
   * @param dest the dest square
   * @return the delta from src to dest
   */
  public static int deltaBetween( int src, int dest )
  {
    return dest-src;
  }
  
  /**
   * Returns the unit direction of the delta specified.
   * 
   * For deltas which are not a cardinal direction, returns
   * the delta unmodified.
   * 
   * @param delta the delta.
   * @return the unit delta in the direction of the specified delta.
   */
  public static int unitDeltaOf( int delta )
  {
    if( delta > 0 )
    {
      if( delta < 8 )
        return RIGHT;
      if( delta % UP == 0 )
        return UP;
      if( delta % UP_LEFT == 0 )
        return UP_LEFT;
      if( delta % UP_RIGHT == 0 )
        return UP_RIGHT;
    }
    if( delta < 0 )
    {
      if( delta > -8 )
        return LEFT;
      if( delta % DOWN == 0 )
        return DOWN;
      if( delta % DOWN_LEFT == 0 )
        return DOWN_LEFT;
      if( delta % DOWN_RIGHT == 0 )
        return DOWN_RIGHT;
    }
    
    return delta;
  }
  
  /**
   * Merges all of the arrays provided as arguments.
   * 
   * @param arrs the arrays
   * @return the merged array
   */
  public static int[]  merge(int[]... arrs)
  {
    int newSize = 0;
    for(int[] arr : arrs)
    {
      newSize += arr.length;
    }
    
    int[] answer = new int[newSize];
    
    for(int i = 0, n = 0; n < arrs.length; ++n)
    {
      for(int j = 0; j < arrs[n].length; ++i, ++j)
      {
          answer[i] = arrs[n][j];
      }
    }
    
    return answer;
  }
  
  /**
   * Used for debugging.
   * 
   * @return true if every valid square on the board contains
   * a valid piece.
   */
  private boolean noNullsOnBoard()
  {
  	for(int row = 0; row < 8; ++row)
  		for(int col = 0; col < 8; ++col)
  			if(board[indexOfSquare(row,col)] == null)
  				return false;
  	
  	return true;
  }
}