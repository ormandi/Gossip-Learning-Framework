package gossipLearning.models.gmm;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class GaussianModel extends ProbabilityModel {
  private static final long serialVersionUID = -2123083125764994578L;
  private static final String PAR_K = "K";

  protected GMM[][] models;
  protected final int k;
  protected double[] ages;
  public GaussianModel(String prefix) {
    super(0.0);
    k = Configuration.getInt(prefix + "." + PAR_K);
  }
  
  @Override
  public Object clone() {
    return null;
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    GMM[] m = models[(int)label];
    ages[(int)label] ++;
    int idx = 0;
    for (int i = 0; i < numberOfFeatures; i++) {
      if (idx < instance.size() && i == instance.indexAt(idx)) {
        m[i].update(instance.valueAt(idx));
        idx ++;
      } else {
        m[i].update(0);
      }
    }
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    super.setParameters(numberOfClasses, numberOfFeatures);
    models = new GMM[numberOfClasses][numberOfFeatures];
    ages = new double[numberOfClasses];
    for (int i = 0; i < numberOfClasses; i++) {
      for (int j = 0; j < numberOfFeatures; j++) {
        models[i][j] = new GMM(k, CommonState.r);
      }
    }
  }
  
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double pc, p;
    //double sum = 0.0;
    //double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < numberOfClasses; i++) {
      pc = Math.log(ages[i] / age);
      p = 0;
      for (int j = 0; j < numberOfFeatures; j++) {
        double value = models[i][j].prob(instance.get(j));
        p += Math.log(value <= 0.0 ? Utils.EPS : value);
      }
      distribution[i] = pc + p;
      if (distribution[i] > 0.0 || Double.isNaN(distribution[i]) || Double.isInfinite(distribution[i])) {
        distribution[i] = -1.0/Utils.EPS;
      }
      /*if (distribution[i] > max) {
        max = distribution[i];
      }*/
    }
    /*for (int i = 0; i < distribution.length; i++) {
      distribution[i] -= max;
      distribution[i] = Math.exp(distribution[i]);
      sum += Math.abs(distribution[i]);
    }
    for (int i = 0; i < distribution.length; i++) {
      distribution[i] /= sum;
    }*/
    return distribution;
  }

}
