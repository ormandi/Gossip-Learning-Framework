package gossipLearning.utils;

import java.util.Comparator;
import java.util.Map;

public class MapComparator<K extends Map<Integer, Double>> implements Comparator<Map<Integer, Double>> {

  public int compare(Map<Integer, Double> arg0, Map<Integer, Double> arg1) {
    for (int key : arg0.keySet()) {
      if (arg1.containsKey(key)) {
        double comp = arg0.get(key).compareTo(arg1.get(key));
        if (comp > 0) {
          return 1;
        }
        if (comp < 0) {
          return -1;
        }
      } else {
        return 1;
      }
    }
    return 0;
  }

}
