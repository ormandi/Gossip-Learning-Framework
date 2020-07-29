package gossipLearning.interfaces.protocols;

import java.util.LinkedList;

import gossipLearning.controls.ChurnControl;
import gossipLearning.messages.ActiveThreadMessage;
import gossipLearning.messages.ModelMessage;
import gossipLearning.messages.OnlineSessionFollowerActiveThreadMessage;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.ChurnTransportM;
import peersim.transport.Transport;

/**
 * This abstract base class (ABC) is situated between the Peersim protocol interface
 * and our concrete learning protocol implementations (in the inheritance tree).
 * Basically it implements and hides the irrelevant details
 * from the viewpoint of learning protocols.
 * So in the concrete protocols we have to take care of only the learning dependent
 * code pieces.<br/>
 * Make sure you initialize well the delayMean and delayVar fields which defines the
 * length of active thread delay. These fields are used here but not initialized!<br/>
 * This implementation also adds some useful methods like getTransport, getOverlay and
 * getCurrentProtocol.
 *
 * @author Róbert Ormándi
 *
 */
public abstract class AbstractProtocol implements GossipProtocol, Cloneable {
  //active thread delay mean and variance
  /** @hidden */
  protected static final String PAR_DELAYMEAN = "delayMean";
  protected final double delayMean;
  /** @hidden */
  protected static final String PAR_DELAYVAR = "delayVar";
  protected final double delayVar;
  
  // variables for modeling churn
  protected long sessionLength = ChurnControl.INIT_SESSION_LENGTH;
  protected int sessionID = 0;
  
  // state variables
  /** @hidden */
  protected Node currentNode;
  /** @hidden */
  protected int currentProtocolID = -1;
  /** @hidden */
  
