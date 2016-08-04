package gossipLearning.utils.random;

import java.io.Serializable;
import java.util.Random;

/**
 * Generates random numbers from Pareto distribution.
 * @author István Hegedűs
 */
public class ParetoRandom implements Serializable{
  private static final long serialVersionUID = 6397455829561044885L;
  
  private final double xm;
  private final double alpha;
  private final Random r;
  
  public ParetoRandom(double xm, double alpha) {
    this(xm, alpha, System.currentTimeMillis());
  }
  
  public ParetoRandom(double xm, double alpha, long seed) {
    this.xm = xm;
    this.alpha = alpha;
    r = new Random(seed);
  }
  
  public ParetoRandom(double xm, double alpha, Random r) {
    this.xm = xm;
    this.alpha = alpha;
    this.r = r;
  }
  
  public double nextDouble() {
    return xm / (Math.pow(r.nextDouble(), 1.0 / alpha));
  }
  
  public double nextDouble(double bound) {
    double rnd = r.nextDouble();
    double xma = Math.pow(xm, alpha);
    double bounda = Math.pow(bound, alpha);
    return Math.pow(-(rnd * bounda - rnd * xma - bounda)/(bounda * xma), -1.0/alpha);
  }

}
