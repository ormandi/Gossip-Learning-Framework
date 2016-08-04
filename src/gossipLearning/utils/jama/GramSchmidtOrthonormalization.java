package gossipLearning.utils.jama;

import gossipLearning.utils.Matrix;
import gossipLearning.utils.Utils;

import java.io.Serializable;
import java.util.Random;

public class GramSchmidtOrthonormalization implements Serializable {
  private static final long serialVersionUID = -3941095301916072869L;
  
  private double[][] Q;
  
  public GramSchmidtOrthonormalization(Matrix A) {
    double[][] a = A.getArray();
    Q = new double[A.getRowDimension()][A.getColumnDimension()];
    for (int j = 0; j < A.getColumnDimension(); j++) {
      // q0 = a0/||a0||
      for (int i = 0; i < A.getRowDimension(); i++) {
        Q[i][j] = a[i][j];
      }
      
      // qj = aj - <aj,q0>q0 - ... - <aj,qj-1>qj-1
      for (int jj = 0; jj < j; jj++) {
        double ip = 0.0;
        for (int i = 0; i < A.getRowDimension(); i++) {
          if (jj == 0) {
            ip += a[i][j] * Q[i][jj];
          } else {
            // for more numerical stability
            ip += Q[i][j] * Q[i][jj];
          }
        }
        for (int i = 0; i < A.getRowDimension(); i++) {
          Q[i][j] -= ip * Q[i][jj];
        }
      }
      
      // compute the norm of the jth column
      double norm = 0.0;
      for (int i = 0; i < A.getRowDimension(); i++) {
        norm = Utils.hypot(norm, Q[i][j]);
      }
      for (int i = 0; i < A.getRowDimension(); i++) {
        Q[i][j] /= norm;
      }
    }
  }
  
  public Matrix getQ() {
    return new Matrix(Q);
  }
  
  public static void main(String[] args) {
    Random r = new Random(1234567890);
    int rd = 10000;
    int cd = 200;
    boolean isPrint = false;
    double[][] matrix = new double[rd][cd];
    double[] norms = new double[cd];
    for (int i = 0; i < rd; i++) {
      for (int j = 0; j < cd; j++) {
        matrix[i][j] = r.nextDouble();
        norms[j] = Utils.hypot(norms[j], matrix[i][j]);
      }
    }
    Matrix M = new Matrix(matrix);
    System.out.print("get QR decomposition\t");
    long time = System.currentTimeMillis();
    Matrix Q = new QRDecomposition(M).getQ();
    System.out.println(System.currentTimeMillis() - time);
    System.out.print("get Gram-Schimdt orthonormalizaton\t");
    time = System.currentTimeMillis();
    Matrix Q2 = new GramSchmidtOrthonormalization(M).getQ();
    System.out.println(System.currentTimeMillis() - time);
    
    if (isPrint) {
      Matrix C = new Matrix(Q);
      System.out.println(Q);
      System.out.println(C.transpose().mul(Q) + "\n");
      C = new Matrix(Q2);
      System.out.println(Q2);
      System.out.println(C.transpose().mul(Q2) + "\n");
    }
  }

}
