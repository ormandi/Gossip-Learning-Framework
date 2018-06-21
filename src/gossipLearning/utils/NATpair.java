package gossipLearning.utils;

public class NATpair {
  private final int senderNATtype;
  private final int receiverNATtype;
  
  public NATpair(int senderNATtype, int receiverNATtype) {
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
