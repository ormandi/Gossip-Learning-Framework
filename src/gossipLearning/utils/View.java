package gossipLearning.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * This is a type designed for the &quot;T-Man&quoty; like protocols' view implementation. Basically it is
 * an array-based implementation of a bounded priority queue using generic type parameter for defining the type of the
 * elements.<br/>However the insert operation is O(n) in the size of this collection is restricted to and so this is not a
 * real constraint or drawback.
 * The real advantage of this implementation is the implementation of get operator which works in O(1).
 * 
 * @author Róbert Ormándi
 *
 * @param <T> T is the type of state which has to implement Serializable (for cloning, see details at the description of
 * clone method) and Comparable<? super T>. 
 * Comparable implementation defines the sorting of the elements in the view,
 * but -- it is very important that -- the contains and remove operations work based on the equals method!
 */
public class View <T extends Serializable & Comparable<? super T>> implements Serializable, Cloneable, Iterable<T> {
  private static final long serialVersionUID = 6829986719707246937L;
  private final T[] view;
  private int c = 0;

  /**
   * This is the only constructor of the class. It creates an empty view with bound size length.
   * @param length
   */
  @SuppressWarnings("unchecked")
  public View(int length) {
    Serializable[] tmp = new Serializable[length];
    this.view = (T[]) tmp;
  }

  /**
   * The method creates a deep copy of the current view which can be useful e.g. when we would like to send the
   * view through the network in a message. Basically it applies a simple hack which uses the object serialization
   * framework of the JDK. This is why the generic parameter T has to implement the Serializable interface.
   */
  public Object clone() {
    Object ret = null;
    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(byteOut);
      out.writeObject(this);
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
      ret = in.readObject();
      in.close();
      out.close();
    } catch (Exception e) {
      // it throws out transparently the caught IOException
      throw new RuntimeException(e);
    }
    return ret;
  }

  /**
   * Simply returns the size of the view.
   *
   * @return the number of the element in the view
   */
  public int size() {
    return Math.min(c, view.length);
  }

  /**
   * Returns the <i>i</i>th element of the current view or null if the index <i>i</i> is out of the size range
   * of the view.
   *
   * @param i index of the required element
   * @return <i>i</i>th element element or null if the index is out of the range
   */
  public T get(int i) {
    if (0 <= i && i < c && i < view.length) {
      return (T) view[i];
    }
    return null;
  }

  /**
   * Deletes all of the elements from the view.
   */
  public void clear() {
    c = 0;
    for (int i = 0; i < view.length; i ++) {
      view[i] = null;
    }
  }
  
  /**
   * Inserts the element <i>a</i> to the current view considering the ordering defined by the comparable interface.
   * Returns true if and only if the insertation was successful.
   *
   * @param a element that has to be inserted
   * @return true if the insertation was successful, false otherwise
   */
  public boolean insert(T a) {
    //System.out.println("Inserting " + a + " to view: [" + this.toString() + "]");
    boolean needRepair = false;
    if (view.length > 0) {
      if (a != null && c < view.length) {
        // view is not full => we can simply store the new element
        view[c ++] = a;
        needRepair = true;
      } else if (a != null && (view[view.length - 1] == null || view[view.length - 1].compareTo(a) > 0)) {
        // new element is better than the worst in the current view => we have to store the new one
        view[c-1] = a;
        needRepair = true;
      }
      if (needRepair) {
        // repair the ordering of the view
        for (int i = c - 1; i > 0 && (view[i-1] == null || view[i-1].compareTo(a) > 0); i --) {
          //swap
          T tmp = view[i];
          view[i] = view[i-1];
          view[i-1] = tmp;
        }
      }
    }
    //System.out.println("After insertation, view: [" + this.toString() + "]");
    return needRepair;
  }

  /**
   * Determines whether the element <i>a</i> is already in the view defined by the equal operation of the element.
   * 
   * @param a searched element
   * @return true if the element is in the view, false otherwise
   */
  public boolean contains(T a) {
    for (int i = 0; i < c && i < view.length; i ++) {
      if (view[i] != null && a.equals(view[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Removes the element <i>a</i> from the view based on the equal operator of the element.
   * 
   * @param a - element which has to be removed from the view
   */
  public void remove(T a) {
    for (int i = 0; i < c && i < view.length; i ++) {
      //System.err.println("view["+i+"]="+view[i] + ", a="+a+ ", (view["+i+"]!=null)=" + (view[i] != null) + ", (a.equals(view["+i+"]))=" + (a.equals(view[i])));
      if (view[i] != null && a.equals(view[i])) {
        view[i] = null;
      }
    }
    clearNulls();
  }

  /**
   * The method simply checks if two views are equal or not. Here the equation means that they contain equal number of elements
   * which are all pairwise equal.
   */
  @SuppressWarnings("unchecked")
  public boolean equals(Object otherViewObject) {
    if (otherViewObject != null && otherViewObject instanceof View) {
      View<T> otherView = (View<T>) otherViewObject;
      if (size() == otherView.size()) {
        for (int i = 0; i < size(); i++) {
          if ((get(i) != null || otherView.get(i) != null) && (get(i) == null || otherView.get(i) == null || !get(i).equals(otherView.get(i)))) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Produces a string representation of the view using the toString method of the elements.
   */
  public String toString() {
    StringBuffer buff = new StringBuffer();
    for (int i = 0; i < c && i < view.length; i ++) {
      buff.append(((view[i] != null) ? view[i].toString() :  "null"));
      if (i < c - 1 && i < view.length - 1) {
        buff.append(", ");
      }
    }
    return buff.toString();
  }

  /**
   * Produces an iterator which does not support the remove operation!
   */
  public Iterator<T> iterator() {
    return new ViewIterator<T>(this);
  }

  private void clearNulls() {
    for (int i = 0; i < c && i < view.length; i ++) {
      if (view[i] == null) {
        boolean isFound = false;
        for (int j = i +1; j < c && j < view.length; j ++) {
          if (view[j] != null) {
            isFound = true;
            view[i] = view[j];
            view[j] = null;
            break;
          }
        }
        if (!isFound) {
          c = i;
          break;
        }
      }
    }
  }
}
