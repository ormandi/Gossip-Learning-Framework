package gossipLearning.utils;

public class CurrentRandomWalkStatus {
  private final long nodeID;
  private final int stepID;
  private final int modelID;
  private final double step;
  private boolean isOnline;
  
  public CurrentRandomWalkStatus(long nodeID, int stepID, int modelID, double step, boolean isOnline) {
    this.nodeID = nodeID;
    this.stepID = stepID;
    this.modelID = modelID;
    this.step = step;
    this.isOnline = isOnline;
  }
  public int getModelID() {
    return modelID;
  }
  public long getNodeID() {
    return nodeID;
  }
  public int getStepID() {
    return stepID;
  }
  public double getStep() {
    return step;
  }
  public boolean isOnline() {
    return isOnline;
  }
  public void setOnline(boolean isOnline) {
    this.isOnline = isOnline;
  }
 
}
