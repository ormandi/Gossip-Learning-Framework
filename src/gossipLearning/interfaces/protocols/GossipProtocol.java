package gossipLearning.interfaces.protocols;

import peersim.edsim.EDProtocol;
import gossipLearning.messages.ModelMessage;


/**
 * This interface defines the two main function for the gossip based protocols.
 * @author István Hegedűs
 * 
 * @has 1 "" n ModelHolder
 * @has 1 "" 1 InstanceHolder
 * @navassoc - - - ModelMessage
 */
public interface GossipProtocol extends EDProtocol, Churnable {
  /**
   * This is where the active processing happens i.e. sending
   * of messages.
   */
  public void activeThread();

  /**
   * This method is responsible for handling an incoming learning
   * message.
   *
   * @param message The content of the incoming message.
   */
  public void passiveThread(ModelMessage message);
}

