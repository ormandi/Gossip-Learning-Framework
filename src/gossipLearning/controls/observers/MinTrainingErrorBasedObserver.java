package gossipLearning.controls.observers;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.boosting.FilterBoost;
import gossipLearning.utils.View;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.core.Protocol;

public class MinTrainingErrorBasedObserver extends BoostPredictionObserver {
  protected static final String PAR_NUM_OF_MINS = "numberOfMinimum";
  protected final int k;

  public MinTrainingErrorBasedObserver(String prefix) throws Exception {
    super(prefix);
    k = Configuration.getInt(prefix + "." + PAR_NUM_OF_MINS,  1);
  }
  
  /**
   * Find the best model of the nodes, and evaluate it on the test set.
   */
  protected Set<Integer> generateIndices() {
    Set<Integer> result = new TreeSet<Integer>();
    View<Tuple> mins = new View<Tuple>(k);
    for (int i = 0; i < g.size(); i ++) {
      Protocol p = ((Node) g.getNode(i)).getProtocol(pid);
      if (p instanceof LearningProtocol) {
        // evaluating the model(s) of the ith node
        int numOfHolders = ((LearningProtocol)p).size();
        for (int holderIndex = 0; holderIndex < numOfHolders; holderIndex++){
          ModelHolder modelHolder = ((LearningProtocol)p).getModelHolder(holderIndex);
          // search best model
          for (int modelIdx = 0; modelIdx < modelHolder.size(); modelIdx ++) {
            Model model = modelHolder.getModel(modelIdx);
            if (model instanceof FilterBoost) {
              FilterBoost filterBoostModel = (FilterBoost) model;
              Tuple t = new Tuple(i, holderIndex, modelIdx, filterBoostModel.getComulativeErr());
              if (!mins.contains(t)) {
                // new model was found with minimal error
                mins.insert(t);
              }
            }
          }
        }
      }
    }
    // return result
    for (Tuple t : mins) {
      result.add(t.nodeID);
    }
    return result; 
  }
  
  class Tuple implements Serializable, Comparable<Tuple> {
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
}