package gossipLearning.utils.codecs;

/**
 * Dummy codec.
 * Encodes to 64-bit floats.
 */
public class F64 extends Codec {

  public F64(String prefix) {
  }

  @Override
  public Object encode(double data) {
    return data;
  }
  
  @Override
  public double decode(Object cdata) {
    return (Double)cdata;
  }
  
}
