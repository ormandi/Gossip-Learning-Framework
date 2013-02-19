package gossipLearning.models.bandits;

import gossipLearning.utils.SparseVector;

public abstract class AbstractBanditModel implements BanditModel {
  private static final long serialVersionUID = -9190698187706416829L;
  
  public abstract Object clone();
  
  @Override
  public final void update(SparseVector instance, double label) {
    // update using the global arm model
    update();
  }

  @Override
  public final double predict(SparseVector instance) {
    if (instance != null && instance.size() == 1) {
      Integer armIdxO = instance.iterator().next().index;
      int armIdx = (armIdxO != null) ? armIdxO.intValue() : -1 ;
      return predict(armIdx);
    }
    return Double.NaN;
  }

  @Override
  public final int getNumberOfClasses() {
    return 0;
  }

  @Override
  public final void setNumberOfClasses(int numberOfClasses) {
  }

}
