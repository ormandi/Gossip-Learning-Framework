package gossipLearning.interfaces.models;

import peersim.core.CommonState;

public abstract class SizedModel implements Model{
  private static final long serialVersionUID = 1865975097606219555L;
  
  protected double modelSize;
  
  public SizedModel(String prefix) {
    setValueToDefault();
  }
  
  public SizedModel(SizedModel sm) {
    modelSize = sm.modelSize;
  }

  public void setValueToDefault(){
    modelSize = 0.0;
  }
  
  protected void init(double minModelSize, double maxModelSize){
    initSize(minModelSize,maxModelSize);
  }
  
  protected double initSize(double minModelSize, double maxModelSize) {
    modelSize = minModelSize + (maxModelSize - minModelSize) * CommonState.r.nextDouble();
    return modelSize;    
  }
  
  public double getModelSize() {
    return modelSize;
  }
  
  public void setModelSize(double modelSize) {
    this.modelSize = modelSize;
  }
  
  /**
   * This method performers the deep copying of the protocol.
   */
  @Override
  public abstract Object clone();
}
