package chess.tests.internal;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.board.Board;
import chess.evaluation.NoEvaluator;
import chess.search.DFS;
import chess.search.Searcher;

public class DebugMain
{  
  public static void main( String[] args )
  {
    DFS<ArrayMove,ArrayBoard> dfs = new DFS<ArrayMove,ArrayBoard>();
    dfs.setFixedDepth(1);
    dfs.setEvaluator(new NoEvaluator<ArrayBoard>());
    String FEN;
    
    FEN = Board.STARTING_POSITION;
    FEN = "K7/7r/8/2k5/8/8/1p1p4/8 b - -";
    
    ArrayBoard board = ArrayBoard.FACTORY.init(FEN);
    System.out.println(board);
    ArrayMove move = board.createMoveFromString("b7a8Q");
    System.out.println(board.isLegalMove(move));
  }
  
  public static void doMove(String t, ArrayBoard board, Searcher<ArrayMove,ArrayBoard> s)
  {
    ArrayMove move = board.createMoveFromString(t);
    board.applyMove(move);
    s.getBestMove(board, 100000, 100000);
  }
}
