package gossipLearning.utils;

import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

public class Utils {
  /**
   * Squared root of 2.
   */
  public static final double SQRT2 = Math.sqrt(2.0);
  /**
   * Squared root of 2 times pi.
   */
  public static final double SQRT2PI = Math.sqrt(2.0 * Math.PI);
  /**
   * Inverse of the natural logarithm of 2.
   */
  public static final double INVLN2 = 1.0 / Math.log(2.0);
  /**
   * Should be used for epsilon.
   */
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
   * Shuffles the specified array using the specified random object.
   * @param r used for shuffling
   * @param array to be shuffled
   */
  public static void arraxShuffle(Random r, Object[] array) {
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
  public static void arrayShuffle(Random r, Object[] array, int from, int to) {
    for (int i=from; i<to; i++) {
      int randomPosition = from + r.nextInt(to - from);
      Object temp = array[i];
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
  public static int[] maximalMatching(double[][] mtx) {
    // FIXME: maximal matching finds minimal matching!
    HungarianMethod hunmeth = new HungarianMethod(mtx);
    return hunmeth.getPermutationArray();
  }
  
  /**
   * Finds the maximal matching of indices based on the specified matrix.
   * This method applies a greedy technique.
   * @param mtx "similarity" matrix
   * @return maximal matching
   */
  public static int[] maximalMatching(Vector<SparseVector> mtx) {
    double[][] tmpMtx = new double[mtx.size()][mtx.size()];
    for (int i = 0; i < mtx.size(); i++) {
      for (int j = 0; j < mtx.size(); j++) {
        // TODO: remove - if maximal matching is fixed
        tmpMtx[i][j] = -mtx.get(i).get(j);
      }
    }
    return maximalMatching(tmpMtx);
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
  
  /**
   * Transforms the specified vector to the frequency space using Discrete 
   * Fast Fourier Transformation (DFFT). The length of the specified vector should 
   * be on power of 2. The vector contains complex numbers as pairs of real 
   * and imaginary parts, so every even elements are the real parts of a complex 
   * number and the corresponding imaginary part is the next one. The output 
   * vector has the same structure.
   * @param x to be transformed
   * @return transformed vector
   */
  public static double[] dfft(double[] x) {
    int N = x.length;

    // base case
    if (N == 2) return new double[]{x[0], x[1]};

    // radix 2 Cooley-Tukey FFT
    if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

    // fft of even terms
    double[] even = new double[N/2];
    for (int k = 0; k < N/4; k++) {
        even[2*k] = x[4*k];
        even[2*k + 1] = x[4*k + 1];
    }
    double[] q = dfft(even);

    // fft of odd terms
    double[] odd  = even;  // reuse the array
    for (int k = 0; k < N/4; k++) {
        odd[2*k] = x[4*k + 2];
        odd[2*k + 1] = x[4*k + 3];
    }
    double[] r = dfft(odd);

    // combine
    double[] y = new double[N];
    for (int k = 0; k < N/4; k++) {
        double kth = -2 * k * Math.PI / (N/2);
        double real = Math.cos(kth);
        double imag = Math.sin(kth);
        
        y[2*k] = q[2*k] + (real*r[2*k] - imag*r[2*k + 1]);
        y[2*k+1] = q[2*k + 1] + (imag*r[2*k] + real*r[2*k + 1]);
        
        y[2*k + N/2] = q[2*k] - (real*r[2*k] - imag*r[2*k + 1]);
        y[2*k + N/2 + 1] = q[2*k + 1] - (imag*r[2*k] + real*r[2*k + 1]);
    }
    return y;
  }
  
  /**
   * Performs the Inverse Discrete Fast Fourier Transformation (iDFFT) on the   
   * specified vector. The length of the specified vector should 
   * be on power of 2. The vector contains complex numbers as pairs of real 
   * and imaginary parts, so every even elements are the real parts of a complex 
   * number and the corresponding imaginary part is the next one. The output 
   * vector has the same structure.
   * @param x to be transformed
   * @return transformed vector
   */
  public static double[] idfft(double[] x) {
    int N = x.length;
    double[] y = new double[N];

    // take conjugate
    for (int i = 0; i < N/2; i++) {
      y[2*i] = x[2*i];
      y[2*i + 1] = -x[2*i + 1];
    }

    // compute forward FFT
    y = dfft(y);

    // take conjugate again
    for (int i = 0; i < N/2; i++) {
      y[2*i + 1] = -y[2*i + 1];
    }

    // divide by N
    for (int i = 0; i < N; i++) {
        y[i] = y[i] / (N/2);
    }

    return y;
  }
  
  private static int waveLength = 2;
  private static double[] coeffs = new double[]{1.0 / Math.sqrt(2.0), -1.0 / Math.sqrt(2.0)};
  private static double[] scales = new double[]{-coeffs[1], coeffs[0]};
  /**
   * Performs the Discrete Haar Wavelet Transformation on the specified vector.
   * The length of the vector should be even. 
   * @param arrTime to be transformed
   * @return transformed vector
   */
  public static double[] dhwt(double[] arrTime) {
    double[] arrHilb = new double[arrTime.length];
    int k = 0;
    int h = arrTime.length >> 1;
    for( int i = 0; i < h; i++ ) {
      for( int j = 0; j < waveLength; j++ ) {
        k = ( i << 1 ) + j;
        while(k >= arrTime.length) {
          k -= arrTime.length;
        }
        // low pass filter - energy (approximation)
        arrHilb[ i ] += arrTime[ k ] * scales[ j ];
        // high pass filter - details
        arrHilb[ i + h ] += arrTime[ k ] * coeffs[ j ]; 
      }
    }
    return arrHilb;
  }
  
  /**
   * Performs the Inverse Discrete Haar Wavelet Transformation on the specified vector.
   * The length of the vector should be even. 
   * @param arrTime to be transformed
   * @return transformed vector
   */
  public static double[] idhwt(double[] arrHilb) {
    double[] arrTime = new double[arrHilb.length];
    int k = 0;
    int h = arrHilb.length >> 1;
    for( int i = 0; i < h; i++ ) {
      for( int j = 0; j < waveLength; j++ ) {
        k = ( i << 1 ) + j;
        while(k >= arrHilb.length) {
          k -= arrHilb.length;
        }
        // adding up details times energy (approximation)
        arrTime[ k ] += ( arrHilb[ i ] * scales[ j ] + arrHilb[ i + h ] * coeffs[ j ] );
      }
    }
    return arrTime;
  }
  
  /**
   * sqrt(x^2 + y^2) without under/overflow.
   */
  public static double hypot(double x, double y) {
    double t;
    x = Math.abs(x);
    y = Math.abs(y);
    t = Math.min(x, y);
    x = Math.max(x, y);
    t = t / x;
    if (x == 0.0) return 0.0;
    return x * Math.sqrt(1 + t * t);
  }

}
