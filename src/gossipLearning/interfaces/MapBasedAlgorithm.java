package gossipLearning.interfaces;

import java.util.Map;


public abstract class MapBasedAlgorithm<M extends gossipLearning.utils.Cloneable<M> & Model<Map<Integer,Double>>> extends AbstractAlgorithm<M,Map<Integer,Double>> implements InstanceHolder<Map<Integer,Double>> {
  protected Map<Integer,Double> x;                                // instance (sparse)
  protected double y;                                             // class label (+1.0 or -1.0)

  public Map<Integer, Double> getInstance() {
    return x;
  }
  
  public double getLabel() {
    return y;
  }
  
  public void setInstance(Map<Integer, Double> instance) {
    this.x = instance;
  }
  
  public void setLabel(double label) {
    if (label != -1.0 && label != 1.0) {
      throw new RuntimeException("Invalid class label at instance " + getCurrentNode().getID() + " which is " + label + "!");
    }
    this.y = label;
  }



}
