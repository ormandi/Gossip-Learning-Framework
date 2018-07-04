package gossipLearning.utils;

import peersim.core.Node;

public class ConnectionInfo implements Cloneable{
  private final Node receiverNode;
  private final int receiverNodeNatType;
  private final Long connectionEstabilishingTime;
  private final Long dataTransferStartTimeStamp;
  private final Long dataTransferTime;

  public ConnectionInfo(Node receiverNode, int receiverNodeNatType, Long connectionEstabilishingTime,
      Long dataTransferStartTimeStamp, Long dataTransferTime) {
    this.receiverNode = receiverNode;
    this.receiverNodeNatType = receiverNodeNatType;
    this.connectionEstabilishingTime = connectionEstabilishingTime;
    this.dataTransferStartTimeStamp = dataTransferStartTimeStamp;
    this.dataTransferTime = dataTransferTime;
  }
  
  public ConnectionInfo(ConnectionInfo anotherConnectionInfo) {
    this.receiverNode = anotherConnectionInfo.receiverNode;
    this.receiverNodeNatType = anotherConnectionInfo.receiverNodeNatType;
    this.connectionEstabilishingTime = anotherConnectionInfo.connectionEstabilishingTime;
    this.dataTransferStartTimeStamp = anotherConnectionInfo.dataTransferStartTimeStamp;
    this.dataTransferTime = anotherConnectionInfo.dataTransferTime;
  }

  @Override
  public Object clone() {
    return new ConnectionInfo(this);
  }
  
  public Node getReceiverNode() {
    return receiverNode;
  }

  public int getReceiverNodeNatType() {
    return receiverNodeNatType;
  }

  public Long getConnectionEstabilishingTime() {
    return connectionEstabilishingTime;
  }

  public Long getDataTransferStartTimeStamp() {
    return dataTransferStartTimeStamp;
  }

  public Long getDataTransferTime() {
    return dataTransferTime;
  }   

}
