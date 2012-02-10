package gossipLearning.controls.observers;

import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * Measures the error or performance of the boosting algorithms.
 * 
 * Basically it runs approximately in each boosting iteration and after a certain
 * number of samples (exponential step size).
 * 
 * @author István Hegedűs
 *
 */
public class BoostPredictionObserver extends SamplingBasedPredictionObserver {

  private static final String PAR_C = "C";
  private int C;
  
  private long t, c_down, c_up, c_middle;
  private boolean evaluate = true;
  
  private long sampleRun = 1;
  
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
      setPrintSuffix("boosting iteration");
      return super.execute();
    }
    if (iter == sampleRun) {
      setPrintSuffix(sampleRun + "th samples");
      sampleRun *= 10;
      return super.execute();
    }
    if (!evaluate && iter > c_up) {
      c_down = c_up;
      c_up += (long)(2 * C * Math.log(t));
      c_middle = c_down + ((c_up - c_down) / 10);
      evaluate = true;
      t ++;
    }
    return false;
  }

}
