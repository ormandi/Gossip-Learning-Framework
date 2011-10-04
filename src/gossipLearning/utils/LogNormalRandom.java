package gossipLearning.utils;

import java.util.Random;

public class LogNormalRandom {
  private static final long serialVersionUID = -6219774338891757855L;
  private final Random rand;
  private final double mu;
  private final double sigma;
  
  public LogNormalRandom(double mu, double sigma) {
    this(mu, sigma, System.currentTimeMillis());
  }
  
  public LogNormalRandom(double mu, double sigma, long seed) {
    this.mu = mu;
    this.sigma = sigma;
    rand = new Random(seed);
  }
  
  public double nextDouble() {
    double r = mu + sigma * rand.nextGaussian(); // scaled normal
    return Math.pow(Math.E, r);
  }

}
