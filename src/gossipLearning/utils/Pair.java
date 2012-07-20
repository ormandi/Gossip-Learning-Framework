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
  
  /**
   * Stores the specified key and value.
   * @param key to be stored
   * @param value to be stored
   */
  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }
  
  /**
   * Returns the key value.
   * @return key
   */
  public K getKey() {
    return key;
  }

  /**
   * Stores the specified key.
   * @param key to be stored
   */
  public void setKey(K key) {
    this.key = key;
  }

  /**
   * Returns the value value.
   * @return value
   */
  public V getValue() {
    return value;
  }

  /**
   * Stores the specified value.
   * @param value to be stored
   */
  public void setValue(V value) {
    this.value = value;
  }

  /**
   * If the keys are equal than compares the values.
   */
  public int compareTo(Pair<K, V> o) {
    int c = key.compareTo(o.key);
    if (c == 0) {
      c = value.compareTo(o.value);
    }
    return c;
  }
  
  /**
   * The string representation is "key=value".
   */
  public String toString() {
    return key.toString() + "=" + value.toString();
  }

}
