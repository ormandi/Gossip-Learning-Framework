package gossipLearning.main.multiMROrtho;

import gossipLearning.utils.Matrix;

public class ButterWorker extends Thread implements Runnable {
  private int from, to, n, rank;
  private double[] theta;
  private Matrix M;
  
  
  
  public ButterWorker(int from, int to, int n, int rank, double[] theta, Matrix M) {
    super();
    this.from = from;
    this.to = to;
    this.n = n;
    this.rank = rank;
    this.theta = theta;
    this.M = M;
  }



  @Override
  public void run() {
    for (int i = from; i < to; i++) {
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
  }
}
