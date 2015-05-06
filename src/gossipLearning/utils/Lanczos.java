package gossipLearning.utils;

import gossipLearning.utils.jama.SingularValueDecomposition;

import java.util.Arrays;
import java.util.Random;

public class Lanczos {
  Matrix U, S, V;
  
  public void run(Matrix A, int k, Random rand) throws Exception {
    run(A, k, rand, 1);
  }
  
  public void run(Matrix A, int k, Random rand, int cores) throws Exception {
    int kk = k + 15;
    Matrix T = new Matrix(kk, kk);
    Matrix r = new Matrix(A.getNumberOfColumns(), 1);
    Matrix Q = new Matrix(A.getNumberOfColumns(), kk);
    for (int i = 0; i < A.getNumberOfColumns(); i++) {
      Q.set(i, 0, rand.nextDouble());
    }
    //for (int rest = 0; rest < 10; rest ++) {
    r = Q.getMatrix(0, A.getNumberOfColumns()-1, 0, 0);
    double beta = r.norm2();
    double alpha;
    Matrix q;
    Matrix Aq;
    Matrix tmp;
    
    // Lanczos iteration
    for (int i = 0; i < kk; i++) {
      r.mulEquals(1.0 / beta);
      q = new Matrix(r);
      Q.setColumn(r.getColumn(0), i);
      // r = A'*A*qi
      Aq = A.mul(q, cores);
      Aq = A.transpose().mul(Aq, cores);
      A.transpose();
      // alpha = qi'*r
      alpha = q.transpose().mul(Aq, cores).get(0, 0);
      q.transpose();
      // r = r - alpha*qi
      r = Aq.subtract(q.mulEquals(alpha));
      if (i != 0) {
        // r = r - beta*qi-1
        r.subtractEquals(q.setColumn(Q.getColumn(i - 1), 0).mulEquals(beta));
        // orthogonalize
        tmp = Q.transpose().mul(r, cores);
        r.subtractEquals(Q.transpose().mul(tmp, cores));
      }
      // beta = norm2(r)
      beta = r.norm2();
      // set alphas and betas in T
      T.set(i, i, alpha);
      if (i < kk-1) {
        T.set(i, i+1, beta);
        T.set(i+1, i, beta);
      }
      // check early stop
      if (Math.abs(beta) < Utils.EPS) {
        //System.err.println("stop: " + i);
        break;
      }
    }
    //}
    
    // compute singular values and left/right singular vectors
    SingularValueDecomposition svd = new SingularValueDecomposition(T);
    S = svd.getS().getMatrix(0, k-1, 0, k-1);
    //double[] s = new double[k];
    for (int i = 0; i < k; i++) {
      //s[i] = Math.sqrt(S.get(i, i));
      //S.set(i, i, 1.0 / s[i]);
      S.set(i, i, Math.sqrt(S.get(i, i)));
    }
    V = Q.mul(svd.getU()).getMatrix(0, A.getColumnDimension() -1, 0, k-1);
    //U = A.mul(V).mul(S);
    U = A.mul(V);
    //System.out.println(U.getColumnDimension() + " " + U.getRowDimension());
    for (int i = 0; i < k; i++) {
      for (int j = 0; j < U.getRowDimension(); j++) {
        U.set(j, i, U.get(j, i) / S.get(i, i));
      }
      //S.set(i, i, s[i]);
    }
  }
  
  public Matrix getU() {
    return U;
  }
  public Matrix getS() {
    return S;
  }
  public Matrix getV() {
    return V;
  }
  
  public static void main(String[] args) throws Exception {
    int n = 2048;
    int m = 2048;
    int rank = 16;
    int eigs = 3;
    double pareto_xm = 1.0;
    double pareto_alpha = 0.5;
    long seed = 1234567890;
    Random r = new Random(seed);
    
    /*Matrix A = Utils.randomBAGraph(n, 2, r);
    System.out.println("decompose");
    SingularValueDecomposition svd = new SingularValueDecomposition(A);
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      System.out.print(i + ":" + svd.getS().get(i, i) + " ");
      sum += svd.getS().get(i, i);
    }
    System.out.println();
    
    double sum2 = 0.0;
    for (int i = 0; i < n; i++) {
      sum2 += svd.getS().get(i, i);
      System.out.print(i + ":" + sum2 / sum + " ");
    }
    System.out.println();
    */
    
    Matrix S = new Matrix(rank, rank);
    //ParetoRandom er = new ParetoRandom(pareto_xm, pareto_alpha, seed);
    double[] arr = new double[rank];
    double[] perc = new double[rank];
    double sum = 0.0;
    for (int i = 0; i < rank; i++) {
      //arr[i] = er.nextDouble() - pareto_xm;
      arr[i] = Utils.nextPareto(pareto_xm, pareto_alpha, r) - pareto_xm;
      sum += arr[i];
    }
    Arrays.sort(arr);
    for (int i = 0; i < (rank + 1) / 2; i++) {
      double tmp = arr[i];
      arr[i] = arr[rank -1 -i];
      arr[rank -1 -i] = tmp;
    }
    double sum2 = 0.0;
    for (int i = 0; i < rank; i++) {
      sum2 += arr[i];
      perc[i] = sum2 / sum;
    }
    System.out.println("#Eigenvalues: " + Arrays.toString(arr));
    System.out.println("#Information: " + Arrays.toString(perc));
    
    for (int i = 0; i < rank; i++) {
      S.set(i, i, arr[i]);
    }
    int cores = Runtime.getRuntime().availableProcessors();
    
    long time = System.currentTimeMillis();
    System.err.print("Construction...\t");
    Matrix U = Utils.butterflyRandomOrthogonalMatrix(n, rank, r);
    Matrix VT = Utils.butterflyRandomOrthogonalMatrix(m, rank, r).transpose();
    Matrix A = U.mul(S, cores).mul(VT, cores);
    System.err.println(System.currentTimeMillis() - time + "ms");
    
    time = System.currentTimeMillis();
    System.err.print("Decomposition...\t");
    Lanczos l = new Lanczos();
    l.run(A, eigs, r, cores);
    //MRLowRank l = new MRLowRank(A, eigs, 5000, 1E-3);
    System.err.println(System.currentTimeMillis() - time + "ms");
    
    time = System.currentTimeMillis();
    System.err.print("Evaluation...\t");
    Matrix UTU = U.transpose().mul(l.getU(), cores);
    Matrix VTV = VT.mul(l.getV(), cores);
    
    //System.out.println("UTU: \n" + UTU);
    //System.out.println("VTV: \n" + VTV);
    Matrix Sl = l.getS();
    double error = 0.0;
    for (int i = 0; i < Math.min(eigs, rank); i++) {
      error += Math.abs(1.0 - Math.abs(UTU.get(i, i)));
      error += Math.abs(1.0 - Math.abs(VTV.get(i, i)));
      error += Math.abs(1.0 - (S.get(i, i) / Sl.get(i, i)));
    }
    error /= 3.0 * eigs;
    System.err.println(System.currentTimeMillis() - time + "ms");
    //System.out.println("UTU:\n" + UTU);
    //System.out.println("VTV:\n" + VTV);
    System.out.println("eigs:\n" + Sl);
    System.out.println("Error: " + error);
  }

}
