package gossipLearning.controls.bandits;

import peersim.core.Control;

public class BanditInitializer implements Control {
  
  public BanditInitializer(String prefix) {
    Machine.getInstance(prefix);
  }

  @Override
  public boolean execute() {
    return false;
  }

}
