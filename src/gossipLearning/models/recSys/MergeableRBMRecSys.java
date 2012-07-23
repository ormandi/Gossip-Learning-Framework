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
    if (!(this.model instanceof Mergeable)) {
      throw new RuntimeException("The inner model (" + model.model.getClass().getCanonicalName() + ") of the RBMRecSys is not mergeable!");
    }
    itemFreqs.merge(model.itemFreqs);
    ((Mergeable)this.model).merge(model.model);
    return this;
  }

}
