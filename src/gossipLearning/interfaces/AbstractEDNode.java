package gossipLearning.interfaces;

import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

@SuppressWarnings({"rawtypes"})
public abstract class AbstractEDNode implements EDProtocol {
  
  protected Transport getTransport(Node currentNode, int currentProtocolID) {
    return ((Transport) currentNode.getProtocol(FastConfig.getTransport(currentProtocolID)));
  }
  
  protected Transport getTransport(Node currentNode) {
    return ((Transport) currentNode.getProtocol(FastConfig.getTransport(CommonState.getPid())));
  }
  
  protected Transport getTransport() {
    return ((Transport) CommonState.getNode().getProtocol(FastConfig.getTransport(CommonState.getPid())));
  }
  
  protected Linkable getOverlay(Node currentNode, int currentProtocolID) {
    return (Linkable) currentNode.getProtocol(FastConfig.getLinkable(currentProtocolID));
  }
  
  protected Linkable getOverlay(Node currentNode) {
    return (Linkable) currentNode.getProtocol(FastConfig.getLinkable(CommonState.getPid()));
  }
  
  protected Linkable getOverlay() {
    return (Linkable) CommonState.getNode().getProtocol(FastConfig.getLinkable(CommonState.getPid()));
  }
  
  protected Node getCurrentNode() {
    return CommonState.getNode();
  }
  
  protected AbstractEDNode getCurrentProtocol(Node currentNode, int currentProtocolID) {
    return (AbstractEDNode) currentNode.getProtocol(currentProtocolID);
  }
  
  protected AbstractEDNode getCurrentProtocol(Node currentNode) {
    return (AbstractEDNode) currentNode.getProtocol(CommonState.getPid());
  }
  
  protected AbstractEDNode getCurrentProtocol() {
    return (AbstractEDNode) CommonState.getNode().getProtocol(CommonState.getPid());
  }
  
  public abstract Object clone();
  public abstract void processEvent(Node currentNode, int currentProtocolID, Object msgObject);
}
