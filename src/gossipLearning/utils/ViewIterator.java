package gossipLearning.utils;

import java.io.Serializable;
import java.util.Iterator;

/**
 * This is an iterator View class. It does not support the remove operation since the remove modifies the
 * inner structure of the representation.
 * 
 * @author ormandi
 *
 * @param <T>
 */
public class ViewIterator<T extends Serializable & Comparable<? super T>> implements Iterator<T> {
  private final View<T> view;
  private int c = 0;
  
  public ViewIterator(View<T> view) {
    this.view = view;
  }
  
  public boolean hasNext() {
    return c < view.size();
  }
  
  public T next() {
    return view.get(c ++);
  }
  
  public void remove() {
    throw new RuntimeException("Not supported operation!!!");
  }

}
