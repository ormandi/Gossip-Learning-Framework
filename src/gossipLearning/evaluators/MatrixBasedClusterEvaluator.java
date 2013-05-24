package gossipLearning.evaluators;

import gossipLearning.interfaces.Evaluator;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Vector;

/**
 * This class is for supporting that type of evaluator, where 
 * the result is based on a matrix and the task is clustering.<br/>
 * Rows belong to the expected values, and columns belong to the predicted values.
 * <br/><br/>
 * This class currently computes the purity, NMI and RI measurements.
 * <ul>
 * <li><b>purity:</b> each cluster is assigned to the class which is most 
 * frequent in the cluster, and then the accuracy of this assignment is 
 * measured by counting the number of correctly assigned documents and dividing 
 * by the number of documents.</li>
 * <li><b>NMI:</b> Normalized Mutual Information</li>
 * <li><b>RI:</b> Rand Index - accuracy (For computing this measure 
 * we find the best matching between the clusters and classes using the 
 * Hungarian Method).</li>
 * </ul>
 * @see <a href="http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html">Evaluation of clustering<a/>
 * @author István Hegedűs
 */
public class MatrixBasedClusterEvaluator extends MatrixBasedEvaluator {
  private static final long serialVersionUID = -6354546808718608891L;
  private double counter;
  
  public MatrixBasedClusterEvaluator() {
    values = new double[3];
    names = new String[]{"purity", "NMI", "RI"};
    mtx = new Vector<SparseVector>();
    counter = 0.0;
  }
  
  public MatrixBasedClusterEvaluator(MatrixBasedClusterEvaluator a) {
    super(a);
    counter = a.counter;
  }
  
  public Object clone() {
    return new MatrixBasedClusterEvaluator(this);
  }
  
  @Override
  public void merge(Evaluator evaluator) {
    counter++;
    MatrixBasedClusterEvaluator e = (MatrixBasedClusterEvaluator)evaluator;
    double putiry = 0.0, nmi = 0.0, sum = 0.0, max, normNmi = 0.0, ri = 0.0;
    double[] sumCol = new double[e.mtx.size()];
    double[] sumRow = new double[e.mtx.size()];
    /*for (int i = 0; i < mtx.size(); i++) {
      System.out.println(mtx.get(i));
    }*/
    int[] matching = Utils.maximalMatching(e.mtx);
    //System.out.println(Arrays.toString(matching));
    /*for (int i = 0; i < mtx.size(); i++) {
      System.out.println(mtx.get(i));
    }*/
    for (int i = 0; i < e.mtx.size(); i++) {
      max = 0.0;
      for (VectorEntry en : e.mtx.get(i)) {
        sumRow[i] += en.value;
        sumCol[en.index] += en.value;
        sum += en.value;
        if (en.value > max) {
          max = en.value;
        }
        if (matching[i] == en.index) {
          ri += en.value;
        }
      }
      putiry += max;
    }
    if (sum == 0) {
      return;
    }
    putiry /= sum;
    ri /= sum;
    for (int i = 0; i < e.mtx.size(); i++) {
      sumRow[i] /= sum;
      sumCol[i] /= sum;
    }
    for (int i = 0; i < e.mtx.size(); i++) {
      double pi = sumRow[i];
      for (int j = 0; j < e.mtx.size(); j++) {
        double pj = sumCol[j];
        double value = e.mtx.get(i).get(j);
        double pij = (value / sum);
        nmi += (value == 0.0 || pi == 0.0 || pj == 0.0) ? 0.0 : pij * Math.log(pij / (pi * pj))* Utils.INVLN2;
      }
      double pj = sumCol[i];
      double entropyi = pi == 0.0 ? 0.0 : pi * Math.log(pi) * Utils.INVLN2;
      double entropyj = pj == 0.0 ? 0.0 : pj * Math.log(pj) * Utils.INVLN2;
      normNmi -= 0.5 * (entropyi + entropyj);
      e.mtx.get(i).clear();
    }
    nmi /= normNmi;
    values[0] += putiry;
    values[1] += nmi;
    values[2] += ri;
  }
  
  @Override
  public double[] getResults() {
    double[] result = new double[values.length];
    if (counter == 0.0) counter = 1.0;
    for (int i = 0; i < values.length; i++) {
      result[i] = values[i] / counter;
      values[i] = 0.0;
    }
    counter = 0.0;
    return result;
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
    for (int i = 0; i <= mtx.size(); i++) {
      s.append("--------");
    }
    s.append('\n');
    int[] matching = Utils.maximalMatching(mtx);
    for (int i = 0; i < mtx.size(); i++) {
      s.append(i);
      s.append(':');
      for (int j = 0; j < mtx.size(); j++) {
        s.append('\t');
        s.append((int)mtx.get(i).get(matching[j]));
      }
      s.append('\n');
    }
    return s.toString();
  }

}
