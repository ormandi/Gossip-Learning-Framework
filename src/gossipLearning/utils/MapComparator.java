package gossipLearning.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Compares two maps, that maps integers to doubles. The comparison based on the followings:
 * <ul>
 * <li>The smaller is that has the least key that has not the other</li>
 * <li>If both have the same keys then the smaller is that has the first
 *     smaller value</li>
 * <li>If both have the same keys and the same mapped values then they are equals</li>
 * </ul>
 */
public class MapComparator<K extends Map<Integer, Double>> implements Comparator<Map<Integer, Double>> {

  public int compare(Map<Integer, Double> arg0, Map<Integer, Double> arg1) {
    // get the union of keys
    TreeSet<Integer> keys = new TreeSet<Integer>();
    keys.addAll(arg0.keySet());
    keys.addAll(arg1.keySet());
    
    // comparison
    int compValue;
    for (int key : keys) {
      if (!arg0.containsKey(key)) {
        return 1;
      } else if (!arg1.containsKey(key)) {
        return -1;
      } else {
        compValue = arg0.get(key).compareTo(arg1.get(key));
        if (compValue != 0) {
          return compValue;
        }
      }
    }
    return 0;
  }

}
