package gossipLearning.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

public class Utils {
  public static final double SQRT2 = Math.sqrt(2.0);
  public static final double SQRT2PI = Math.sqrt(2.0 * Math.PI);
  
  private static void polyGen(int d, int n, Stack<Integer> s, Vector<Vector<Integer>> result, boolean generateAll) {
    if ((generateAll || n == 0) && s.size() > 0) {
      Stack<Integer> retS = new Stack<Integer>();
      retS.addAll(s);
      result.add(retS);
    }
    if (n <= 0) {
      return;
    }
    for (int i = (s.size() > 0) ? s.peek() : 0; i < d; i ++) {
      s.push(i);
      polyGen(d, n-1, s, result, generateAll);
      s.pop();
    }
  }
  
  public static Vector<Vector<Integer>> polyGen(int d, int n, boolean generateAll) {
    Vector<Vector<Integer>> result = new Vector<Vector<Integer>>();
    Stack<Integer> stack = new Stack<Integer>();
    polyGen(d, n, stack, result, generateAll);
    return result;
  }
  
  public static boolean isPower2(double t) {
    final long tl = (long) t;
    return (tl & (tl - 1)) == 0;
  }
  
  /**
   * It computes the inner product between the two input vectors.
   *
   * @param x first vector
   * @param y second vector
   * @return innerProduct
   */
  public static double innerProduct(final Map<Integer, Double> x, final Map<Integer, Double> y) {
    /*if (x == null || y == null || x.size() == 0 || y.size() == 0) {
      return 0.0;
    }*/
    Map<Integer, Double> x2, y2;
    if (y.size() < x.size()) {
      x2 = y;
      y2 = x;
    } else {
      x2 = x;
      y2 = y;
    }
    double ret = 0.0;
    Double yval;
    for (Map.Entry<Integer, Double> e : x2.entrySet()) {
      yval = y2.get(e.getKey());
      if (yval != null) {
        ret += e.getValue() * yval;
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
    if (a.size() > 0 && b.size() > 0) {
      return Math.max(((TreeMap<Integer, Double>) a).lastKey(), ((TreeMap<Integer, Double>) b).lastKey());
    }
    int max = Integer.MIN_VALUE;
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
  public static double cdf(double x, double mu, double sigma) {
    return 0.5 * (1.0 + erf((x - mu) / (SQRT2 * sigma)));
  }
  
  /**
   * Returns the value of the erf funcition using Taylor approximation
   * @param z the parameter of the erf
   * @return erf
   */
  public static double erf(double z) {
    double sign = 1.0;
    if (z < 0) {
        sign = -1.0;
    }
    z = Math.abs(z);
    double a1 =  0.254829592;
    double a2 = -0.284496736;
    double a3 =  1.421413741;
    double a4 = -1.453152027;
    double a5 =  1.061405429;
    double p  =  0.3275911;
    
    double t = 1.0 / (1.0 + p * z);
    double y = 1.0 - (((((a5*t + a4)*t) + a3)*t + a2)*t + a1)*t*Math.exp(-z*z);

    return sign * y;
  }
  
  public static void arraySuffle(Random r, int[] array) {
    arraySuffle(r, array, 0, array.length);
  }
  
  public static void arraySuffle(Random r, int[] array, int from, int to) {
    for (int i=from; i<to; i++) {
      int randomPosition = from + r.nextInt(to - from);
      int temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }
  
  public static double[] autoCorrelate(double[] array) {
    double[] result = new double[array.length];
    Arrays.fill(result, 0.0);
    for (int i = 0; i < array.length; i++) {
      for (int j = 0; i + j < array.length; j++) {
        result[j] += array[i] * array[i + j];
      }
    }
    for (int i = array.length -1; i >= 0; i--) {
      result[i] /= result[0];
    }
    return result;
  }
  
  public static double[] autoCorrelate2(double[] array) {
    double[] result = new double[array.length];
    Arrays.fill(result, 0.0);
    for (int i = 0; i < array.length; i++) {
      for (int j = 0; j < array.length; j++) {
        result[j] += array[i] * array[(i + j)%array.length];
      }
    }
    for (int i = array.length -1; i >= 0; i--) {
      result[i] /= result[0];
    }
    return result;
  }
  /*
  public static double[] autoCorrelate(BoundedQueue<Double> array) {
    double[] result = new double[array.size()];
    Arrays.fill(result, 0.0);
    for (int i = 0; i < array.size(); i++) {
      for (int j = 0; i + j < array.size(); j++) {
        result[j] += array.get(i) * array.get(i + j);
      }
    }
    for (int i = array.size() -1; i >= 0; i--) {
      result[i] /= result[0];
    }
    return result;
  }
  
  public static double[] autoCorrelate2(BoundedQueue<Double> array) {
    double[] result = new double[array.size()];
    Arrays.fill(result, 0.0);
    for (int i = 0; i < array.size(); i++) {
      for (int j = 0; j < array.size(); j++) {
        result[j] += array.get(i) * array.get((i + j)%array.size());
      }
    }
    for (int i = array.size() -1; i >= 0; i--) {
      result[i] /= result[0];
    }
    return result;
  }
  */
  public static double computeSimilarity(double[] a, double[] b) {
    if (a.length != b.length) {
      throw new RuntimeException("Parameters have different sizes:" + a.length + " and " + b.length);
    }
    double mul = 0.0;
    double nA = 0.0;
    double nB = 0.0;
    for (int i = 0; i < a.length; i++) {
      mul += a[i] * b[i];
      nA += a[i] * a[i];
      nB += b[i] * b[i];
    }
    if (nA == 0.0 || nB == 0.0) {
      return 0.0;
    }
    return mul / Math.sqrt(nA * nB);
  }
}
