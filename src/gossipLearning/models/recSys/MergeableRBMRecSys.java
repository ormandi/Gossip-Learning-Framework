package gossipLearning.models.recSys;

import gossipLearning.interfaces.Mergeable;

public class MergeableRBMRecSys extends RBMRecSys implements Mergeable<MergeableRBMRecSys>{
  private static final long serialVersionUID = -8030178585030378614L;

  public MergeableRBMRecSys() {
    super();
  }
  
  protected MergeableRBMRecSys(MergeableRBMRecSys a) {
    super(a);
  }
  
  public Object clone() {
    return new MergeableRBMRecSys(this);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public MergeableRBMRecSys merge(MergeableRBMRecSys model) {
    int numClusters = numberOfClusters == 0 ? 1 : numberOfClusters;
    for (int i = 0; i < numClusters; i++) {
      if (!(this.model[i] instanceof Mergeable)) {
        throw new RuntimeException("The inner model (" + model.model.getClass().getCanonicalName() + ") of the RBMRecSys is not mergeable!");
      }
      //itemFreqs.merge(model.itemFreqs);
      ((Mergeable)this.model[i]).merge(model.model[i]);
    }
    return this;
  }

}
