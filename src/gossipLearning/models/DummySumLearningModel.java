package gossipLearning.models;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.SizedModel;
import gossipLearning.utils.SparseVector;

public class DummySumLearningModel extends SizedModel implements LearningModel,Mergeable<DummySumLearningModel>{
  private static final long serialVersionUID = 532238655136262419L;
  private Integer sum; 

  public DummySumLearningModel(String prefix) {
    super(prefix);
    sum=0;
  }
  
  public DummySumLearningModel(DummySumLearningModel sm) {
    super(sm);
    sum=sm.sum;
  }
  
  @Override
  public Object clone() {
    return new DummySumLearningModel(this);
  }
  
  @Override
  public void init(double minModelSize, double maxModelSize) {
    super.init(minModelSize, maxModelSize);
    setSum(0);
  }
  
  @Override
  public double getAge() {
    return 0;
  }
  
  public void setSum(Integer sum) {
    this.sum = sum;
  }
  
  public Integer getSum() {
    return sum;
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    this.sum+=1;
  }

  @Override
  public DummySumLearningModel merge(DummySumLearningModel model) {
    this.sum+=model.getSum();
    return this;
  }
  
  @Override
  public double predict(SparseVector instance) {
    return this.sum;
  }

  @Override
  public int getNumberOfClasses() {
    return 0;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {}



}
