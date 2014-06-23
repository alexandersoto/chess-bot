package chess.board;

import chess.util.Creatable;

public interface Move
<
  M extends Move<M>
>
extends Creatable<M>
{    
  /**
   * @return true iff this move is a capture move.
   */
  public boolean isCapture  ();
  /**
   * @return true iff this move promoted a pawn.
   */
  public boolean isPromotion();
  
  /**
   * @return true iff this move is an enpassant.
   */
  public boolean isEnpassant();

  /**
   * @return true iff this move is a castle.
   */
  public boolean isCastle();

  /**
   * @return the file of the source square
   */
  public int     srcRow();    
  /**
   * @return the rank of the source square
   */
  public int     srcCol();    
  /**
   * @return the file of the dest square
   */
  public int     destRow();    
  /**
   * @return the rank of the dest square
   */
  public int     destCol();
  
  /**
   * This method is used to send commands to the server and
   * therefore must follow the form specified below
   * 
   * !!! WARNING !!!
   * This is the format the server accepts from clients,
   * but the server does not emit this format to clients. 
   * !!! WARNING !!!
   * 
   * The string must be:
   *  - The 1st character (a|b|c|d|e|f|g|h) is the src file
   *  - The 2nd character (1|2|3|4|5|6|7|8) is the src rank
   *  - The 3rd character (a|b|c|d|e|f|g|h) is the dest file
   *  - The 4th character (1|2|3|4|5|6|7|8) is the dest rank
   * 
   * **Promotion moves are a special case. Take the length 4 move
   * and append the following:
   * 
   *  - The 5th character (=) a literal
   *  - The 6th character (Q|R|B|N) the piece you are promoting
   *    to in uppercase.
   * 
   * @return a <tt>String</tt> representing this <tt>Move</tt>
   */
  public String  serverString ();
  
  /**
   * Returns the Smith notation for the this move.
   * 
   * @link https://www.chessclub.com/chessviewer/smith.html
   * 
   * @return the Smith notation for this move.
   */
  public String smithString();

  /**
   * Returns the capture piece if it is a capture, null otherwise.
   * @return
   */
  public ArrayPiece getCapture();
}
