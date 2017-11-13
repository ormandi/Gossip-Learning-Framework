package gossipLearning.models;

import gossipLearning.interfaces.models.SizedModel;

public class DummySizedModel extends SizedModel {
  
  public DummySizedModel(String prefix) {
    super(prefix);
  }
  
  public DummySizedModel(DummySizedModel sm) {
    super(sm);
  }

  @Override
  public void init(double minModelSize, double maxModelSize) {
    super.init(minModelSize, maxModelSize);
  }
  
  private static final long serialVersionUID = -3374830635052417777L;

  @Override
  public double getAge() {
    return 0;
  }

  @Override
  public Object clone() {
    return new DummySizedModel(this);
  }
  
}
