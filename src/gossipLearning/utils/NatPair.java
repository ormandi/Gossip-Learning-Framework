package gossipLearning.utils;

public class NatPair {
  private final int senderNatType;
  private final int receiverNatType;
  
  public NatPair(int senderNatType, int receiverNatType) {
    this.senderNatType = senderNatType;
    this.receiverNatType = receiverNatType;
  }

  public int getSenderNATtype() {
    return senderNatType;
  }

  public int getReceiverNATtype() {
    return receiverNatType;
  }
  
  
}
