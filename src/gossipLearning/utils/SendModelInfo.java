package gossipLearning.utils;

import gossipLearning.interfaces.models.MultiLearningModel;
import peersim.core.Node;

public class SendModelInfo {
  private final MultiLearningModel sendModel;
  private final Node dest; 
  private final Long predictedTimeBasedOnEDSimulator;
  private boolean isSending;

  public SendModelInfo() {
    dest = null;
    sendModel = null;
    predictedTimeBasedOnEDSimulator=0L;
    isSending = false;
  }

  public SendModelInfo(MultiLearningModel sendingModel,Node dest,Long predictedTimeBasedOnEDSimulator) {
    this.sendModel = sendingModel;
    this.dest = dest;
    this.predictedTimeBasedOnEDSimulator = predictedTimeBasedOnEDSimulator;
    this.isSending = true;
  }

  public SendModelInfo(SendModelInfo o) {
    this.sendModel=o.sendModel;
    this.dest=o.dest;
    this.predictedTimeBasedOnEDSimulator=o.getPredictedTimeBasedOnEDSimulator(); 
    this.isSending=o.isSending;
  }
  
  @Override
  public Object clone(){
    return new SendModelInfo(this);
  }
  
  public MultiLearningModel getModelToBeSent() {
    return sendModel;
  }

  public void stopSending(){
    this.isSending = false;
  }
  
  public Node getDest() {
    return dest;
  }

  public Long getPredictedTimeBasedOnEDSimulator() {
    return predictedTimeBasedOnEDSimulator;
  }
  
  public boolean isSending() {
    return isSending;
  }



}
