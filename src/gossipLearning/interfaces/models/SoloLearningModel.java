package gossipLearning.interfaces.models;

import gossipLearning.utils.SparseVector;
import peersim.core.CommonState;
import peersim.core.Network;

public abstract class SoloLearningModel implements LearningModel,Cloneable,Mergeable<SoloLearningModel> {
 
  private static final long serialVersionUID = -3817122072099454437L;
  protected int stepID;
  protected int modelID;
  protected int step;
 
  public SoloLearningModel(String prefix){
    stepID = 0;
    modelID = 0;
    step = 0;
  }
  
  public SoloLearningModel(SoloLearningModel li) {
    stepID = li.stepID;
    modelID = li.modelID;
    step = li.step; 
  }
  
  /**
   * This method performers the deep copying of the protocol.
   */
  @Override
  public abstract Object clone();
  
  @Override
  public void update(SparseVector instance, double label) {
    nextStep();
  }
  
  public int nextStep(){
    if(step < 0) {
      step = 0;
      initIDs();
    } else {
      step++;
      this.stepID = resetID(stepID);
    }
    return step;
  }
  
  @Override
  public SoloLearningModel merge(SoloLearningModel model) {
    if(model.step < this.step)
      return this;
    else
      return model;
  }
  
  public void init(){
    initIDs();
    initStep();
  }
  
  public int initIDs() {
    this.modelID = resetID(modelID);
    this.stepID = resetID(stepID);
    return stepID;
  }
  
  public int resetStepID(){
    this.stepID = resetID(stepID);
    return stepID;
  }
  
  public int resetID(int prevID) {
    int stepID = 0;
    while(stepID == 0 || prevID == stepID){
      stepID = CommonState.r.nextInt();
    }
    return stepID;
  }

  public int getStepID() {
    return stepID;  
  }
  
  public int getModelID() {
    return modelID;
  }

  public void resetModel() {
    stepID = 0;
    modelID = 0;
    init();
  }
  
  @Override
  public double getAge() {
    return getStep();
  }
  
  public double getStep() {
    return step;
  }

  public int initStep(){
    step= (CommonState.r.nextInt(Network.size())+1)*(-1);
    return step;
  }
  

  
}
