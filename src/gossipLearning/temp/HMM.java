package gossipLearning.temp;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import cern.colt.Arrays;

public class HMM {
  protected final int n;
  protected double[] pi;
  protected double[][] A;
  protected double[][] B;
  protected final String[] alphabet;
  protected final Map<String, Integer> alphabet2Idx;
  protected final Random r;
  
  protected double[][] alpha;
  protected double[][] beta;
  protected double[][] gamma;
  protected double[][][] xi;
  
  public HMM(int n, String[] alphabet) throws Exception {
    this(n, alphabet, new Random(System.nanoTime()));
  }
  
  public HMM(int n, String[] alphabet, Random r) throws Exception {
    this.n = n;
    pi = new double[n];
    // TODO: final state (n + 1)
    A = new double[n][n];
    B = new double[n][alphabet.length];
    this.alphabet = alphabet;
    this.alphabet2Idx = new TreeMap<String, Integer>();
    for (int i = 0; i < alphabet.length; i++) {
      this.alphabet2Idx.put(alphabet[i], i);
    }
    if (alphabet.length != this.alphabet2Idx.size()) {
      throw new Exception("Repeated alphabet element!");
    }
    this.r = r;
    for (int i = 0; i < n; i++) {
      pi[i] = r.nextDouble();
      for (int j = 0; j < n; j++) {
        A[i][j] = r.nextDouble();
      }
      normalize(A[i]);
      for (int j = 0; j < alphabet.length; j++) {
        B[i][j] = r.nextDouble();
      }
      normalize(B[i]);
    }
    normalize(pi);
    
    alpha = new double[n][1];
    beta = new double[n][1];
    gamma = new double[n][1];
    xi = new double[n][n][1];
  }
  
  public HMM(int n, String[] alphabet, Random r, double[] pi, double[][] A, double[][] B) throws Exception {
    this(n, alphabet, r);
    this.pi = pi;
    this.A = A;
    this.B = B;
  }
  
