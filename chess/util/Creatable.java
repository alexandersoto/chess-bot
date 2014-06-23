package chess.util;

/**
 * @author Owen Durni (opd@andrew.cmu.edu)
 *
 * A class which implements Creatable should define
 * a public static member variable called FACTORY and
 * should initialize it as an object of said class.
 * 
 * In this way, anyone can create objects of the
 * specified class without using reflection in a type
 * safe manner by calling ClassName.FACTORY.create()
 * 
 * Additionally, the object returned by create should
 * either be initialized, or the class should define
 * an init(...) method which takes the needed arguments.
 */
public interface Creatable
<
  R extends Creatable<R>
>
{
  /**
   * Returns a new object of the same type as this.
   * 
   * State of the returned object may not be valid.
   * 
   * @return a new object of the same type as this.
   */
  public R    create  ();
  
  /**
   * Returns a deep copy of this.
   * 
   * @return the copy.
   */
  public R    copy    ();
}
