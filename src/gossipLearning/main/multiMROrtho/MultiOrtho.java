package gossipLearning.main.multiMROrtho;

import gossipLearning.evaluators.MAError;
import gossipLearning.interfaces.Evaluator;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.Utils;
import gossipLearning.utils.jama.QRDecomposition;

import java.util.Arrays;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;

public class MultiOrtho {

  public static Matrix butterflyrandom(int n, int rank, Random r) throws Exception {
    return butterflyrandom(n, rank, r, 1);
  }
  
  public static Matrix butterflyrandom(int n, int rank, Random r, int numThreads) throws Exception {
    if (!Utils.isPower2(n)) {
      throw new RuntimeException("n should be on power of 2: " + n);
    }
    if (rank > n) {
      throw new RuntimeException("rank should less then or equal to n: " + rank);
    }
    double[] theta = new double[n];
    for (int i = 0; i < n; i++) {
      theta[i] = 2.0 * r.nextDouble() * Math.PI;
    }
    Matrix M = new Matrix(n, rank);
    
    Thread[] worker = new ButterWorker[numThreads];
    for (int i = 0; i < numThreads; i++) {
      int from = i * (n / numThreads);
      int to = (i + 1) * (n / numThreads);
      if (i == numThreads - 1) {
        to = n;
      }
      worker[i] = new ButterWorker(from, to, n, rank, theta, M);
      worker[i].start();
    }
    for (int i = 0; i < numThreads; i++) {
      worker[i].join();
    }
    return M;
  }

  public static Matrix random(int n, int rank, Random r) {
    Matrix M = new Matrix(n, rank);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < rank; j++) {
        M.set(i, j, r.nextDouble());
      }
    }
    QRDecomposition qr = new QRDecomposition(M);
    return qr.getQ();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: MultiOrtho LocalConfig");
      System.exit(0);
    }

    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);

    int numThreads = Configuration.getInt("numThreads");
    Worker[] worker = new Worker[numThreads];

    int m = Configuration.getInt("numRows");
    int n = Configuration.getInt("numColumns");
    int rank = Configuration.getInt("rank");

    long seed = Configuration.getLong("seed");
    int dimension = Configuration.getInt("dimension");
    int numIters = Configuration.getInt("numIters");
    int numEvals = Configuration.getInt("numEvals");
    double lambda = Configuration.getDouble("lambda");

    int prt = numIters / numEvals;
    Random r = new Random(seed);
    System.err.println("generate");
    double paretoXm = 1.0;
    double paretoAlpha = 0.5;
    double[] arr = new double[rank];
    double sum = 0.0;
    double sum2 = 0.0;
    double lookf = 0.0;
    for (int i = 0; i < rank; i++) {
      arr[i] = Utils.nextPareto(paretoXm, paretoAlpha, r) - paretoXm;
    }
    Arrays.sort(arr);
    System.out.println("# " + Arrays.toString(arr));
    double[] perc = new double[rank];
    Matrix S = new Matrix(rank, rank);
    for (int i = 0; i < rank; i++) {
      S.set(i, i, arr[rank - i - 1]);
      if (i < dimension) {
        lookf += arr[rank - i - 1];
      }
      sum += arr[rank - i - 1];
      sum2 = Utils.hypot(sum2, arr[rank - i - 1]);
      perc[rank - i - 1] = sum;
    }
    for (int i = 0; i < rank; i++) {
      perc[i] /= sum;
    }
    System.out.println("# " + Arrays.toString(perc));
    System.out.println("# " + dimension + " - " + lookf / sum);
    
    long time = System.currentTimeMillis();
    Matrix M = butterflyrandom(m, rank, r, numThreads);
    System.err.println("bf time: " + (System.currentTimeMillis() - time));
    time = System.currentTimeMillis();
    Matrix UST = M.mul(S, numThreads);
    
    System.err.println("mul time: " + (System.currentTimeMillis() - time));
    time = System.currentTimeMillis();

    M = butterflyrandom(n, rank, r, numThreads);
    System.err.println("bf time: " + (System.currentTimeMillis() - time));
    time = System.currentTimeMillis();
    Matrix VT = M.transpose();

    M = UST.mul(VT, numThreads);
    System.err.println("mul time: " + (System.currentTimeMillis() - time));
    time = System.currentTimeMillis();
    UST.transpose();
    System.err.println("done");
    
    Matrix U = new Matrix(m, dimension, 1.0 / n);
    Matrix V = new Matrix(n, dimension, 1.0 / n);

    Matrix deltaU = new Matrix(m, dimension);
    Matrix deltaV = new Matrix(n, dimension);
    Matrix R = new Matrix(dimension, dimension);

    Evaluator evaluator = new MAError();

    System.out.print("#iter");
    String[] names = evaluator.getNames();
    for (int i = 0; i < names.length; i++) {
      System.out.print("\t" + names[i]);
    }
    System.out.println();

    for (int iter = 0; iter <= numIters; iter++) {
      if (iter % prt == 0) {
        // evaluate
        R.fill(0.0);
        for (int j = 0; j < dimension; j++) {
          for (int i = 0; i < V.getRowDimension(); i++) {
            R.set(j, j, Utils.hypot(R.get(j, j), V.get(i, j)));
          }
        }
        evaluator.clear();
        Matrix VTV = VT.mul(V, numThreads);
        Matrix USTU = UST.mul(U.mul(R, numThreads), numThreads);
        for (int i = 0; i < dimension; i++) {
          double pred = Math.abs(VTV.get(i, i)) / R.get(i, i);
          //System.out.println("1 " + pred);
          evaluator.evaluate(1.0, pred);
          pred = Math.abs(USTU.get(i, i)) / (S.get(i, i) * S.get(i, i));
          //System.out.println("2 " + pred);
          //System.out.println("R: " + R);
          evaluator.evaluate(1.0, pred);
        }
        System.out.print(iter);
        double[] results = evaluator.getResults();
        for (int i = 0; i < results.length; i++) {
          System.out.format("\t%.6f", results[i]);
        }
        System.out.println();
      }
      // compute delta
      double nu = lambda / Math.log(iter + 2);
      deltaU.fill(0.0);
      deltaV.fill(0.0);

      for (int i = 0; i < numThreads; i++) {
        int from = i * (U.getRowDimension() / numThreads);
        int to = (i + 1) * (U.getRowDimension() / numThreads);
        if (i == numThreads - 1) {
          to = U.getRowDimension();
        }
        worker[i] = new Worker(M, U, V, deltaU, deltaV, from, to, dimension, nu);
        worker[i].start();
      }
      for (int i = 0; i < numThreads; i++) {
        worker[i].join();
      }

      // update U and V
      U.addEquals(deltaU);
      for (int i = 0; i < numThreads; i++) {
        V.addEquals(worker[i].getDeltaV());
      }
    }
  }

}
