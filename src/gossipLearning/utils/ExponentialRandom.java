package gossipLearning.utils;

import java.io.Serializable;
import java.util.Random;

/**
 * Generates random numbers from Exponential distribution.
 * @author István Hegedűs
 */
public class ExponentialRandom implements Serializable {
  private static final long serialVersionUID = 4739313532381990734L;
  
  private final double lambda;
  private final Random r;
  
  public ExponentialRandom(double lambda) {
    this(lambda, System.currentTimeMillis());
  }
  
  public ExponentialRandom(double lambda, long seed) {
    this.lambda = lambda;
    r = new Random(seed);
  }
  
  public double nextDouble() {
    return -Math.log(r.nextDouble()) / lambda;
  }

}
