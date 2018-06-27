package gossipLearning.interfaces.protocols;

public interface ProtocolWithNatInfo {
  public void connectionChanged(int newNatType);
  public int getNatType();
}