  public void update(String[] sequence) {
    if (alpha[0].length < sequence.length) {
      alpha = new double[n][sequence.length];
      beta = new double[n][sequence.length];
      gamma = new double[n][sequence.length];
      xi = new double[n][n][sequence.length - 1];
    }
    
    forward(sequence, alpha);
    backward(sequence, beta);
    
    for (int si = 0; si < sequence.length; si++) {
      double sum = 0.0;
      for (int i = 0; i < n; i++) {
        gamma[i][si] = alpha[i][si] * beta[i][si];
        sum += gamma[i][si];
      }
      for (int i = 0; i < n; i++) {
        gamma[i][si] /= sum;
      }
    }
    
    for (int si = 0; si < sequence.length - 1; si++) {
      Integer alphabetIdx = alphabet2Idx.get(sequence[si + 1]);
      if (alphabetIdx == null) {
        throw new RuntimeException(sequence[0] + " can not be found in the aplhabet!");
      }
      double sum = 0.0;
      for (int from = 0; from < n; from++) {
        for (int to = 0; to < n; to++) {
          xi[from][to][si] = alpha[from][si] * A[from][to] * B[to][alphabetIdx] * beta[to][si + 1];
          sum += xi[from][to][si];
        }
      }
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          xi[i][j][si] /= sum;
        }
      }
    }
    
    // update
    for (int i = 0; i < n; i++) {
      pi[i] = gamma[i][0];
      for (int to = 0; to < n; to++) {
        A[i][to] = 0.0;
        double sum = 0.0;
        for (int si = 0; si < sequence.length - 1; si++) {
          A[i][to] += xi[i][to][si];
          sum += gamma[i][si];
        }
        A[i][to] /= sum;
      }
      for (int alphabetIdx = 0; alphabetIdx < alphabet2Idx.size(); alphabetIdx++) {
        double sum = 0.0;
        B[i][alphabetIdx] = 0.0;
        for (int si = 0; si < sequence.length; si++) {
          if (alphabet2Idx.get(sequence[si]) == alphabetIdx) {
            B[i][alphabetIdx] += gamma[i][si];
          }
          sum += gamma[i][si];
        }
        B[i][alphabetIdx] /= sum;
      }
      
    }
  }
  
  private double forward(String[] sequence, double[][] alpha) {
    //double[][] alpha = new double[n][sequence.length];
    Integer alphabetIdx = alphabet2Idx.get(sequence[0]);
    if (alphabetIdx == null) {
      throw new RuntimeException(sequence[0] + " can not be found in the aplhabet!");
    }
    for (int i = 0; i < n; i++) {
      alpha[i][0] = pi[i] * B[i][alphabetIdx];
    }
    
    for (int si = 1; si < sequence.length; si++) {
      alphabetIdx = alphabet2Idx.get(sequence[si]);
      if (alphabetIdx == null) {
        throw new RuntimeException(sequence[si] + " can not be found in the aplhabet!");
      }
      for (int to = 0; to < n; to++) {
        alpha[to][si] = 0.0;
        for (int from = 0; from < n; from++) {
          alpha[to][si] += alpha[from][si - 1] * A[from][to];
        }
        alpha[to][si] *= B[to][alphabetIdx];
      }
    }
    double prob = 0.0;
    for (int i = 0; i < n; i++) {
      prob += alpha[i][sequence.length - 1];
    }
    return prob;
  }
  
  public double probability(String[] sequence) {
    if (alpha[0].length < sequence.length) {
      alpha = new double[n][sequence.length];
      beta = new double[n][sequence.length];
      gamma = new double[n][sequence.length];
      xi = new double[n][n][sequence.length - 1];
    }
    return forward(sequence, alpha);
  }
  
  private void backward(String[] sequence, double[][] beta) {
    //double[][] beta = new double[n][sequence.length];
    for (int i = 0; i < n; i++) {
      beta[i][sequence.length - 1] = 1.0;
    }
    for (int si = sequence.length - 1; si > 0; si--) {
      for (int from = 0; from < n; from++) {
        beta[from][si - 1] = 0.0;
        for (int to = 0; to < n; to++) {
          Integer alphabetIdx = alphabet2Idx.get(sequence[si]);
          if (alphabetIdx == null) {
            throw new RuntimeException(sequence[si] + " can not be found in the aplhabet!");
          }
          beta[from][si - 1] += A[from][to] * beta[to][si] * B[to][alphabetIdx];
        }
      }
    }
  }
  
  public String[] generate(int length) {
    String[] result = new String[length];
    int state = getState(pi, r);
    for (int si = 0; si < length; si++) {
      result[si] = alphabet[getState(B[state], r)];
      state = getState(A[state], r);
    }
    return result;
  }
  
  public int[] decode(String[] sequence) {
    int[] result = new int[sequence.length];
    double[] v = new double[n];
    double[] vtmp = new double[n];
    int[][] b = new int[sequence.length][n];
    Integer alphabetIdx = alphabet2Idx.get(sequence[0]);
    if (alphabetIdx == null) {
      throw new RuntimeException(sequence[0] + " can not be found in the aplhabet!");
    }
    for (int i = 0; i < n; i++) {
      v[i] = Math.log(pi[i]) + Math.log(B[i][alphabetIdx]);
    }
    double max = Double.NEGATIVE_INFINITY;
    int maxIdx = -1;
    for (int si = 1; si < sequence.length; si++) {
      alphabetIdx = alphabet2Idx.get(sequence[si]);
      if (alphabetIdx == null) {
        throw new RuntimeException(sequence[si] + " can not be found in the aplhabet!");
      }
      for (int to = 0; to < n; to++) {
        max = Double.NEGATIVE_INFINITY;
        maxIdx = -1;
        for (int from = 0; from < n; from++) {
          double prob = v[from] + Math.log(A[from][to]) + Math.log(B[to][alphabetIdx]);
          if (max < prob) {
            max = prob;
            maxIdx = from;
          }
        }
        vtmp[to] = max;
        b[si][to] = maxIdx;
      }
      double[] tmp = vtmp;
      vtmp = v;
      v = tmp;
    }
    
    max = Double.NEGATIVE_INFINITY;
    maxIdx = -1;
    for (int i = 0; i < n; i++) {
      if (max < v[i]) {
        max = v[i];
        maxIdx = i;
      }
    }
    result[sequence.length - 1] = maxIdx;
    for (int si = sequence.length - 1; 0 < si; si--){
      maxIdx = b[si][maxIdx];
      result[si - 1] = maxIdx;
    }
    return result;
  }
  
  private int getState(double[] dist, Random r) {
    int result = 0;
    double value = r.nextDouble();
    double sum = dist[result];
    while (sum < value) {
      result ++;
      sum += dist[result];
    }
    return result;
  }
  
  private static void normalize(double[] a) {
    double sum = 0.0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i];
    }
    for (int i = 0; i < a.length; i++) {
      a[i] /= sum;
    }
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("alphabet: " + Arrays.toString(alphabet) + "\n");
    sb.append("pi: " + Arrays.toString(pi) + "\n");
    sb.append("A:\n");
    for (int i = 0; i < n; i++) {
      sb.append(Arrays.toString(A[i]) + "\n");
    }
    sb.append("B:\n");
    for (int i = 0; i < n; i++) {
      sb.append(Arrays.toString(B[i]) + "\n");
    }
    return sb.toString();
  }
  
  public static void main(String[] args) throws Exception {
    int n = 2;
    String[] alphabet = new String[]{"w", "s", "c"};
    double[] pi = new double[]{0.5, 0.5};
    double[][] A = new double[][]{
        {0.7, 0.3}, 
        {0.4, 0.6}};
    double[][] B = new double[][]{
        {0.1, 0.4, 0.5}, 
        {0.6, 0.3, 0.1}
    };
    String[] sequence = new String[]{"w", "s", "c", "c", "w", "w", "w", "c"};
    HMM hmm = new HMM(n, alphabet, new Random(1234567890), pi, A, B);
    //hmm = new HMM(n, alphabet);
    System.out.println(hmm);
    System.out.println("sequence: " + Arrays.toString(sequence));
    System.out.println("seq prob init: " + hmm.probability(sequence));
    for (int i = 0; i < 10000; i++) {
      hmm.update(sequence);
    }
    System.out.println("seq prob: " + hmm.probability(sequence));
    System.out.println("decode: " + Arrays.toString(hmm.decode(sequence)));
    System.out.println("generated: " + Arrays.toString(hmm.generate(sequence.length)));
    System.out.println(hmm);
  }
  
}
