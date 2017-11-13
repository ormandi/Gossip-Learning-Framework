package gossipLearning.models;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Mergeable;
import gossipLearning.interfaces.models.SizedModel;
import gossipLearning.utils.SparseVector;
import peersim.core.CommonState;

public class DummySumLearningModelWithEncryption extends SizedModel implements LearningModel,Mergeable<DummySumLearningModelWithEncryption>{
  private static final long serialVersionUID = 532238655136262419L;
  private Integer sum; 
  private double encodingTime;
  private double decodingTime;
  
  public DummySumLearningModelWithEncryption(String prefix) {
    super(prefix);
    sum=0;
  }
  
  public DummySumLearningModelWithEncryption(DummySumLearningModelWithEncryption sm) {
    super(sm);
    sum=sm.sum;
  }
  
  @Override
  public Object clone() {
    return new DummySumLearningModelWithEncryption(this);
  }
  
  public void init(double minModelSize, double maxModelSize, double minEncodingTime, double maxEncodingTime,  double minDecodingTime, double maxDecodingTime) {
    super.init(minModelSize, maxModelSize);
    initEncodingTime(minEncodingTime, maxEncodingTime);
    initDecodingTime(minDecodingTime, maxDecodingTime);
    setSum(0);
  }
  
  protected double initEncodingTime(double minTime, double maxTime) {
    encodingTime = minTime + (maxTime - minTime) * CommonState.r.nextDouble();
    return encodingTime;    
  }
  
  protected double initDecodingTime(double minTime, double maxTime) {
    decodingTime = minTime + (maxTime - minTime) * CommonState.r.nextDouble();
    return decodingTime;    
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
  
  public double getEncodingTime() {
    return encodingTime;
  }
  
  public double getDecodingTime() {
    return decodingTime;
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    this.sum+=1;
  }

  @Override
  public DummySumLearningModelWithEncryption merge(DummySumLearningModelWithEncryption model) {
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
