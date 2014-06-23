package chess.util;

import java.util.Iterator;

/**
 * @author Owen Durni (opd@andrew.cmu.edu)
 * 
 * An Iterator that is Iterable over itself.
 * (For use in foreach loops).
 */
public interface Iteratorable<E> extends Iterator<E>, Iterable<E>
{
  //nothing
}
