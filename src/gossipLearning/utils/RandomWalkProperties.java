package gossipLearning.utils;

import peersim.core.CommonState;

public final class RandomWalkProperties {
  private final int stepID;
  private final long lastSeenTimeStamp;
  private final int step;
  private final int rwPropStep;

  public int getRwPropStep() {
    return rwPropStep;
  }

  public RandomWalkProperties(){
    stepID = 0;
    step = -1;
    lastSeenTimeStamp = -1;
    rwPropStep = 0;
  }

  public RandomWalkProperties(int stepID, long lastSeenTimeStamp, int step, int rwPropStep) {
    this.stepID = stepID;
    this.lastSeenTimeStamp = lastSeenTimeStamp;
    this.step = step;
    this.rwPropStep = rwPropStep;
  }
  
  public RandomWalkProperties(RandomWalkProperties a) {
    this.stepID = a.stepID;
    this.lastSeenTimeStamp = a.lastSeenTimeStamp;
    this.step = a.step;
    this.rwPropStep = a.rwPropStep;
  }

  public Object clone(){
    return new RandomWalkProperties(this);
  }

  public int getStepID() {
    return stepID;
  }

  public long getLastSeenTimeStamp() {
    return lastSeenTimeStamp;
  }
/*
  public void setLastSeenTimeStamp(long lastSeenTimeStamp) {
    this.lastSeenTimeStamp = lastSeenTimeStamp;
  }
*/
  public long getAge() {
    return CommonState.getTime()-lastSeenTimeStamp;
  }

  public int getStep() {
    return step;
  }
  
  @Override
  public String toString() {
    return stepID+" "+step+" "+getAge();
  }

}
