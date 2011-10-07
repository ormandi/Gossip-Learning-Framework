package gossipLearning.controls.observers;

import gossipLearning.interfaces.Churnable;
import peersim.core.Fallible;
import peersim.core.Node;
import peersim.reports.GraphObserver;

/**
 * This observer simply prints the IDs of the online nodes with their session length.
 *  
 * @author ormandi
 *
 */
public class OnlineNodeObserver extends GraphObserver {
  
  public OnlineNodeObserver(String prefix) {
    super(prefix);
  }

  public boolean execute() {
    updateGraph();
    for (int i = 0; i < g.size(); i ++) {
      Node n = (Node) g.getNode(i);
      if (n.getFailState() == Fallible.OK) {
        System.out.print(n.getID() + ":" + ((Churnable)n.getProtocol(pid)).getSessionLength() + " ");
      }
    }
    System.out.println();
    return false;
  }

}
