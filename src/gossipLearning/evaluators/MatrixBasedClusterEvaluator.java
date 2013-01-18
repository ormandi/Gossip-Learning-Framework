package gossipLearning.evaluators;

import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import gossipLearning.utils.VectorEntry;

import java.util.Vector;

public class MatrixBasedClusterEvaluator extends MatrixBasedEvaluator {
  private static final long serialVersionUID = -6354546808718608891L;
  
  public MatrixBasedClusterEvaluator() {
    values = new double[3];
    names = new String[]{"purity", "NMI", "RI"};
    mtx = new Vector<SparseVector>();
  }
  
  public MatrixBasedClusterEvaluator(MatrixBasedClusterEvaluator a) {
    super(a);
  }
  
  public Object clone() {
    return new MatrixBasedClusterEvaluator(this);
  }
  
  @Override
  public double[] getResults() {
    double putiry = 0.0, nmi = 0.0, sum = 0.0, max, normNmi = 0.0, ri = 0.0;
    double[] sumCol = new double[mtx.size()];
    double[] sumRow = new double[mtx.size()];
    int[] matching = Utils.maximalMatching(mtx);
    for (int i = 0; i < mtx.size(); i++) {
      max = 0.0;
      for (VectorEntry e : mtx.get(i)) {
        sumRow[i] += e.value;
        sumCol[e.index] += e.value;
        sum += e.value;
        if (e.value > max) {
          max = e.value;
        }
        if (matching[i] == e.index) {
          ri += e.value * mtx.size();
        } else {
          ri += e.value * (mtx.size() - 2);
        }
      }
      putiry += max;
    }
    if (sum == 0) {
      return new double[] {0.0, 0.0, 0.0};
    }
    putiry /= sum;
    ri /= sum * mtx.size();
    for (int i = 0; i < mtx.size(); i++) {
      sumRow[i] /= sum;
      sumCol[i] /= sum;
    }
    for (int i = 0; i < mtx.size(); i++) {
      double pi = sumRow[i];
      for (int j = 0; j < mtx.size(); j++) {
        double pj = sumCol[j];
        double value = mtx.get(i).get(j);
        double pij = (value / sum);
        nmi += (value == 0.0 || pi == 0.0 || pj == 0.0) ? 0.0 : pij * Math.log(pij / (pi * pj))* Utils.INVLN2;
      }
      double pj = sumCol[i];
      double entropyi = pi == 0.0 ? 0.0 : pi * Math.log(pi) * Utils.INVLN2;
      double entropyj = pj == 0.0 ? 0.0 : pj * Math.log(pj) * Utils.INVLN2;
      normNmi -= 0.5 * (entropyi + entropyj);
      mtx.get(i).clear();
    }
    nmi /= normNmi;
    // TODO: use better matching for ri
    return new double[] {putiry, nmi, 0.0 * ri};
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
    int[] matching = Utils.maximalMatching(mtx);
    for (int i = 0; i < mtx.size(); i++) {
      s.append(i);
      for (int j = 0; j < mtx.size(); j++) {
        s.append('\t');
        s.append((int)mtx.get(i).get(matching[j]));
      }
      s.append('\n');
    }
    //System.out.println(Arrays.toString(matching));
    return s.toString();
  }

}
