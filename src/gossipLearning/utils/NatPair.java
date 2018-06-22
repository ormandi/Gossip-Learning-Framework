package gossipLearning.utils;

public class NatPair {
  private final int senderNATtype;
  private final int receiverNATtype;
  
  public NatPair(int senderNATtype, int receiverNATtype) {
    this.senderNATtype = senderNATtype;
    this.receiverNATtype = receiverNATtype;
  }

  public int getSenderNATtype() {
    return senderNATtype;
  }

  public int getReceiverNATtype() {
    return receiverNATtype;
  }
  
  
}
