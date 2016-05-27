package gossipLearning.models;

import gossipLearning.interfaces.models.SoloLearningModel;
import gossipLearning.utils.SparseVector;

public class DummySoloLearningModel extends SoloLearningModel {
  
  private static final long serialVersionUID = -575565546869213127L;

  
  public DummySoloLearningModel(String prefix) {
    super(prefix);
  }
  
  public DummySoloLearningModel(DummySoloLearningModel dummySoloLearningModel) {
    super(dummySoloLearningModel);
  }

  @Override
  public Object clone() {
    return new DummySoloLearningModel(this);
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    super.update(instance,label);
  }

  @Override
  public double predict(SparseVector instance) {
    return 0;
  }

  @Override
  public int getNumberOfClasses() {
    return 0;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {}

}
