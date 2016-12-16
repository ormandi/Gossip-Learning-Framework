package gossipLearning.utils;

import java.util.HashMap;
import java.util.HashSet;

import gossipLearning.protocols.MultiWalkerProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class ModelInfo implements Comparable<ModelInfo> {

  private static final String PAR_MAX_MODEL_SIZE = "maxsize";
  private static final String PAR_MIN_MODEL_SIZE = "minsize";
  public static HashMap<Integer, HashSet<Integer>> modelIDSet = new HashMap<Integer, HashSet<Integer>>();
  private static double MAX_MODEL_SIZE;
  private static double MIN_MODEL_SIZE;

  private int modelID;
  private int walkID;
  private long lastSeenTimeStamp;
  private double step;
  private double modelSize; // size of the modelcd bi 
  

  public ModelInfo(String prefix) {
    MAX_MODEL_SIZE = Configuration.getDouble(prefix + "." + PAR_MAX_MODEL_SIZE);
    MIN_MODEL_SIZE = Configuration.getDouble(prefix + "." + PAR_MIN_MODEL_SIZE);
    setValueToDefault();
  }

  public ModelInfo(ModelInfo li) {
    modelID = li.modelID;
    walkID = li.walkID;
    lastSeenTimeStamp = li.lastSeenTimeStamp;
    step = li.step;
    modelSize = li.modelSize;
  }

  public Object clone(){
    return new ModelInfo(this); 
  }

  public void setValueToDefault(){
    modelID = 0;
    walkID = 0;
    lastSeenTimeStamp = 0L;
    modelSize = 0.0;
    step = 0.0;
  }

  public void init(){
    initIDs();
    initSize();
    initStep();
    setThisTime();
  }

  private int initIDs() {
    this.modelID = generateNewModelID();
    this.walkID = generateNewWalkID(this.modelID);
    return walkID;
  }
  private double initSize() {
    modelSize = MIN_MODEL_SIZE + (MAX_MODEL_SIZE - MIN_MODEL_SIZE) * CommonState.r.nextDouble();
    return modelSize;    
  }
  
  public void setThisTime() {
    lastSeenTimeStamp = CommonState.getTime();
  }
  
  public void setThisTimeWithDelay(Long delay) {
    lastSeenTimeStamp = CommonState.getTime()-delay;
  }
  
  private double initStep(){
    step=0.0; //alternate: (CommonState.r.nextInt(Network.size())+1)*(-1);
    return step;
  }

  public void removeModel(){
    removeWalkID(walkID);
    setValueToDefault();
  }

  public void restartModel(){
    removeWalkID(walkID);
    this.walkID = generateNewWalkID(this.modelID);
    setThisTime();
  }

  private void updateModelIDSet(int modelID) {
    if(!modelIDSet.containsKey(modelID)){
      modelIDSet.put(modelID, new HashSet<Integer>());
    }
  }

  private void removeWalkID(int walkID){
    if(modelIDSet.get(modelID).contains(walkID))
      modelIDSet.get(modelID).remove(walkID);
  }
  
  @SuppressWarnings("unused")
  private void updateModelIDSet(int modelID, int walkID) {
    updateModelIDSet(modelID);
    if(!modelIDSet.get(modelID).contains(walkID)){
      modelIDSet.get(modelID).add(walkID);
    }
  }

  private int generateNewModelID(){
    int testID = CommonState.r.nextInt();
    while (modelIDSet.containsKey(testID)  || testID == 0){
      testID = CommonState.r.nextInt();
    }
    modelIDSet.put(testID, new HashSet<Integer>());
    return testID;
  }

  private int generateNewWalkID(int modelID) {
    int testID = CommonState.r.nextInt();
    while (modelIDSet.get(modelID).contains(testID) || testID == 0){
      testID = CommonState.r.nextInt();
    }
    modelIDSet.get(modelID).add(testID);
    return testID;
  }
  
  public void addNoiseToLastSeenTimeStamp(){
    this.lastSeenTimeStamp-=getRandomLatency();
  }
  
  private Long getRandomLatency(){
    int lat = (int)MultiWalkerProtocol.MIN_MESSAGE_LATENCY;
    return Math.round(CommonState.r.nextLong((int)lat+1)-lat+(lat/2.0));
  }

  public int getModelID() {
    return modelID;
  }
  public void setModelID(int modelID) {
    this.modelID = modelID;
  }
  public int getWalkID() {
    return walkID;
  }
  public void setWalkID(int walkID) {
    this.walkID = walkID;
  }
  public long getLastSeenTimeStamp() {
    return lastSeenTimeStamp;
  }
  public void setLastSeenTimeStamp(long lastSeenTimeStamp) {
    this.lastSeenTimeStamp = lastSeenTimeStamp;
  }
  public double getStep() {
    return step;
  }
  public void setStep(double step) {
    this.step = step;
  }
  public double getModelSize() {
    return modelSize;
  }
  public void setModelSize(double modelSize) {
    this.modelSize = modelSize;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + modelID;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ModelInfo other = (ModelInfo) obj;
    if (modelID != other.modelID)
      return false;
    return true;
  }

  @Override
  public int compareTo(ModelInfo o) {
    if(o.getModelID() < this.getModelID()){
      return 1;
    } else if(o.getModelID() > this.getModelID()) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    return "ModelInfo [modelID=" + modelID + ", walkID=" + walkID + ", lastSeenTimeStamp=" + lastSeenTimeStamp
        + ", step=" + step + "]";
  }
  
}
