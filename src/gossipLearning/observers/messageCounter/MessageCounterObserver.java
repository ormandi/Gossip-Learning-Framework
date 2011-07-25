package gossipLearning.observers.messageCounter;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.reports.GraphObserver;

/**
 * This contorol sums up the messages from a given type during one iteration defined by LOGTIME. It requires the using
 * of the MessageCounterTransoprt which does the message summing.
 * 
 * @author ormandi
 *
 */
public class MessageCounterObserver extends GraphObserver {
  private static final String PAR_TRANSPORT = "protocol";
  private final int pid;
  private static final String PAR_FORMAT = "format";
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
        System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + numOfModels + "\t# " + transport.getFilterClass().getCanonicalName());
      } else {
        System.out.println(transport.getFilterClass().getCanonicalName() + ":\tnumberOfMessages=" + numOfModels);
      }
    }
    return false;
  }

}
