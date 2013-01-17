package gossipLearning.interfaces;

import java.io.Serializable;

/**
 * This interface defines the functions that have to be implemented by the used evaluators.
 * 
 * @author István Hegedűs
 */
public interface Evaluator extends Serializable, Cloneable {
  /**
   * Makes a deep copy of the current object.
   * @return the deep copy
   */
  public Object clone();
  /**
   * Computes the evaluation based on the specified expected and predicted values
   * @param expected the expected value
   * @param predicted the predicted value
   */
  public void evaluate(double expected, double predicted);
  /**
   * Merges the specified evaluator to the current object. The specified evaluator 
   * should come from the same class. 
   * @param evaluator to be merged
   */
  public void merge(Evaluator evaluator);
  /**
   * Returns the calculated evaluation results.
   * @return the results
   */
  public double[] getResults();
  /**
   * Returns the name of the calculated results.
   * @return the name of the results
   */
  public String[] getNames();
  /**
   * Clears the collected statistics of the evaluator
   */
  public void clear();
}
