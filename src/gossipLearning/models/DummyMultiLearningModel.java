package gossipLearning.models;

import gossipLearning.interfaces.models.MultiLearningModel;
import gossipLearning.utils.ModelInfo;
import gossipLearning.utils.SparseVector;

public class DummyMultiLearningModel extends MultiLearningModel {
  
  private static final long serialVersionUID = -575565546869213127L;

  
  public DummyMultiLearningModel(String prefix) {
    super(prefix);
  }
  
  public DummyMultiLearningModel(ModelInfo mi){
    super(mi);
  }
  
  public DummyMultiLearningModel(DummyMultiLearningModel dummySoloLearningModel) {
    super(dummySoloLearningModel);
  }

  @Override
  public Object clone() {
    return new DummyMultiLearningModel(this);
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
