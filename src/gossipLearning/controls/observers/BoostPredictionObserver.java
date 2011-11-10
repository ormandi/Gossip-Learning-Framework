package gossipLearning.controls.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * Measures the error or performance of the boosting algorithms.
 * @author István Hegedűs
 *
 */
public class BoostPredictionObserver extends SamplingBasedPredictionObserver {

  private static final String PAR_C = "C";
  private int C;
  
  private long t, c_down, c_up, c_middle;
  private boolean evaluate = true;
  
  public BoostPredictionObserver(String prefix) throws Exception {
    super(prefix);
    C = Configuration.getInt(prefix + "." + PAR_C);
    t = 3;
    c_down = 0;
    c_up = 0;
    c_middle = -1;
  }
  
  public boolean execute() {
    long iter = CommonState.getTime()/Configuration.getLong("simulation.logtime");
    if (evaluate && iter > c_middle) {
      evaluate = false;
      return super.execute();
    }
    if (!evaluate && iter > c_up) {
      c_down += c_up + (long)(C * Math.log(t));
      c_up += (long)(2 * C * Math.log(t));
      c_middle = c_down + ((c_up - c_down) / 2);
      evaluate = true;
      t ++;
    }
    return false;
  }

}
