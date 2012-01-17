package gossipLearning.utils;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Utils {
  /**
   * It computes the inner product between the two input vectors.
   *
   * @param x first vector
   * @param y second vector
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
   * It computes the Cosine similarity between two models which are vectors in sparse representation (Map).
   * 
   * @param x first model
   * @param y second model
   * @return Cosine similarity between the models. If both of them are 0 vectors, the method returns 1.0. Otherwise if either of them is zero vector or null, it return 0.0.
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
   * @param vector vector to be normalized
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
   * Returns the normalized vector of the specified vector.
   * @param vector vector to be normalized
   * @return normalized vector
   */
  public static double[] normalize(final double[] vector){
    double norm = 0.0;
    for (int i = 0; i < vector.length; i++){
      norm += vector[i] * vector[i];
    }
    norm = Math.sqrt(norm);
    double[] normalized = new double[vector.length];
    if (norm == 0.0) {
      return normalized;
    }
    for (int i = 0; i < vector.length; i++){
      normalized[i] = vector[i] / norm;
    }
    return normalized;
  }

  /**
   * Returns the squared norm of the specified vector.
   * @param vector vector to get squared norm
   * @return squared norm
   */
  public static double getNorm(final Map<Integer, Double> vector){
    double norm = 0.0;
    for (int i : vector.keySet()){
      Double valueD = vector.get(i);
      double value = valueD == null ? Double.NaN : valueD.doubleValue();
      norm += value * value;
    }
    norm = Math.sqrt(norm);
    return norm;
  }

  /**
   * Finds the maximal index of vectors a and b.
   * @param a vector a
   * @param b vector b
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
  
  /**
   * Returns the value of Gauss error function or the cumulative distribution 
   * function (cdf) respect to the parameter z. Uses Taylor series for approximation. 
   * @param z is (x-mu)/(sqrt(2)*sigma), where mu and sigma are the parameters of the
   * Gauss distribution function
   * @param components the number of the Taylor components
   * @return value of the cdf
   */
  public static double erf(double z, int components) {
    double ret = z;
    double delimiter = 1.0;
    double factorial = 1.0;
    double sign = 1.0;
    double cumZ = z;
    double z2 = z*z;
    for (int i = 1; i < components; i++) {
      sign *= -1.0;
      factorial *= i;
      delimiter += 2.0;
      cumZ *= z2;
      ret += sign * (cumZ / (delimiter * factorial));
    }
    ret /= Math.sqrt(Math.PI);
    ret += 0.5;
    return ret;
  }
  
  public static void arraySuffle(Random r, int[] array) {
    arraySuffle(r, array, 0, array.length);
  }
  
  public static void arraySuffle(Random r, int[] array, int from, int to) {
    for (int i=from; i<to; i++) {
      int randomPosition = r.nextInt(to - from);
      int temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }

}
