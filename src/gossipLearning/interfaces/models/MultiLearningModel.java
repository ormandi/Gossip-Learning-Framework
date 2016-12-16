package gossipLearning.interfaces.models;

import gossipLearning.utils.ModelInfo;
import gossipLearning.utils.SparseVector;

public abstract class MultiLearningModel implements LearningModel,Cloneable,Comparable<MultiLearningModel> {

  private static final long serialVersionUID = -3817122072099454437L;

  protected ModelInfo modelInfo;

  public MultiLearningModel(String prefix){
    this.modelInfo = new ModelInfo(prefix);
  }
  
  public MultiLearningModel(ModelInfo mi){
    this.modelInfo = (ModelInfo)mi.clone();
  }

  public MultiLearningModel(MultiLearningModel li) {
    this.modelInfo = (ModelInfo)li.modelInfo.clone();
  }

  /**
   * This method performers the deep copying of the protocol.
   */
  @Override
  public abstract Object clone();

  public void initModel(){
    modelInfo.init();
  }
  
  public void restart() {
    modelInfo.restartModel();
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    nextStep();
  }

  public double nextStep(){
    if(modelInfo.getStep() < 0) {
      modelInfo.init();
    } else {
      modelInfo.setStep(modelInfo.getStep()+1);
      modelInfo.setThisTime();
    }
    return modelInfo.getStep();
  }

  public void setValueToDefault(){
    modelInfo.setValueToDefault();
  }

  public void removeModel(){
    modelInfo.removeModel();
  }

  public int getModelID() {
    return modelInfo.getModelID();
  }

  public int getWalkID() {
    return modelInfo.getWalkID();
  }  

  @Override
  public double getAge() {
    return getStep();
  }

  public double getStep() {
    return modelInfo.getStep();
  }  

  public long getLastSeenTimeStamp() {
    return modelInfo.getLastSeenTimeStamp();
  }

  public double getModelSize() {
    return modelInfo.getModelSize();
  }

  public ModelInfo getModelInfo() {
    return modelInfo;
  }
  
  @Override
  public int hashCode() {
    return this.modelInfo.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    return this.modelInfo.equals(((MultiLearningModel)obj).getModelInfo());
  }
  
  @Override
  public int compareTo(MultiLearningModel o) {
    return this.modelInfo.compareTo(o.modelInfo);
  }

  @Override
  public String toString() {
    return modelInfo.toString();
  }
  
}
