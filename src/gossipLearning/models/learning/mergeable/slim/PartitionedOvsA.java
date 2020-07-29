package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Partitioned;
import gossipLearning.models.learning.mergeable.MergeableOvsA;

public class PartitionedOvsA extends MergeableOvsA implements Partitioned {
  
  public PartitionedOvsA(String prefix) {
    super(prefix);
  }

  public PartitionedOvsA(PartitionedOvsA a) {
    super(a);
  }
  
  protected PartitionedOvsA(PartitionedOvsA a, boolean deep) {
    super(a, deep);
  }

  @Override
  public PartitionedOvsA clone() {
    return new PartitionedOvsA(this);
  }
  
  @Override
  public PartitionedOvsA getModelPart(int p) {
    PartitionedOvsA result = new PartitionedOvsA(this,false);
    result.classifiers.clear();
    for (int i = 0; i < numberOfClasses; i++) {
      result.classifiers.add(((Partitioned)classifiers.getModel(i)).getModelPart(p));
    }
    return result;
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    super.setParameters(numberOfClasses, numberOfFeatures);
    for (int i = 0; i < classifiers.size(); i++) {
      if (!(classifiers.getModel(i) instanceof Partitioned)) {
        throw new RuntimeException("Base models have to implement " + Partitioned.class.getSimpleName() + " interface!!!");
      }
    }
  }
  
}
