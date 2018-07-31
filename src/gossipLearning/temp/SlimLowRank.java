package gossipLearning.temp;

import gossipLearning.models.factorization.MergeableLowRank;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class SlimLowRank extends MergeableLowRank {
  private static final long serialVersionUID = -5025213718281613599L;
  private static final String PAR_FRAC = "fraction";
  
  protected final double fraction;
  
  public SlimLowRank(String prefix) {
    super(prefix);
    fraction = Configuration.getDouble(prefix + "." + PAR_FRAC);
    if (fraction <= 0.0 || fraction > 1.0) {
      throw new RuntimeException("The value of the parameter " + PAR_FRAC + " should be in (0;1) instead " + fraction);
    }
  }
  
  public SlimLowRank(SlimLowRank a) {
    super(a);
    fraction = a.fraction;
  }
  
  public SlimLowRank(double age, SparseVector[] columnModels, int dimension, double lambda, double alpha, int maxIndex, double fraction) {
    super(age, columnModels, dimension, lambda, alpha, maxIndex);
    this.fraction = fraction;
  }
  
  @Override
  public Object clone() {
    return new SlimLowRank(this);
  }
  
  @Override
  public SlimLowRank getModelPart() {
    return new SlimLowRank(this);
    /*
    double[] norms = new double[origDimension];
    double sum = 0.0;
    for (int i = 0; i < origDimension; i++) {
      SparseVector v = this.columnModels[i];
      norms[i] = v == null ? 0.0 : v.norm();
      sum = Utils.hypot(sum, norms[i]);
      norms[i] *= norms[i];
      if (i > 0) {
        norms[i] += norms[i-1];
      }
    }
    sum *= sum;
    for (int i = 0; i < origDimension; i++) {
      norms[i] /= sum;
    }
    //norms[maxIndex] = 1.0;
    
    //System.out.println((maxIndex + 1) * fraction);
    
    Set<Integer> ind = new TreeSet<Integer>();
    while (ind.size() <= (maxIndex + 1) * fraction) {
      if (sum == 0.0) {
        ind.add(CommonState.r.nextInt(maxIndex + 1));
      } else {
        double rand = CommonState.r.nextDouble();
        int i = 0;
        while (norms[i] < rand) {
          i++;
        }
        ind.add(i);
      }
    }
    int size = 1;
    while (size <= ind.size()) {
      size <<= 1;
    }
    HashMap<Integer, SparseVector> columnModels = new HashMap<Integer, SparseVector>(size, 0.9f);
    for (int index : ind) {
      SparseVector v = this.columnModels.get(index);
      if (v != null) {
        columnModels.put(index, v);
      }
    }
    return new SlimLowRank(age, columnModels, dimension, lambda, alpha, maxIndex, fraction);
    */
    //return this;
  }

}
