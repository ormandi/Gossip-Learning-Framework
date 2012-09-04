package gossipLearning.controls.observers.messageCounter;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.reports.GraphObserver;

/**
 * This observer class is usable for computing the number of 
 * messages that go through the transport of the specified protocol. 
 * This transport layer should can count the transit messages.
 * @author István Hegedűs
 *
 */
public class MessageCounterObserver extends GraphObserver {
  private static final String PAR_TRANSPORT = "protocol";
  private final int pid;
  private static final String PAR_FORMAT = "format";
  /** @hidden */
  protected final String format;
  
  public MessageCounterObserver(String prefix) {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
    format = Configuration.getString(prefix + "." + PAR_FORMAT, "");
  }
  
    
  public boolean execute() {
    updateGraph();
    
    MessageCounterTransport transport = (MessageCounterTransport) Network.get(0).getProtocol(pid);
    
    if (format.equals("gpt") && CommonState.getTime() == 0) {
      System.out.println("#iter\tnumOfMessages\t# " + transport.getFilterClass().getCanonicalName());
    }
    
    if (CommonState.getTime() > 0) {
      int numOfModels = transport.numberOfAllIncommingMessages();
      transport.resetCounters();
      
      if (format.equals("gpt")) {
        System.out.println(CommonState.getTime() + "\t" + numOfModels + "\t# " + transport.getFilterClass().getCanonicalName());
      } else {
        System.out.println(transport.getFilterClass().getCanonicalName() + ":\tnumberOfMessages=" + numOfModels);
      }
    }
    return false;
  }

}
