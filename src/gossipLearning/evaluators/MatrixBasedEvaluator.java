package gossipLearning.evaluators;

import gossipLearning.interfaces.Evaluator;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

import java.util.Arrays;
import java.util.Vector;

/**
 * This class is for supporting that type of evaluator, where 
 * the result is based on a matrix (i.e. precision, recall, ...).</br>
 * Rows belong to the expected values, and columns belong to the predicted values.
 * </br></br>
 * This class currently computes the accuracy, precision, recall and F1 measures.
 * @author István Hegedűs
 */
public class MatrixBasedEvaluator implements Evaluator {
  private static final long serialVersionUID = -2270711243281944118L;
  
  protected double[] values;
  protected String[] names;
  protected Vector<SparseVector> mtx;
  
  public MatrixBasedEvaluator() {
    values = new double[4];
    names = new String[]{"acc", "prec", "rec", "f1"};
    mtx = new Vector<SparseVector>();
  }
  
  public MatrixBasedEvaluator(MatrixBasedEvaluator a) {
    values = Arrays.copyOf(a.values, a.values.length);
    names = a.names;
    mtx = new Vector<SparseVector>();
    for (int i = 0; i < mtx.size(); i++) {
      mtx.add((SparseVector)a.mtx.get(i).clone());
    }
  }
  
  public Object clone() {
    return new MatrixBasedEvaluator(this);
  }

  @Override
  public void evaluate(double expected, double predicted) {
    if ((int)expected != expected || (int)predicted != predicted) {
      throw new NumberFormatException("Integer numbers are required instead of (" + expected + " - " + predicted + ")");
    }
    double max = Math.max(expected, predicted);
    if (mtx.size() <= max) {
      for (int i = mtx.size(); i <= max; i++) {
        mtx.add(new SparseVector());
      }
    }
    mtx.get((int)expected).add((int)predicted, 1.0);
  }

  @Override
  public void merge(Evaluator evaluator) {
    if (evaluator instanceof MatrixBasedEvaluator) {
      MatrixBasedEvaluator e = (MatrixBasedEvaluator)evaluator;
      for (int i = 0; i < e.mtx.size(); i++) {
        if (mtx.size() == i) {
          mtx.add((SparseVector)e.mtx.get(i).clone());
        } else {
          mtx.get(i).add(e.mtx.get(i));
        }
        e.mtx.get(i).clear();
      }
    }
  }

  @Override
  public double[] getResults() {
    double acc = 0.0, prec = 0.0, rec = 0.0, f1, sum = 0.0, sumRow;
    double[] sumCol = new double[mtx.size()];
    double[] diag = new double[mtx.size()];
    for (int i = 0; i < mtx.size(); i++) {
      sumRow = 0.0;
      for (VectorEntry e : mtx.get(i)) {
        sumRow += e.value;
        sumCol[e.index] += e.value;
        sum += e.value;
        if (i == e.index) {
          acc += e.value;
          diag[i] = e.value;
        }
      }
      double value = mtx.get(i).get(i);
      prec += sumRow == 0.0 ? 0.0 : value / sumRow;
      mtx.get(i).clear();
    }
    for (int i = 0; i < sumCol.length; i++) {
      rec += sumCol[i] == 0.0 ? 0.0 : diag[i] / sumCol[i];
    }
    acc = sum == 0.0 ? 0.0 : acc / sum;
    prec = prec == 0.0 ? 0.0 : prec / mtx.size();
    rec = rec == 0.0 ? 0.0 : rec / mtx.size();
    f1 = (prec + rec) == 0.0 ? 0.0 : (2.0 * prec * rec) / (prec + rec);
    return new double[] {acc, prec, rec, f1};
  }

  @Override
  public String[] getNames() {
    return names;
  }
  
  @Override
  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append("e\\p");
    for (int i = 0; i < mtx.size(); i++) {
      s.append('\t');
      s.append(i);
    }
    s.append('\n');
    for (int i = 0; i < mtx.size(); i++) {
      s.append(i);
      for (int j = 0; j < mtx.size(); j++) {
        s.append('\t');
        s.append((int)mtx.get(i).get(j));
      }
      s.append('\n');
    }
    return s.toString();
  }

}
