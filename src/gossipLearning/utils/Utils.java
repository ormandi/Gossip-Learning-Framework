package gossipLearning.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
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
   * a * x + b </br>
   * The slope variable (a) is multiplied by the length of the input vector.
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
   * Shuffles the specified array using the specified random object between 
   * the specified positions.
   * @param r used for shuffling
   * @param array to be shuffled
   * @param from from index (inclusive)
   * @param to to index (exclusive)
   */
  public static void arrayShuffle(Random r, int[] array, int from, int to) {
    for (int i=from; i<to; i++) {
      int randomPosition = i + r.nextInt(to - i);
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
  public static void arrayShuffle(Random r, Object[] array) {
    arrayShuffle(r, array, 0, array.length);
  }
  
  /**
   * Shuffles the specified array using the specified random object from 
   * the specified position to the spefified position.
   * @param r used for shuffling
   * @param array to be shuffled
   * @param from from index (inclusive)
   * @param to to index (exclusive)
   */
  public static void arrayShuffle(Random r, Object[] array, int from, int to) {
    for (int i=from; i<to; i++) {
      int randomPosition = i + r.nextInt(to - i);
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
  
  /**
   * Generates a random column orthogonal matrix with n rows and rank columns. <br/>
   * Q<sup>(2n)</sup> = |Q<sup>(n)</sup>c<sub>n</sub> -Q<sup>(n)</sup>s<sub>n</sub>| <br/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   *                    |Q&#770;<sup>(n)</sup>s<sub>n</sub> &nbsp; Q&#770;<sup>(n)</sup>c<sub>n</sub>| <br/>
   * where <ul> 
   *  <li>c<sub>i</sub> = cos(&#952;<sub>i</sub>)</li>
   *  <li>s<sub>i</sub> = sin(&#952;<sub>i</sub>)</li>
   *  <li>Q&#770;<sup>(n)</sup> has the same form as Q<sup>(n)</sup> except that c<sub>i</sub>
   * and s<sub>i</sub> indices are increased by n</li>
   *  <li>Q<sup>(1)</sup> = [1]</li>
   *  <li>&#952;<sub>i</sub> is a random angle in [0;2&#960;] radian.</li>
   * </ul>
   * @param n number of rows (should be on power of 2)
   * @param rank number of columns (should not be grater than n)
   * @param r random number generator
   * @return column orthogonal matrix with the specified rank
   */
  public static Matrix butterflyRandomOrthogonalMatrix(int n, int rank, Random r) {
    if (!Utils.isPower2(n)) {
      throw new RuntimeException("n should be on power of 2: " + n);
    }
    if (rank > n) {
      throw new RuntimeException("rank should less than or equal to n: " + rank);
    }
    double[] theta = new double[n];
    for (int i = 0; i < n; i++) {
      theta[i] = 2.0 * r.nextDouble() * Math.PI;
    }
    Matrix M = new Matrix(n, rank);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < rank; j++) {
        double value = 1.0;
        int indexi = i;
        int indexj = j;
        int t = 0;
        for (int power = n >> 1; power > 0; power >>= 1) {
          //System.out.println(power + " " + t);
          if (indexi < power && indexj < power) {
            //System.out.print("c" + (power + t));
            value *= Math.cos(theta[power + t]);
          } else if (indexi < power && indexj >= power) {
            //System.out.print("-s" + (power + t));
            value *= -Math.sin(theta[power + t]);
            indexj %= power;
          } else if (indexi >= power && indexj < power) {
            //System.out.print("s" + (power + t));
            value *= Math.sin(theta[power + t]);
            indexi %= power;
            t = power;
          } else if (indexi >= power && indexj >= power) {
            //System.out.print("c" + (power + t));
            value *= Math.cos(theta[power + t]);
            indexi %= power;
            indexj %= power;
            t = power;
          }
        }
        M.set(i, j, value);
        //System.out.print("\t");
      }
    }
    return M;
  }
  
  public static Matrix randomKOutGraph(int n, int k, Random r) {
    Matrix M = new Matrix(n, n);
    HashSet<Integer> set = new HashSet<Integer>();
    for (int i = 0; i < n; i++) {
      while (set.size() < k) {
        set.add(r.nextInt(n));
      }
      for (int j : set) {
        M.set(i, j, 1.0);
      }
    }
    return M;
  }
  
  public static Matrix randomBAGraph(int n, int m, Random r) {
    if (n <= m) {
      throw new RuntimeException("number of nodes should be greater than the number of initial nodes:  " + n + " - " + m);
    }
    if (m < 2) {
      throw new RuntimeException("initial number of nodes should be at least 2: " + m);
    }
    Matrix M = new Matrix(n, n);
    double[] degrees = new double[n];
    double numEdges = 0.0;
    
    // initializing the network
    for (int i = 0; i < m; i++) {
      for (int j = i + 1; j < m; j++) {
        M.set(i, j, 1.0);
        M.set(j, i, 1.0);
        degrees[i] ++;
        degrees[j] ++;
        numEdges += 2.0;
      }
    }
    
    // add node i to the network
    for (int i = m; i < n; i++) {
      // add m edges to node i
      for (int k = 0; k < m; k++) {
        double rnd = r.nextDouble();
        double prob = 0.0;
        // try to add edge to node j
        for (int j = 0; j < i; j++) {
          prob += degrees[j] / numEdges;
          if (rnd <= prob && M.get(i, j) == 0.0) {
            M.set(i, j, 1.0);
            M.set(j, i, 1.0);
            degrees[i] ++;
            degrees[j] ++;
            numEdges += 2.0;
          }
        }
      }
    }
    /*for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        M.set(i, j, M.get(i, j) / degrees[i]);
      }
    }*/
    return M;
  }
  
  /**
   * Returns a random number generated from normal distribution using 
   * the specified parameters.
   * @param mu
   * @param sigma
   * @param r
   * @return mu + sigma * r.nextGaussian()
   */
  public static double nextNormal(double mu, double sigma, Random r) {
    return mu + sigma * r.nextGaussian();
  }
  
  /**
   * Returns a random number generated from log-normal distribution using 
   * the specified parameters.
   * @param mu
   * @param sigma
   * @param r
   * @return Math.exp(nextNormal(mu, sigma, r))
   */
  public static double nextLogNormal(double mu, double sigma, Random r) {
    return Math.exp(nextNormal(mu, sigma, r));
  }
  
  /**
   * Returns a random number generated from exponential distribution using 
   * the specified parameters.
   * @param lambda
   * @param r
   * @return -Math.log(r.nextDouble()) / lambda
   */
  public static double nextExponential(double lambda, Random r) {
    return -Math.log(r.nextDouble()) / lambda;
  }
  
  /**
   * Returns a random number generated from pareto distribution using 
   * the specified parameters.
   * @param xm
   * @param alpha
   * @param r
   * @return xm / (Math.pow(r.nextDouble(), 1.0 / alpha))
   */
  public static double nextPareto(double xm, double alpha, Random r) {
    return xm / (Math.pow(r.nextDouble(), 1.0 / alpha));
  }
  
  public static double nextPareto(double xm, double alpha, double bound, Random r) {
    double rnd = r.nextDouble();
    double xma = Math.pow(xm, alpha);
    double bounda = Math.pow(bound, alpha);
    return Math.pow(-(rnd * bounda - rnd * xma - bounda)/(bounda * xma), -1.0/alpha);
  }
  
  public static double nextLaplace(double mu, double b, Random r) {
    double u = r.nextDouble() - 0.5;
    if (u > 0.0) {
      return mu - b*Math.log(1 - 2*u);
    } else {
      return mu + b*Math.log(1 + 2*u);
    }
  }
  
  /**
   * Returns a random number generated from gamma distribution using 
   * the specified parameters.
   * @param n
   * @param r
   * @return
   */
  public static double nextGamma(double n, Random r) {
    double result = 0.0;
    for (int i = 0; i < n; i++) {
      result -= Math.log(r.nextDouble());
    }
    return result;
  }
  
  /**
   * Returns a random number generated from gamma distribution using 
   * the specified parameters.
   * @param n
   * @param r
   * @return
   */
  public static double nextGammaFast(double n, Random r) {
    final double d = n - 0.333333333333333333;
    final double c = 1 / (3 * Math.sqrt(d));
    while (true) {
      final double x = r.nextGaussian();
      final double v = (1 + c * x) * (1 + c * x) * (1 + c * x);
      if (v <= 0) {
        continue;
      }
      final double x2 = x * x;
      final double u = r.nextDouble();
      // Squeeze
      if (u < 1 - 0.0331 * x2 * x2) {
        return d * v;
      }
      if (Math.log(u) < 0.5 * x2 + d * (1 - v + Math.log(v))) {
        return d * v;
      }
    }
  }
  
  /**
   * Returns a random number generated from beta distribution using 
   * the specified parameters.
   * @param alpha
   * @param beta
   * @param r
   * @return
   */
  public static double nextBeta(double alpha, double beta, Random r) {
    double x = nextGamma(alpha, r);
    double y = nextGamma(beta, r);
    return x / (x + y);
  }
  
  /**
   * Returns a random number generated from beta distribution using 
   * the specified parameters.
   * @param alpha
   * @param beta
   * @param r
   * @return
   */
  public static double nextBetaFast(double alpha, double beta, Random r) {
    double x = nextGammaFast(alpha, r);
    double y = nextGammaFast(beta, r);
    return x / (x + y);
  }
  
  public static double scaleValueRange(double value, int nbits, Random r) {
    double correction = r == null ? 0.0 : r.nextDouble() - 0.5;
    assert value <= 1.0 && value >= -1.0;
    double scale = (1 << nbits - 1);
    return Math.round((value * scale) + correction) / scale;
  }
  
  public static double[] getRandomVector(int d, Random r) {
    double[] vector = new double[d];
    double length = 0.0;
    for (int i = 0; i < d; i++) {
      vector[i] = r.nextGaussian();
      length = hypot(length, vector[i]);
    }
    for (int i = 0; i < d; i++) {
      vector[i] /= length;
    }
    return vector;
  }
  
  private static double[] KSTest(double[] a, double[] b) {
    double[] ca = a.clone();
    double[] cb = b.clone();
    Arrays.sort(ca);
    Arrays.sort(cb);
    double ia = 1.0 / ca.length;
    double ib = 1.0 / cb.length;
    double max = 0.0;
    double val;
    for (int i = 0; i < ca.length; i++) {
      val = Math.abs((i*ia) - (Math.abs(getIdx(cb, ca[i]))*ib));
      if (val > max) {
        max = val;
      }
    }
    double c_a = max / Math.sqrt((a.length + b.length) / (double)(a.length * b.length));
    return new double[]{max, c_a};
  }
  
  private static int getIdx(double[] a, double value) {
    int first = 0;
    int last = a.length - 1;
    while(first <= last) {
      int med = (first + last) >>> 1;
      if (a[med] < value) {
        first = med + 1;
      } else if (a[med] > value) {
        last = med - 1;
      } else {
        return med;
      }
    }
    return -(first + 1);
  }
  
  public static Matrix randomProjectionMatrix(int n, int m, Random r, boolean isSparse) {
    Matrix R = new Matrix(n, m);
    for (int i = 0; i < R.getNumberOfRows(); i++) {
      double norm = 0.0;
      // generate random row
      for (int j = 0; j < R.getNumberOfColumns(); j++) {
        double rand = r.nextDouble();
        R.set(i, j, isSparse ? rand < 1.0/3.0 ? rand < 1.0/6.0 ? -1 : 1 : 0 : rand - 0.5);
        norm = Utils.hypot(norm, R.get(i, j));
      }
      // normalize row to has unit length
      for (int j = 0; j < R.getNumberOfColumns(); j++) {
        R.set(i, j, R.get(i, j) / norm);
      }
    }
    return R;
  }
  
  public static long getSeed() {
    return System.nanoTime() % (long)1E9;
  }
  
  public static void main(String[] args) {
    Random r = new Random(System.currentTimeMillis());
    int size = 100;
    double[] a = new double[size];
    double[] b = new double[size];
    for (int i = 0; i < size; i++) {
      a[i] = nextGamma(5, r);
      b[i] = nextGammaFast(5, r);
      //a[i] = nextNormal(0.0, 2.0, r);
      //b[i] = nextNormal(0.0, 1.0, r);
    }
    System.out.print("[" + a[0]);
    for (int i = 1; i < a.length; i++) {
      System.out.print(" " + a[i]);
    }
    System.out.println("]");
    
    System.out.print("[" + b[0]);
    for (int i = 1; i < b.length; i++) {
      System.out.print(" " + b[i]);
    }
    System.out.println("]");
    
    System.out.println(Arrays.toString(KSTest(a, b)));
    
    double[] arr = new double[]{1,3,5,5,3,2,1,4,4,-1};
    r = new Random(Utils.getSeed());
    size = 10;
    arr = new double[size];
    for (int i = 0; i < size; i++) {
      arr[i] = r.nextDouble();
    }
    
    System.out.println(Arrays.toString(arr));
    int[] res = IndexSort.sort(arr);
    System.out.println("ISort: " + Arrays.toString(res));
    
  }
  
  /**
   * 
   * @param k number of labels
   * @param n number of nodes
   * @param c number of different labels per node
   * @return mapping
   */
  public static LinkedList<Integer>[] mapLabelsToNodes(int k, int n, int c) {
    if (k < c || c == 0) {
      System.err.println("|--WARNING: can not be set " + c + " different labels from " + k + " different classes for a node. So " + k + " different will be set.");
      c = k;
    }
    if (c * n < k) {
      System.err.print("|--WARNING: can not be set " + c + " different labels from " + k + " different classes for a node of " + n + " nodes. ");
      c = (int)Math.ceil(k / (double)n);
      System.err.println("So " + c + " different will be set.");
    }
    @SuppressWarnings("unchecked")
    LinkedList<Integer>[] map = new LinkedList[k];
    for (int i = 0; i < k; i++) {
      map[i] = new LinkedList<Integer>();
    }
    int ki = 0;
    for (int ni = 0; ni < n; ni++) {
      for (int ci = 0; ci < c; ci++) {
        map[ki].add(ni);
        ki = (ki + 1) % k;
      }
    }
    return map;
  }

}
