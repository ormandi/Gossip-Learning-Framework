package gossipLearning.utils.codecs;

/**
 * Pivot codec.
 * Encodes to 1 bit.
 */
public class Pivot extends Codec {

  private double estimate = 0;
  private double step = 1;
  private boolean last = false;
  
  public Pivot(String prefix) {
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
      step /= -2;
    }
    last = bit;
    return estimate;
  }
  
}
