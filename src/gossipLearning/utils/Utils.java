package gossipLearning.utils;

import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

public class Utils {
  public static final double SQRT2 = Math.sqrt(2.0);
  public static final double SQRT2PI = Math.sqrt(2.0 * Math.PI);
  public static final double INVLN2 = 1.0 / Math.log(2.0);
  public static final double EPS = 1E-10;
  
  /**
   * Computes the liner regression line for the values of the specified double array.</br>
   * a * x + b
   * @param array array of values to be approximated
   * @return double[]{a,b}
   */
  public static double[] regression(double[] array) {
    double a = 0.0;
    double b = 0.0;
    double cov = 0.0;
    double sumx = 0.0;
    double sumy = 0.0;
    double sum2x = 0.0;
    for (int i = 0; i < array.length; i++) {
      cov += (i+1)*array[i];
      sumx += (i+1);
      sumy += array[i];
      sum2x += (i+1)*(i+1);
    }
    a = (array.length * cov - (sumx * sumy)) / (array.length * sum2x - (sumx * sumx));
    b = sumy / array.length - a * sumx / array.length;
    return new double[]{a*array.length, b};
  }
  
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
  
  public static InstanceHolder convert(InstanceHolder origSet, Vector<Vector<Integer>> mapping) {
    // TODO: we should optimize this function for sparse vectors!
    // create the new instance set
    InstanceHolder newSet = new InstanceHolder(origSet.getNumberOfClasses(), mapping.size());
    
    for (int i = 0; i < origSet.size(); i++) {
      // get original instance and create mapped one
      SparseVector origInstance = origSet.getInstance(i);
      SparseVector newInstance = new SparseVector(mapping.size());
      
      // for each new dimension
      for (int j = 0; j < mapping.size(); j++) {
        // perform mapping based on the original values
        double newValue = 1.0;
        for (int k = 0; k < mapping.get(j).size(); k ++) {
          newValue *= origInstance.get(mapping.get(j).get(k));
        }
        // store new value of dimension j
        newInstance.put(j, newValue);
      }
      
      // store mapped instance
      newSet.add(newInstance, origSet.getLabel(i));
    }
    
    // return new instance set
    return newSet;
  }
  
  /**
   * Returns true if the specified number is the power of the 2.
   * @param t to be checked
   * @return is power of 2
   */
  public static boolean isPower2(double t) {
    final long tl = (long) t;
    return (tl & (tl - 1)) == 0;
  }

  /**
  * Returns value of the cumulative distribution function (cdf) of the Gaussian 
  * distribution respect to the specified parameters.
  * @param x value be computed at.
  * @param mu the expected value of the the value of Gaussian distribution
  * @param sigma the variance of the the value of Gaussian distribution
  * @return value of the cdf
  */
    public static double cdf(double x, double mu, double sigma) {
      return 0.5 * (1.0 + erf((x - mu) / (SQRT2 * sigma)));
    }
    
    /**
  * Returns the value of the erf function at the specified value using 
  * Taylor series for approximation. The maximum error is 1.5*10^-7.
  * @param z the parameter of the erf function
  * @return the value of the erf function at the specified value
  */
    public static double erf(double z) {
      double sign = 1.0;
      if (z < 0) {
          sign = -1.0;
      }
      z = Math.abs(z);
      double a1 = 0.254829592;
      double a2 = -0.284496736;
      double a3 = 1.421413741;
      double a4 = -1.453152027;
      double a5 = 1.061405429;
      double p = 0.3275911;
      
      double t = 1.0 / (1.0 + p * z);
      double y = 1.0 - (((((a5*t + a4)*t) + a3)*t + a2)*t + a1)*t*Math.exp(-z*z);

      return sign * y;
    }
  
  /**
   * Shuffles the specified array using the specified random object.
   * @param r used for shuffling
   * @param array to be shuffled
   */
  public static void arrayShuffle(Random r, int[] array) {
    arrayShuffle(r, array, 0, array.length);
  }
  
  /**
   * Shuffles the specified array using the specified random object from 
   * the specified position to the spefified position.
   * @param r used for shuffling
   * @param array to be shuffled
   * @param from from index
   * @param to to index
   */
  public static void arrayShuffle(Random r, int[] array, int from, int to) {
    for (int i=from; i<to; i++) {
      int randomPosition = from + r.nextInt(to - from);
      int temp = array[i];
      array[i] = array[randomPosition];
      array[randomPosition] = temp;
    }
  }
  
  /**
   * Finds the maximal matching of indices based on the specified matrix.
   * This method applies a greedy technique.
   * @param mtx "similarity" matrix
   * @return maximal matching
   */
  public static int[] maximalMatching(Vector<SparseVector> mtx) {
    // FIXME: we should use the hungarian method instead of this
    int[] result = new int[mtx.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = i;
    }
    return result;
  }
  
  /**
   * Returns a new array that is the normalized version of the specified vector.
   * @param vector to be normalized.
   * @return the normalized vector
   */
  public static double[] normalize(double[] vector) {
    double[] result = Arrays.copyOf(vector, vector.length);
    double norm = 0.0;
    for (int i = 0; i < result.length; i++) {
      norm += result[i] * result[i];
    }
    norm = Math.sqrt(norm);
    for (int i = 0; i < result.length; i++) {
      result[i] /= norm;
    }
    return result;
  }

}
