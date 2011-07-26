package gossipLearning.utils;

public abstract class Cloneable<T> implements java.lang.Cloneable {
  public Object clone() {
    return genericClone();
  }
  protected abstract T genericClone();
}
