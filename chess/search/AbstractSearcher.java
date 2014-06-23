package chess.search;

import java.util.Observable;
import java.util.Observer;

import chess.board.Board;
import chess.board.Move;
import chess.evaluation.Evaluator;
import chess.gui.Config;


public abstract class AbstractSearcher
<
  M extends Move<M>,
  B extends Board<M,B>
>
  implements Searcher<M,B>
{
  protected Evaluator<B> evaluator;
  protected Timer        timer;
  protected int          minDepth;
  protected int          maxDepth;
  protected long         leafCount;
  protected long         nodeCount;
  
  private BestMovePublisher<M>
    bestMovePublisher = new BestMovePublisher<M>();

  public void setEvaluator(Evaluator<B> e)
  {
    evaluator = e;
  }

  public void setFixedDepth(int depth)
  {
    setMaxDepth(depth);
    setMinDepth(depth);
  }

  public void setMaxDepth(int depth)
  {
    maxDepth = depth;
  }

  public void setMinDepth(int depth)
  {
    minDepth = depth;
  }

  public void setTimer(Timer t)
  {
    timer = t;
  }

  public long leafCount()
  {
    return leafCount;
  }

  public long nodeCount()
  {
    return nodeCount;
  }
  
  public void addBestMoveObserver(Observer o)
  {
	  
	// Make sure we only have one observer at a time
	bestMovePublisher.deleteObservers();
    bestMovePublisher.addObserver(o);
  }
  
  protected void reportNewBestMove(M move)
  {
    bestMovePublisher.updateBestMove(move);
    
	// Sleep so user can see moves visually as the searcher finds better moves
	try {
		Thread.sleep(Config.delayBetweenBestMoves);
	} catch(InterruptedException ex) {
		Thread.currentThread().interrupt();
	}											
  }
  
  private static class BestMovePublisher
  <
    M extends Move<M>
  > extends Observable
  {
    public void updateBestMove( M move )
    {
      setChanged();
      notifyObservers(move);
    }
  }
}
