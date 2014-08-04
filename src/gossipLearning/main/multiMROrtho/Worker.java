package gossipLearning.main.multiMROrtho;

import gossipLearning.utils.Matrix;

public class Worker extends Thread implements Runnable {
  private Matrix M, U, V, deltaU, deltaV;
  private int from, to, dimension;
  private double nu;
  
  
  public Worker(Matrix M, Matrix U, Matrix V, Matrix deltaU, Matrix deltaV, int from, int to, int dimension, double nu) {
    super();
    this.M = M;
    this.U = U;
    this.V = V;
    this.deltaU = deltaU;
    this.deltaV = new Matrix(deltaV);
    this.from = from;
    this.to = to;
    this.dimension = dimension;
    this.nu = nu;
  }

  @Override
  public void run() {
    for (int i = from; i < to; i++) {
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
  }
  
  public Matrix getDeltaV() {
    return deltaV;
  }

}
