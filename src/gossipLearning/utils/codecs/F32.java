package gossipLearning.utils.codecs;

/**
 * Encodes to 32-bit floats.
 */
public class F32 extends Codec {

  public F32(String prefix) {
  }

  @Override
  public Object encode(double data) {
    return (float)data;
  }
  
  @Override
  public double decode(Object cdata) {
    return (Float)cdata;
  }
  
}
