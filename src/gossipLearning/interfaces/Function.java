package gossipLearning.interfaces;

/**
 * Defines an execute method which applies the implemented function for 
 * the specified parameter and returns the result.
 * @author Istvan
 */
public interface Function {
  /**
   * Applies the implemented function for the specified value.
   * @param x function to be applied for
   * @return the result of the function
   */
  public double execute(double x);
}
