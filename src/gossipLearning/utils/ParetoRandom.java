package gossipLearning.utils;

import java.io.Serializable;
import java.util.Random;

public class ParetoRandom implements Serializable {
  private static final long serialVersionUID = -5189783894521551718L;
  
  private final Random rand;
  private final double alpha;
  private final double xm;
  
  public ParetoRandom(double alpha, double xm) {
    this(alpha, xm, System.currentTimeMillis());
  }
  
  public ParetoRandom(double alpha, double xm, long seed) {
    this.alpha = alpha;
    this.xm = xm;
    rand = new Random(seed);
  }
  
  public double nextDouble() {
    double r = xm / Math.pow(rand.nextDouble(), 1.0/alpha);
    return r;
  }

}
