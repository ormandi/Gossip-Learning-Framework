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
    reset();
  }
  
  public ValueBasedEvaluator(ValueBasedEvaluator a) {
    values = Arrays.copyOf(a.values, a.values.length);
    names = a.names;
    counter = a.counter;
    numOfMerges = a.numOfMerges;
  }

  public abstract Object clone();
  
  public abstract double getValue(double expected, double predicted);
  
  public abstract double postProcess(double meanValue);
  
  @Override
  public void evaluate(double expected, double predicted) {
    double value = getValue(expected, predicted);
    values[0] += value;
    counter ++;
  }
  
  @Override
  public void merge(Evaluator evaluator) {
    if (evaluator instanceof ValueBasedEvaluator) {
      ValueBasedEvaluator e = (ValueBasedEvaluator)evaluator;
      e.values[0] /= e.counter;
      e.postProcess(e.values[0]);
      values[0] += e.values[0];
      values[1] += e.values[0] * e.values[0];
      values[2] = Math.min(values[2], e.values[0]);
      values[3] = Math.max(values[3], e.values[0]);
      ((ValueBasedEvaluator) evaluator).reset();
      numOfMerges ++;
    }
  }

  @Override
  public double[] getResults() {
    values[0] /= numOfMerges;
    values[1] = Math.sqrt(values[1] / numOfMerges - values[0] * values[0]);
    if (numOfMerges == 0.0) {
      Arrays.fill(values, 1.0);
    }
    double[] res = Arrays.copyOf(values, values.length);
    reset();
    return res; 
  }
  
  @Override
  public String[] getNames() {
    return names;
  }
  
  private void reset() {
    values[0] = 0.0;
    values[1] = 0.0;
    values[2] = Double.POSITIVE_INFINITY;
    values[3] = Double.NEGATIVE_INFINITY;
    counter = 0.0;
    numOfMerges = 0.0;
  }
  
  @Override
  public String toString() {
    return values[0] + "\t" + values[1] + "\t" + values[2] + "\t" + values[3];
  }

}
