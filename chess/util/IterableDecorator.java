package chess.util;

import java.util.Iterator;

/**
 * @author Owen Durni (opd@andrew.cmu.edu)
 * 
 * Wraps an Iterator so that it is Iterable over itself.
 */
public class IterableDecorator<E>
  implements Iteratorable<E>
{
  Iterator<E> iter;
  
  public IterableDecorator(Iterator<E> iter)
  {
    this.iter = iter;
  }

  public boolean hasNext()
  {
    return iter.hasNext();
  }

  public E next()
  {
    return iter.next();
  }

  public void remove()
  {
    iter.remove();
  }

  public Iterator<E> iterator()
  {
    return iter;
  }
}