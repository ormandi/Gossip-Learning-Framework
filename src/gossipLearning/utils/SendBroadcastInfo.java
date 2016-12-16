package gossipLearning.utils;

import gossipLearning.protocols.MultiWalkerProtocol;
import peersim.core.CommonState;

public class SendBroadcastInfo implements Comparable<SendBroadcastInfo>{
  
  private final ModelInfo modelInfo; 
  private int counter;
  private final EventWithModelInfoEnum sendType;
  private final long eventID;

  public SendBroadcastInfo(ModelInfo modelInfo,EventWithModelInfoEnum sendType) {
    this.modelInfo=(ModelInfo)modelInfo.clone();;
    this.counter=MultiWalkerProtocol.BROADCAST_LEVEL;
    this.sendType=sendType;
    this.eventID=newEvent();
  }
  public SendBroadcastInfo(ModelInfo modelInfo,Long eventID,EventWithModelInfoEnum sendType) {
    this.modelInfo=(ModelInfo)modelInfo.clone();;
    this.counter=MultiWalkerProtocol.BROADCAST_LEVEL;
    this.sendType=sendType;
    this.eventID=eventID;
  }

  public SendBroadcastInfo(SendBroadcastInfo o) {
    this.modelInfo=(ModelInfo)o.modelInfo.clone();
    this.sendType=o.sendType;
    this.counter=o.counter;
    this.eventID=o.eventID;
  }
  
  @Override
  public Object clone(){
    return new SendBroadcastInfo(this);
  }
  
  private long newEvent() {
    return CommonState.r.nextLong();
  }
  
  public ModelInfo getModelInfo() {
    return modelInfo;
  }

  public int getCounter() {
    return counter;
  }

  public EventWithModelInfoEnum getSendType() {
    return sendType;
  }

  public boolean nextSend(){
    if(counter>0){
      counter--;
      return true;
    } else {
      return false;
    }
  }

  public boolean hasNextSend(){
    if(counter>0){
      return true;
    } else {
      return false;
    }
  }
  
  public long getEventID() {
    return eventID;
  }
  
 
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (eventID ^ (eventID >>> 32));
    result = prime * result + ((modelInfo == null) ? 0 : modelInfo.hashCode());
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
    SendBroadcastInfo other = (SendBroadcastInfo) obj;
    if (eventID != other.eventID)
      return false;
    if (modelInfo == null) {
      if (other.modelInfo != null)
        return false;
    } else if (!modelInfo.equals(other.modelInfo))
      return false;
    return true;
  }
  
  @Override
  public int compareTo(SendBroadcastInfo o) {
    int compareValue = this.modelInfo.compareTo(o.modelInfo);
    if  (compareValue == 0){
      if (this.eventID < o.getEventID()){
        return 1;
      } else if (this.eventID > o.getEventID()) {
        return -1;
      } else {
        return 0;
      }
    }
    return compareValue;
  }
  
  @Override
  public String toString() {
    return "SendBroadcastInfo [modelInfo=" + modelInfo.getModelID() + ", counter=" + counter + ", sendType=" + sendType
        + ", eventID=" + eventID + "]";
  }
  

  
}
