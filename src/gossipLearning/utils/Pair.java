package gossipLearning.utils;

/**
 * This class represents a comparable pair of objects, where the objects have to be comparable.<br/><br/>
 * The comparison works as follows: first compares the keys, if these are equals (means 
 * compareTo is 0) returns the comparison value of values, else the keys decide the comparison.
 * @author István Hegedűs
 *
 * @param <K> key class
 * @param <V> value class
 */
public class Pair<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<Pair<K, V>> {

  private K key;
  private V value;
  
  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }
  
  public K getKey() {
    return key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public int compareTo(Pair<K, V> o) {
    int c = key.compareTo(o.key);
    if (c == 0) {
      c = value.compareTo(o.value);
    }
    return c;
  }
  
  public String toString() {
    return "<" + key.toString() + ":" + value.toString() + ">";
  }

}
