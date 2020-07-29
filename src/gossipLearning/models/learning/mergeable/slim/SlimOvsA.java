package gossipLearning.models.learning.mergeable.slim;

import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.models.learning.mergeable.MergeableOvsA;

import java.util.Random;

public class SlimOvsA extends MergeableOvsA implements SlimModel, Partializable {
  private static final long serialVersionUID = 4459146413742898799L;
  public SlimOvsA(String prefix) {
    super(prefix);
  }

  public SlimOvsA(SlimOvsA a) {
    super(a);
  }
  
  protected SlimOvsA(SlimOvsA a, boolean deep) {
    super(a, deep);
  }

  @Override
  public SlimOvsA clone() {
    return new SlimOvsA(this);
  }
  
  @Override
  public Model getModelPart(Random r) {
    SlimOvsA result = new SlimOvsA(this,false);
    result.classifiers.clear();
    for (int i = 0; i < numberOfClasses; i++) {
      Model m = ((Partializable)this.classifiers.getModel(i)).getModelPart(r);
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

  @Override
  public Model weightedAdd(Model model, double times) {
    SlimOvsA m = (SlimOvsA)model;
    age += m.age * times;
    for (int i = 0; i < classifiers.size(); i++) {
      ((SlimModel)classifiers.getModel(i)).weightedAdd(m.classifiers.getModel(i), times);
    }
    return this;
  }
  
  @Override
  public double getSize() {
    return ((SlimModel)classifiers.getModel(0)).getSize();
  }
  
  @Override
  public Model scale(double value) {
    for (int i = 0; i < classifiers.size(); i++) {
      ((SlimModel)classifiers.getModel(i)).scale(value);
    }
    return this;
  }
  
}
