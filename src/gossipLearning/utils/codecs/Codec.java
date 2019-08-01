package gossipLearning.utils.codecs;

/**
 * Lossily and adaptively encodes a correlated stream of real numbers to a stream of implementation-specific objects, and vice versa.
 */
public abstract class Codec implements Cloneable {

  public Codec clone() {
    try {
      return (Codec)super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /** Encodes the next real number. This does not update the Codec state. */
  public abstract Object encode(double data);
  
  /** Decodes the next real number. This also updates the Codec state. */
  public abstract double decode(Object cdata);
  
}
