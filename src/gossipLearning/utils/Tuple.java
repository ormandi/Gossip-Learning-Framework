package gossipLearning.utils;

import java.io.Serializable;

public class Tuple implements Serializable, Comparable<Tuple> {
  private static final long serialVersionUID = 1611226132532948362L;
  public final int nodeID;
  public final int holderID;
  public final int modelID;
  public final double cumulativeError;
  
  public Tuple(int n, int h, int m, double c) {
    nodeID = n;
    holderID = h;
    modelID = m;
    cumulativeError = c;
  }

  @Override
  public int compareTo(Tuple o) {
    if (cumulativeError < o.cumulativeError) {
      return -1;
    } else if (cumulativeError > o.cumulativeError) {
      return 1;
    }
    return 0;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof Tuple) {
      Tuple t = (Tuple) o;
      return cumulativeError == t.cumulativeError;
    }
    return false;
  }
  
  @Override
  public String toString() {
    return "" + cumulativeError;
  }
}