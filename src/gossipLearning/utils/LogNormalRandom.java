package gossipLearning.utils;

import java.io.Serializable;
import java.util.Random;

/**
 * This class can generate log-normal random numbers.
 * 
 * @author István Hegedűs
 */
public class LogNormalRandom implements Serializable {
  private static final long serialVersionUID = -4939915314829563458L;
  /** @hidden */
  private final Random rand;
  private final double mu;
  private final double sigma;
  
  /**
   * Creates a log-normal random generator and sets the specified parameters.
   * The seed will be the System.nanoTime().
   * @param mu mean value
   * @param sigma standard deviation
   */
  public LogNormalRandom(double mu, double sigma) {
    this(mu, sigma, System.nanoTime());
  }
  
  /**
   * Creates a log-normal random generator and sets the specified parameters.
   * The seed will be the System.nanoTime().
   * @param mu mean value
   * @param sigma standard deviation
   * @param seed seed for the random
   */
  public LogNormalRandom(double mu, double sigma, long seed) {
    this.mu = mu;
    this.sigma = sigma;
    rand = new Random(seed);
  }
  
  /**
   * Returns a log-normal random number. <br/>
   * X = exp(mu + sigma * Z), where Z is from the standard normal distribution.
   * @return log-normal random number
   */
  public double nextDouble() {
    double r = mu + sigma * rand.nextGaussian();
    return Math.exp(r);
  }

}
