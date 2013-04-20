package gossipLearning.evaluators;

import gossipLearning.interfaces.Evaluator;

import java.util.Arrays;

/**
 * This abstract class is for supporting that type of evaluator, where 
 * the result is based on a number (i.e. MAE, RMSE, ...).
 * </br></br>
 * This class currently computes the mean, deviation, minimum and maximum values.
 * @author István Hegedűs
 */
public abstract class ValueBasedEvaluator implements Evaluator {
  private static final long serialVersionUID = -3470050470774017007L;
  
  protected double[] values;
  protected String[] names;
  protected double counter;
  protected double numOfMerges;
  
  public ValueBasedEvaluator() {
    values = new double[4];
    names = new String[]{"mean", "dev", "min", "max"};
    clear();
  }
  
  public ValueBasedEvaluator(ValueBasedEvaluator a) {
    values = Arrays.copyOf(a.values, a.values.length);
    names = Arrays.copyOf(a.names, a.names.length);
    counter = a.counter;
    numOfMerges = a.numOfMerges;
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ValueBasedEvaluator)) {
      return false;
    }
    ValueBasedEvaluator e = (ValueBasedEvaluator)o;
    if (counter != e.counter || numOfMerges != e.numOfMerges || values.length != e.values.length || names.length != e.names.length) {
      return false;
    }
    for (int i = 0; i < values.length; i++) {
      if (values[i] != e.values[i] || !names[i].equals(e.names[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public abstract Object clone();
  
  /**
   * Returns the evaluation result based on the specified parameters.
   * @param expected expected value
   * @param predicted predicted value
   * @return evaluated result
   */
  public abstract double getValue(double expected, double predicted);
  
  /**
   * Makes post processing step on the specified value, that should be the 
   * mean of the results of the getValue(...) function
   * @param meanValue mean of the evaluation
   * @return post processed mean
   */
  public abstract double postProcess(double meanValue);
  
  @Override
  public void evaluate(double expected, double predicted) {
    double value = getValue(expected, predicted);
    counter ++;
    values[0] = (1.0 - 1.0 / counter) * values[0] + (1.0 / counter) * value;
    values[1] = (1.0 - 1.0 / counter) * values[1] + (1.0 / counter) * value * value;
    values[2] = Math.min(values[2], value);
    values[3] = Math.max(values[3], value);
  }
  
  @Override
  public void merge(Evaluator evaluator) {
    if (evaluator instanceof ValueBasedEvaluator) {
      numOfMerges ++;
      ValueBasedEvaluator e = (ValueBasedEvaluator)evaluator;
      e.values[0] = e.postProcess(e.values[0]);
      values[0] = (1.0 - 1.0 / numOfMerges) * values[0] + (1.0 / numOfMerges) * e.values[0];
      values[1] = (1.0 - 1.0 / numOfMerges) * values[1] + (1.0 / numOfMerges) * e.values[0] * e.values[0];
      values[2] = Math.min(values[2], e.values[0]);
      values[3] = Math.max(values[3], e.values[0]);
      ((ValueBasedEvaluator) evaluator).clear();
    }
  }

  @Override
  public double[] getResults() {
    values[1] = Math.sqrt(Math.abs(values[1] - values[0] * values[0]));
    if (numOfMerges == 0.0 && counter > 0.0) {
      values[0] = postProcess(values[0]);
    }
    double[] res = Arrays.copyOf(values, values.length);
    clear();
    return res; 
  }
  
  @Override
  public String[] getNames() {
    return names;
  }
  
  @Override
  public final void clear() {
    values[0] = 0.0;
    values[1] = 0.0;
    values[2] = Double.POSITIVE_INFINITY;
    values[3] = Double.NEGATIVE_INFINITY;
    counter = 0.0;
    numOfMerges = 0.0;
  }
  
  @Override
  public String toString() {
    return (numOfMerges == 0.0 ? postProcess(values[0]) : values[0]) + "\t" + Math.sqrt(Math.abs(values[1] - values[0] * values[0])) + "\t" + values[2] + "\t" + values[3];
  }

}
