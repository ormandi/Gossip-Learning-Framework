package gossipLearning.utils.database;

import java.util.Vector;

public class Database<I> {
  private final Vector<I> instances;
  private final Vector<Double> labels; 
  
  public Database(Vector<I> i, Vector<Double> l) {
    instances = i;
    labels = l;
  }

  public Vector<I> getInstances() {
    return instances;
  }

  public Vector<Double> getLabels() {
    return labels;
  }
  

}
