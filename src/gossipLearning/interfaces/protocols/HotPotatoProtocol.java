package gossipLearning.interfaces.protocols;

import gossipLearning.messages.Message;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;

/**
 * This interface defines the main function for the hot potato protocol.
 * @author István Hegedűs
 * 
 * @has 1 "" n ModelHolder
 * @has 1 "" 1 InstanceHolder
 * @navassoc - - - ModelMessage
 */
public interface HotPotatoProtocol extends EDProtocol,CDProtocol,Churnable{
  
  /**
   * This method is responsible for handling an incoming learning
   * message and for sending the latest message
   *
   * @param message The content of the incoming message.
   */
  public void onReceiveRandomWalk(Message message);
}
