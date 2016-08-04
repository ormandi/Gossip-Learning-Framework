package gossipLearning.utils;

public class MRLowRank {
  Matrix U, S, V;
  
  public MRLowRank(Matrix M, int dimension, int iteration, double lambda) {
    U = new Matrix(M.getRowDimension(), dimension, 0.1);
    S = new Matrix(dimension, dimension);
    V = new Matrix(M.getColumnDimension(), dimension, 0.1);
    Matrix deltaU = new Matrix(M.getRowDimension(), dimension);
    Matrix deltaV = new Matrix(M.getColumnDimension(), dimension);
    
    for (int iter = 0; iter <= iteration; iter ++) {
      double nu = lambda / Math.log(iter + 2);
      nu = lambda;
      deltaU.mulEquals(0.0);
      deltaV.mulEquals(0.0);
      for (int i = 0; i < U.getRowDimension(); i++) {
        for (int j = 0; j < V.getRowDimension(); j++) {
          double value = M.get(i,j);
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
      
      // compute error
      deltaU.setMatrix(U);
      Matrix UTU = deltaU.transpose().mul(U);
      deltaU.transpose();
      deltaV.setMatrix(V);
      Matrix VTV = deltaV.transpose().mul(V);
      deltaV.transpose();
      double errorU = 0.0;
      double errorV = 0.0;
      for (int i = 0; i < dimension; i++) {
        for (int j = 0; j < dimension; j++) {
          if (i != j) {
            errorU = Utils.hypot(errorU, UTU.get(i, j));
            errorV = Utils.hypot(errorV, VTV.get(i, j));
          }
        }
      }
      System.err.println(iter + "\t" + errorU + "\t" + errorV);
      if ((errorU + errorV) / 2.0 < 1E-5) {
        break;
      }
    }
    
    for (int j = 0; j < dimension; j++) {
      double norm = 0.0;
      for (int i = 0; i < V.getRowDimension(); i++) {
        norm = Utils.hypot(norm, V.get(i, j));
      }
      for (int i = 0; i < V.getRowDimension(); i++) {
        V.set(i, j, V.get(i, j) / norm);
      }
      S.set(j, j, norm);
      
      norm = 0.0;
      for (int i = 0; i < U.getRowDimension(); i++) {
        norm = Utils.hypot(norm, U.get(i, j));
      }
      for (int i = 0; i < U.getRowDimension(); i++) {
        U.set(i, j, U.get(i, j) / norm);
      }
      S.set(j, j, S.get(j, j) * norm);
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
}
