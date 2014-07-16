package gossipLearning.evaluators;

import gossipLearning.interfaces.Evaluator;
import gossipLearning.utils.Utils;

import java.util.Arrays;

public class FNorm implements Evaluator {
  private static final long serialVersionUID = 2759907416727840835L;

  protected double[] values;
  protected String[] names;
  protected double counter;
  protected double numOfMerges;
  
  public FNorm() {
    values = new double[1];
    names = new String[]{"FNorm"};
    clear();
  }
  
  public FNorm(FNorm a) {
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
  public Object clone() {
    return new FNorm(this);
  }
  
  @Override
  public void evaluate(double expected, double predicted) {
    values[0] = Utils.hypot(values[0], expected - predicted);
  }

  @Override
  public void merge(Evaluator evaluator) {
    FNorm e = (FNorm)evaluator;
    numOfMerges ++;
    values[0] = Math.hypot(values[0], e.values[0]);
    e.clear();
  }

  @Override
  public double[] getResults() {
    double[] res = Arrays.copyOf(values, values.length);
    clear();
    return res;
  }

  @Override
  public String[] getNames() {
    return names;
  }

  @Override
  public void clear() {
    values[0] = 0.0;
    numOfMerges = 0.0;
    counter = 0.0;
  }
  
  @Override
  public String toString() {
    return "" + values[0];
  }

}
