package gossipLearning.utils.codecs;

import peersim.config.Configuration;

/**
 * Pivot codec.
 * Encodes to 1 bit.
 */
public class Pivot extends Codec {

  /**
  * Initial step size. Default is 1.
  * @config
  */
  private static final String PAR_STEP = "stepSize";

  private double estimate = 0;
  private double step;
  private boolean last = false;
  
  public Pivot(String prefix) {
    step = Configuration.getDouble(prefix+"."+PAR_STEP,1);
  }
  
  @Override
  public Object encode(double data) {
    return Math.abs(estimate+step-data)<Math.abs(estimate-data);
  }
  
  @Override
  public double decode(Object cdata) {
    boolean bit = (Boolean)cdata;
    if (bit) {
      estimate += step;
      if (last)
        step *= 2;
    } else {
      if (step/2==0) // to deal with the limitations of floating-point arithmetic
        step *= -1;
      else
        step /= -2;
    }
    last = bit;
    return estimate;
  }
  
}
