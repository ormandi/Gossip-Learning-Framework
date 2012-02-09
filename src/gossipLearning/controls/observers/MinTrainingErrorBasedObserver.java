package gossipLearning.controls.observers;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.models.boosting.FilterBoost;

import java.util.Set;
import java.util.TreeSet;

import peersim.core.Node;
import peersim.core.Protocol;

public class MinTrainingErrorBasedObserver extends BoostPredictionObserver {

  public MinTrainingErrorBasedObserver(String prefix) throws Exception {
    super(prefix);
    // TODO Auto-generated constructor stub
  }
  
  /**
   * Find the best model of the nodes, and evaluate it on the test set.
   */
  protected Set<Integer> generateIndices() {
    Set<Integer> result = new TreeSet<Integer>();
    int nodeID = 0;
    double approximatedError = Double.MAX_VALUE;
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
              if (filterBoostModel.getComulativeErr() < approximatedError) {
                // new model was found with minimal error
                approximatedError = filterBoostModel.getComulativeErr();
                nodeID = i;
              }
            }
          }
        }
      }
    }
    // return result
    result.add(nodeID);
    return result; 
  }

}