  public AbstractProtocol(String prefix) {
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR);
  }
  
  protected AbstractProtocol(AbstractProtocol a) {
    delayMean = a.delayMean;
    delayVar = a.delayVar;
  }
  
  /**
   * This method performers the deep copying of the protocol.
   */
  @Override
  public abstract AbstractProtocol clone();
  
  private LinkedList<Integer> onlines = new LinkedList<Integer>();
  /**
   * Returns a node reference from the neighbor node list selected uniform 
   * randomly.
   * @return random neighbor node reference
   */
  protected Node getRandomNeighbor() {
    Linkable overlay = getOverlay();
    Node randomNode = overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
    return randomNode;
  }
  
  /**
   * Returns a node reference from the neighbor node list that will be possible 
   * online at the message arrive time, requires {@link ChurnTransportM}. Or 
   * null if there is no online neighbor.
   * @return online neighbor node reference or null
   */
  protected Node getOnlineNeighbor() {
    onlines.clear();
    Linkable overlay = getOverlay();
    for (int i = 0; i < overlay.degree(); i++) {
      Node randomNode = overlay.getNeighbor(i);
      ChurnTransportM transport = (ChurnTransportM) randomNode.getProtocol(FastConfig.getTransport(currentProtocolID));
      if (transport.isOnline()) {
        onlines.add(i);
      }
    }
    
    Node randomNode = null;
    if (onlines.size() != 0) {
      randomNode = overlay.getNeighbor(onlines.get(CommonState.r.nextInt(onlines.size())));
    }
    return randomNode;
  }
  
  /**
   * Sends the specified message to the specified destination node for the 
   * specified protocol from the specified source node.
   * @param src source node
   * @param dst destination node
   * @param msg message to be sent
   * @param pid target protocol id
   */
  protected void send(Node src, Node dst, ModelMessage msg, int pid) {
    //System.out.println("SEND\t" + src.getID() + "\t" + dst.getID() + "\t" + msg.getModel(0));
    getTransport().send(src, dst, msg, pid);
  }
  
  /**
   * It is method which makes more easer of the accessing to the transport layer of the current node.
   * 
   * @return The transform layer is returned.
   */
  protected Transport getTransport() {
    return ((Transport) currentNode.getProtocol(FastConfig.getTransport(currentProtocolID)));
  }
  
  /**
   * This method supports the accessing of the overlay of the current node.
   * 
   * @return The overlay of the current node is returned.
   */
  protected Linkable getOverlay() {
    return (Linkable) currentNode.getProtocol(FastConfig.getLinkable(currentProtocolID));
  }
  
  /**
   * This is a helper method which returns the current protocol instance.
   * Here we assume that the subclass implements the interface {@link gossipLearning.interface.LearningProtocol}
   * for which this helper ABC implementation was designed.<br/>
   * Implementing the interface {@link gossipLearning.interface.LearningProtocol} is
   * strongly suggested!
   * 
   * @return This protocol instance is returned.
   */
  protected GossipProtocol getCurrentProtocol() {
    return (GossipProtocol) currentNode.getProtocol(currentProtocolID);
  }
  
  /**
   * This is the most basic implementation of processEvent which 
   * can recognize two types of messages:
   * <ul>
   *   <li>In the case when the protocol receives message from the
   *   first type it indicates that an <i>activeThread()</i> method call
   *   has to be performed. Messages of the first type are the instances of
   *   {@link gossipLearning.messages.ActiveThreadMessage} or 
   *   {@link gossipLearning.messages.OnlineSessionFollowerActiveThreadMessage}.</li>
   *   <li>In the other hand when a {@link gossipLearning.messages.ModelMessage}
   *   is received the protocol perform a <i>passiveThread(modelMessage)</i> call.</li>
   * </ul>
   * Notice that the two abstract methods here are the same as those specified in the
   * interface {@link gossipLearning.interfaces.protocols.GossipProtocol}.
   * 
   * @param currentNode Reference to the current node.
   * @param currentProtocolID ID of the current protocol.
   * @param messageObj The message as an Object.
   */
  @Override
  public void processEvent(Node currentNode, int currentProtocolID, Object messageObj) {
    // the current node and protocol fields are updated
    this.currentNode = currentNode;
    this.currentProtocolID = currentProtocolID;
    
    if ( messageObj instanceof ActiveThreadMessage || 
          (messageObj instanceof OnlineSessionFollowerActiveThreadMessage && 
          ((OnlineSessionFollowerActiveThreadMessage)messageObj).sessionID == sessionID) ) {
      
      // The received message is a valid active thread alarm => performing active thread call
      activeThread();
      
      // After the processing we set a new alarm with a delay
      if (!Double.isInfinite(delayMean)) {
        int delay = (int)(delayMean + CommonState.r.nextGaussian()*delayVar);
        delay = (delay > 0) ? delay : 1;
        EDSimulator.add(delay, new OnlineSessionFollowerActiveThreadMessage(sessionID), currentNode, currentProtocolID);
      }
    } else if (messageObj instanceof ModelMessage) {
      ModelMessage msg = (ModelMessage)messageObj;
      //System.out.println("RECV\t" + msg.getDestination().getID() + "\t" + msg.getSource().getID() + "\t" + msg.getModel(0));
      // The received message is a model message => calling the passive thread handler
      passiveThread(msg);
    }
  }
  
  /**
   * Returns the ID of the current protocol.
   * @return the protocol ID
   */
  public int getPID() {
    if (currentProtocolID < 0) {
      throw new RuntimeException("Too early request for PID!");
    }
    return currentProtocolID;
  }
  
  //----- Churnable related methods -----
  
  /**
   * Basic churn implementation which simply stores the remaining session length
   * in a field.
   * 
   * @return remaining session length
   */
  @Override
  public long getSessionLength() {
    return sessionLength;
  }
  
  /**
   * It sets a new session length.
   * 
   * @param new session length which overwrites the original value 
   */
  @Override
  public void setSessionLength(long sessionLength) {
    this.sessionLength = sessionLength;
  }

  /**
   * Session initialization simply awakes the protocol by adding an active thread event to itself
   * with delay 0.
   */
  @Override
  public void initSession(Node node, int protocol) {
    sessionID ++;
    EDSimulator.add(1, new OnlineSessionFollowerActiveThreadMessage(sessionID), node, protocol);
  }

}
