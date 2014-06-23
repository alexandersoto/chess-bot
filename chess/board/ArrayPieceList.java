/**
 * Maintains separate piece lists for every type of non-empty piece.
 */
package chess.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chess.util.Iteratorable;


import static chess.board.ArrayBoard.*;
import static chess.board.ArrayPiece.*;

public class ArrayPieceList
{    
  @SuppressWarnings("unchecked")
  private List<ArrayPiece>[] lists = new ArrayList[16];
  private int                count = 0;

  public ArrayPieceList()
  {
  	for(int i = 0; i < lists.length; ++i)
  	{
  		lists[i] = new ArrayList<ArrayPiece>();
  	}
  }

	/**
   * Adds a piece to the appropriate list.
   * 
   * @param p the piece to add.
   */
  public void add(ArrayPiece p)
  {
    if(p.piece == EMPTY) return;
    
    lists[p.piece].add(p);
    ++count;
  }
    
  /**
   * Returns true if the specified piece is in the lists.
   * 
   * @param p the piece.
   */
  public boolean contains(ArrayPiece p)
  {
  	return lists[p.piece].contains(p);
  }
  
  /**
   * Removes a piece from the appropriate list.
   * 
   * @param p the piece to remove.
   */
  public void remove(ArrayPiece p)
  {
    if(p.piece != EMPTY) {
        lists[p.piece].remove(p);
        count--;
    }
  }
  
  public void clear()
  {
    for( List<ArrayPiece> list : lists )
    {
      list.clear();
    }
  }
  
  /**
   * @return the total number of pieces in all the piece lists.
   */
  public int countOfAllPieces()
  {
  	return count;
  }
  
  /**
   * Counts the number of pieces of the specified type
   * 
   * @param piece the code for the color and type of piece
   * @return the count.
   */
  public int countOfPiece(int piece)
  {
    return lists[piece].size();
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
    return countOfPiece(p.piece);
  }
  
  /**
   * Counts the number of pieces of the specified color.
   * 
   * @param color the color.
   * @return the count.
   */
  public int countOfColor(int color)
  {
    int answer = 0;
    
    for(int type : PIECES_OF_COLOR[color])
    {
      answer += countOfPiece(type);
    }
    
    return answer;
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
    return countOfPiece(makePieceCode(WHITE,type))
         + countOfPiece(makePieceCode(BLACK,type));
  }
  
  @Override
  public int hashCode()
  {
    return Arrays.hashCode(lists);
  }
  
  @Override
  public boolean equals(Object o)
  {
    if(this==o) return true;
    if(!(o instanceof ArrayPieceList)) return false;
    
    ArrayPieceList ps = (ArrayPieceList)o;
    
    if(this.countOfAllPieces() != ps.countOfAllPieces()) return false;
    
    for(ArrayPiece p : iterateOver(ArrayPiece.ALL_PIECES))
    {
    	if(!ps.contains(p)) return false;
    }
    
    return true;
  }
  
  /**
   * Returns an iterable iterator over the pieces in this piece list
   * which match any of the pieces in the specified array.
   * 
   * @param pieces the pieces to iterate over.
   * @return the iterable iterator.
   */
  public Iteratorable<ArrayPiece> iterateOver(int[] pieces)
  {
    return new ArrayPieceListsIterator(pieces);
  }
  
  public class ArrayPieceListsIterator
    implements Iteratorable<ArrayPiece>
  {
    private ArrayPiece           next      = null;
    private Iterator<ArrayPiece> iter;
    private int                  nextIndex = 1;
    private int[]                pieces;
    
    public ArrayPieceListsIterator(int[] pieces)
    {
        this.pieces = pieces;
        this.iter   = lists[pieces[0]].iterator();
        setNext();
    }
    
    public boolean hasNext()
    {
        return next != null;
    }

    public ArrayPiece next()
    {
        ArrayPiece answer = next;
        
        setNext();
        
        return answer;
    }
    
    public void setNext()
    {
        if( iter.hasNext() )
        {
            next = iter.next();
            return;
        }
        
        if( nextIndex < pieces.length )
        {
            iter = lists[pieces[nextIndex++]].iterator();
            setNext();
            return;
        }
        
        next = null;
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Remove not supported");
    }

    public Iterator<ArrayPiece> iterator()
    {
        return this;
    }
  }
}
