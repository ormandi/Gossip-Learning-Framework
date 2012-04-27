package gossipLearning.interfaces;

import java.io.Serializable;

public class VectorEntry implements Serializable{
  private static final long serialVersionUID = 602724499869617224L;
  public int index;
  public double value;
  public VectorEntry(int index, double value) {
    this.index = index;
    this.value = value;
  }
  public String toString() {
    return index + " " + value;
  }
}
