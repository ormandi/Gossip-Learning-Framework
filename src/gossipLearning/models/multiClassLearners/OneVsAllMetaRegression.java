package gossipLearning.models.multiClassLearners;

import gossipLearning.utils.SparseVector;

public class OneVsAllMetaRegression extends OneVsAllMetaClassifier {
  private static final long serialVersionUID = -7599813741802888264L;
  
  public OneVsAllMetaRegression() {
    super();
  }
  
  public OneVsAllMetaRegression(OneVsAllMetaRegression a) {
    super(a);
  }
  
  public Object clone() {
    return new OneVsAllMetaRegression(this);
  }
  
  public double predict(SparseVector instance) {
    double[] distribution = super.distributionForInstance(instance);
    double min = Double.POSITIVE_INFINITY;
    double sum = 0.0;
    for (int i = 0; i < distribution.length; i++) {
      if (distribution[i] < min) {
        min = distribution[i];
      }
    }
    double pred = 0.0;
    for (int i = 0; i < distribution.length; i++) {
      pred += (distribution[i] - min) * i;
      sum += distribution[i] - min;
    }
    pred /= sum;
    return pred;
  }
}
