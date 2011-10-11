package gossipLearning.utils;

import java.io.Serializable;
import java.util.Random;

/**
 * This is a random number generator which produces random numbers according to Poisson distribution.<br/>
 * It can receive parameters:
 * <ul>
 *   <li>The double valued parameter called lambda is the parameter of the distribution (expected value of the event occurrences).
 *   The default value of this parameter is equal to 1.0.</li>
 *   <li>The long valued is the seed</li>
 * </ul>
 * 
 * @author ormandi
 *
 */
public class PoissonRandom implements Serializable {
  private static final long serialVersionUID = 3842367873625083266L;
  private double lambda;          // Parameter of the distribution
  /** @hidden */
  private final Random random;    // Underlying random generator
  
  /**
   * Generates a generator in which the parameter lambda is initialized to 1.0.
   * This can be changed later using the getter and setter methods.  
   */
  public PoissonRandom() {
    lambda = 1.0;
    random = new Random();
  }
  
  /**
   * This constructor creates a generator which is initialized with the given lambda parameter.
   * 
   * @param l value of the lambda parameter of the distribution
   */
  public PoissonRandom(double l) {
    lambda = l;
    random = new Random();
  }
  
  /**
   * It initializes a parameterized random number generator. Where the parameters are the lambda parameter of
   * the distribution and the seed.
   * 
   * @param l value of the lambda parameter of the distribution
   * @param s value of the seed. This valued cannot be changed later.
   */
  public PoissonRandom(double l, long s) {
    lambda = l;
    random = new Random(s);
  }
  
  /**
   * Simply generates the next random number according to a Poisson distribution with parameter lambda.
   * The implementation is similar to that was given by Knuth presented at http://en.wikipedia.org/wiki/Poisson_distribution
   * 
   * @return random number ~ Poisson(lambda)
   */
  public long nextPoisson() {
    double sum = 0.0;
    long rand = -1;
    
    while (sum < lambda) {
        rand ++;
        sum -= Math.log(random.nextDouble());
    }
    
    return rand;
  }
  
  /**
   * It returns the current value of parameter lambda.
   * 
   * @return the current value of parameter lambda
   */
  public double getLambda() {
    return lambda;
  }

  /**
   * It replaces the value of parameter lambda with the given value.
   *  
   * @param lambda new value of parameter lambda
   */
  public void setLambda(double lambda) {
    this.lambda = lambda;
  }
}
