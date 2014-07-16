package gossipLearning.controls.bandits;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.reports.GraphObserver;

public class BanditObserver extends GraphObserver {
  protected boolean isPrintPrefix = true;
  protected final long logTime;
  
  public BanditObserver(String prefix) {
    super(prefix);
    logTime = Configuration.getLong("simulation.logtime");
  }

  @Override
  public boolean execute() {
    updateGraph();
    if (isPrintPrefix) {
      System.out.println("#iter\tacc\tregret");
      isPrintPrefix = false;
    }
    System.out.println((CommonState.getTime()/logTime) + "\t" + Machine.getInstance().getPrecision() + "\t" + Machine.getInstance().getRegret());
    return false;
  }

}
