package gossipLearning.utils;

/**
 * A simple type parametrized version of Java SDK provided Cloneable class.
 * 
 * @author ormandi
 *
 * @param <T>
 */
public interface Cloneable<T> {
  public T clone();
}
