package gossipLearning.models.extraction;

import gossipLearning.interfaces.models.FeatureExtractorModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.VectorEntry;

public class Normalizer implements FeatureExtractorModel {
  private static final long serialVersionUID = 1378448923909137195L;
  
  protected SparseVector mins;
  protected SparseVector maxs;
  protected double age;
  
  public Normalizer(String prefix) {
    age = 0.0;
    mins = new SparseVector();
    maxs = new SparseVector();
  }
  
  public Normalizer(Normalizer a) {
    age = a.age;
    mins = (SparseVector)a.mins.clone();
    maxs = (SparseVector)a.maxs.clone();
  }
  
  public Object clone() {
    return new Normalizer(this);
  }

  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    maxs.add(mins, -1.0);
    InstanceHolder holder = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
    for (int i = 0; i < instances.size(); i++) {
      SparseVector v = (SparseVector)instances.getInstance(i).clone();
      v.add(mins, -1.0);
      v.div(maxs);
      holder.add(v, instances.getLabel(i));
    }
    maxs.add(mins);
    return holder;
  }
  
  public SparseVector extract(SparseVector instance) {
    maxs.add(mins, -1.0);
    SparseVector v = (SparseVector)instance.clone();
    v.add(mins, -1.0);
    v.div(maxs);
    maxs.add(mins);
    return v;
  }

  @Override
  public double getAge() {
    return age;
  }

  @Override
  public void update(SparseVector instance,double label) {
    for (VectorEntry e : instance) {
      if (e.value < mins.get(e.index)) {
        mins.put(e.index, e.value);
      }
      if (maxs.get(e.index) < e.value) {
        maxs.put(e.index, e.value);
      }
    }
  }

  @Override
  public FeatureExtractorModel merge(FeatureExtractorModel model) {
    // TODO Auto-generated method stub
    return this;
  }

}
