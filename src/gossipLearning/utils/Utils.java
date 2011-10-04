package gossipLearning.utils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

public class Utils {
  /**
   * It computes the inner product between the two input vectors.
   * 
   * @param x - first vector
   * @param y - second vector
   * @return innerProduct
   */
  public static double innerProduct(final Map<Integer, Double> x, final Map<Integer, Double> y) {
    if (x == null || y == null || x.size() == 0 || y.size() == 0) {
      return 0.0;
    }
    double ret = 0.0;
    for (int id : x.keySet()) {
      if (y.containsKey(id)) {
        ret += x.get(id) * y.get(id);
      }
    }
    return ret;
  }
  
  /**
   * It computes the Cosine similarity between two models which are vectors in sparse representation (map) basically.
   * 
   * @param x - first model
   * @param y - second model
   * @return Cosine similarity between the models. If both of them are 0 vectors, the method returns 1.0. If eighter of them is zero vector or null, it return 0.0.
   */
  public static double computeSimilarity(final Map<Integer, Double> x, final Map<Integer, Double> y) {
    if (x != null && x.size() == 0 && y != null && y.size() == 0) {
      return 1.0;
    } else if (x.size() == 0 || y.size() == 0 || x == null || y == null) {
      return -1.0;
    }
    
    double yN = 0.0, xN = 0.0;
    double innerP = 0.0;
    for (int i : x.keySet()) {
      double xI = x.get(i);
      if (y.containsKey(i)) {
        innerP += xI * y.get(i);
      }
      xN += xI * xI;
    }
    for (int i : y.keySet()) {
      double yI = y.get(i);
      yN += yI * yI;
    }
    return innerP / Math.sqrt(xN * yN);
  }
  
  /**
   * Returns the normalized vector of the specified vector.
   * @param vector - vector to be normalized
   * @return normalized vector
   */
  public static Map<Integer, Double> normalize(final Map<Integer, Double> vector){
    double norm = 0.0;
    for (int i : vector.keySet()){
      norm += vector.get(i) * vector.get(i);
    }
    norm = Math.sqrt(norm);
    Map<Integer, Double> normalized = new TreeMap<Integer, Double>();
    for (int i : vector.keySet()){
      normalized.put(i, vector.get(i) / norm);
    }
    return normalized; 
  }
  
  /**
   * Returns the squared norm of the specified vector.
   * @param vector - vector to get squared norm
   * @return squared norm
   */
  public static double getNorm(final Map<Integer, Double> vector){
    double norm = 0.0;
    for (int i : vector.keySet()){
      norm += vector.get(i) * vector.get(i);
    }
    norm = Math.sqrt(norm);
    return norm;
  }
  
  /**
   * Finds the maximal index of specified vectors.
   * @param a - vector a
   * @param b - vector b
   * @return maximal index
   */
  public static int findMaxIdx(final Map<Integer, Double> a, final Map<Integer, Double> b) {
    int max = - Integer.MAX_VALUE;
    for (int d : a.keySet()) {
      if (d > max) {
        max = d;
      }
    }
    for (int d : b.keySet()) {
      if (d > max) {
        max = d;
      }
    }
    return max;
  }

}
