package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeableOvsA;

public class SlimOvsA extends MergeableOvsA implements SlimModel {
  private static final long serialVersionUID = 4459146413742898799L;
  public SlimOvsA(String prefix) {
    super(prefix);
  }

  public SlimOvsA(SlimOvsA a) {
    super(a);
  }

  @Override
  public Object clone() {
    return new SlimOvsA(this);
  }
  
  @Override
  public Model getModelPart() {
    SlimOvsA result = new SlimOvsA(this);
    result.classifiers.clear();
    for (int i = 0; i < numberOfClasses; i++) {
      Model m = ((Partializable)this.classifiers.getModel(i)).getModelPart();
      result.classifiers.add(m);
    }
    return result;
  }
  
  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
    super.setParameters(numberOfClasses, numberOfFeatures);
    for (int i = 0; i < classifiers.size(); i++) {
      if (!(classifiers.getModel(i) instanceof SlimModel)) {
        throw new RuntimeException("Base models have to implement " + SlimModel.class.getSimpleName() + " interface!!!");
      }
    }
  }
  
}
