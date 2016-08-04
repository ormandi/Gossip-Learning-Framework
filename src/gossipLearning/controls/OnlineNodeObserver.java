package gossipLearning.controls;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;

public class OnlineNodeObserver implements Control {
  
  protected final long logTime;

  public OnlineNodeObserver(String prefix) {
    logTime = Configuration.getLong("simulation.logtime");
  }
  
  @Override
  public boolean execute() {
    double online = 0.0;
    for (int i = 0; i < Network.size(); i++) {
      if (Network.get(i).getFailState() == Fallible.OK) {
        online ++;
      }
    }
    System.out.println((CommonState.getTime()/logTime) + "\t" + (online / Network.size()));
    return false;
  }

}
