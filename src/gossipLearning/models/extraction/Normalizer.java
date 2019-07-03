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
    mins = a.mins.clone();
    maxs = a.maxs.clone();
  }
  
  public Normalizer clone() {
    return new Normalizer(this);
  }

  @Override
  public InstanceHolder extract(InstanceHolder instances) {
    maxs.add(mins, -1.0);
    InstanceHolder holder = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
    for (int i = 0; i < instances.size(); i++) {
      SparseVector v = instances.getInstance(i).clone();
      v.add(mins, -1.0);
      v.div(maxs);
      holder.add(v, instances.getLabel(i));
    }
    maxs.add(mins);
    return holder;
  }
  
  public SparseVector extract(SparseVector instance) {
    maxs.add(mins, -1.0);
    SparseVector v = instance.clone();
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
  public void setAge(double age) {
    this.age = age;
  }

  @Override
  public void update(SparseVector instance) {
    for (VectorEntry e : instance) {
      double value = mins.get(e.index);
      if (value == 0.0 || e.value < value) {
        mins.put(e.index, e.value);
      }
      value = maxs.get(e.index);
      if (value == 0.0 || value < e.value) {
        maxs.put(e.index, e.value);
      }
    }
  }
  
  @Override
  public void clear() {
    age = 0.0;
    mins.clear();
    maxs.clear();
  }

}
