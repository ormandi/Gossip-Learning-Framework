package gossipLearning.temp.main;

import gossipLearning.evaluators.MAError;
import gossipLearning.interfaces.Evaluator;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.Utils;
import gossipLearning.utils.jama.QRDecomposition;

import java.util.Arrays;
import java.util.Random;

public class Ortho {

  public static void main(String[] args) {
    int m = 100;
    int n = 100;
    int dim = 100;
    
    long seed = 1234567890;
    int dimension = 1;
    int numIters = 10000;
    int numEvals = 10;
    double lambda = 1E-5;
    
    int prt = numIters / numEvals;
    Random r = new Random(seed);
    System.err.println("generate");
    //ExponentialRandom er = new ExponentialRandom(2, seed);
    double paretoXm = 1.0;
    double paretoAlpha = 0.5;
    //ParetoRandom er = new ParetoRandom(paretoXm, 0.5, seed);
    double[] arr = new double[dim];
    double sum = 0.0;
    double lookf = 0.0;
    for (int i = 0; i < dim; i++) {
      //arr[i] = er.nextDouble() - paretoXm;
      arr[i] = Utils.nextPareto(paretoXm, paretoAlpha, r) - paretoXm;
      //arr[i] *= arr[i];
    }
    Arrays.sort(arr);
    arr[arr.length-2] = arr[arr.length-1];
    System.out.println(Arrays.toString(arr));
    
    Matrix S = new Matrix(dim, dim);
    for (int i = 0; i < dim; i++) {
      S.set(i, i, arr[dim - i - 1]);
      //System.out.println(i + " " + arr[dim - i - 1]);
      if (i < dimension) {
        lookf += arr[dim - i - 1];
      }
      sum += arr[i];
    }
    System.out.println(lookf / sum);
    //System.exit(0);
    
    Matrix M = new Matrix(m, dim);
    for (int i = 0; i < m; i++) {
      M.set(i, 0, r.nextDouble());
    }
    for (int i = 1; i < dim; i++) {
      M.set(i-1, i, 1.0);
    }
    QRDecomposition qr = new QRDecomposition(M);
    Matrix UST = qr.getQ().mul(S);
    
    M = new Matrix(n, dim);
    for (int i = 0; i < n; i++) {
      M.set(i, 0, r.nextDouble());
    }
    for (int i = 1; i < dim; i++) {
      M.set(i-1, i, 1.0);
    }
    qr = new QRDecomposition(M);
    Matrix VT = qr.getQ().transpose();
    
    M = UST.mul(VT);
    UST.transpose();
    System.err.println("done");
    
    //Matrix STS = new Matrix(S).transpose().mul(S);
    //System.out.println("STS:\n" + STS);
    //Matrix MTM = new Matrix(M).transpose().mul(M);
    //Matrix MMT = M.mul(new Matrix(M).transpose());
    //long time = System.currentTimeMillis();
    //SingularValueDecomposition svd = new SingularValueDecomposition(M);
    //System.out.println("U:\n" + svd.getU());
    //System.out.println("V:\n" + svd.getV());
    //System.out.println("svd time: " + (System.currentTimeMillis() - time));
    //time = System.currentTimeMillis();
    //Lanczos l = new Lanczos();
    //System.out.println(MTM + "\n");
    //Matrix Q = l.run(MTM, Math.max(n, m), r);
    //l.run(MMT, dim+1, r);
    //System.out.println("lanczos time: " + (System.currentTimeMillis() - time));
    //System.out.println(Q.transpose().mul(svd.getU()));
    
    Matrix U = new Matrix(m, dimension, 1.0 / dimension);
    Matrix V = new Matrix(n, dimension, 1.0 / dimension);
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
    
    for (int iter = 0; iter <= numIters; iter ++) {
      if (iter % prt == 0) {
        // evaluate
        R.fill(0.0);
        for (int j = 0; j < dimension; j++) {
          for (int i = 0; i < V.getRowDimension(); i++) {
            R.set(j, j, Utils.hypot(R.get(j, j), V.get(i, j)));
          }
        }
        evaluator.clear();
        Matrix VTV = VT.mul(V);
        Matrix USTU = UST.mul(U.mul(R));
        for (int i = 0; i < dimension; i++) {
          double pred = Math.abs(VTV.get(i, i)) / R.get(i, i);
          //System.out.println("1 " + pred);
          evaluator.evaluate(1.0, pred);
          pred = Math.abs(USTU.get(i, i)) / (S.get(i, i) * S.get(i, i));
          //System.out.println("2 " + pred);
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
      for (int i = 0; i < U.getRowDimension(); i++) {
        for (int j = 0; j < V.getRowDimension(); j++) {
          double value = M.get(i, j);
          for (int d = 0; d < dimension; d++) {
            double pred = U.get(i, d) * V.get(j, d);
            double err = value - pred;
            deltaU.set(i, d, deltaU.get(i, d) + V.get(j, d) * nu * err);
            deltaV.set(j, d, deltaV.get(j, d) + U.get(i, d) * nu * err);
            value -= pred;
          }
        }
      }
      
      // update U and V
      U.addEquals(deltaU);
      V.addEquals(deltaV);
    }
  }

}
