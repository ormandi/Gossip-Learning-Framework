package gossipLearning.interfaces.protocols;

public interface ProtocolWithNatInfo {
  public void connectionChanged(int natType);
  public int getConnectionTime(int natTypeOfAnotherNode);
}
