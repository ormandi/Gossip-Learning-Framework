package gossipLearning.controls.observers;

import gossipLearning.interfaces.ErrorEstimatorModel;
import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.utils.Tuple;
import gossipLearning.utils.View;

import java.util.Set;
import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;

public class MinTrainingErrorObserver extends SamplingBasedPredictionObserver {
  protected static final String PAR_NUM_OF_MINS = "numberOfMinimum";
  protected final int k;
  protected final long logtime;

  public MinTrainingErrorObserver(String prefix) throws Exception {
    super(prefix);
    k = Configuration.getInt(prefix + "." + PAR_NUM_OF_MINS,  1);
    logtime = Configuration.getLong("simulation.logtime");
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
          Model model = modelHolder.getModel(modelHolder.size() -1);
          if (model instanceof ErrorEstimatorModel) {
            ErrorEstimatorModel errorModel = (ErrorEstimatorModel) model;
            Tuple t = new Tuple(i, holderIndex, modelHolder.size() -1, errorModel.getError());
            if (!mins.contains(t)) {
              // new model was found with minimal error
              mins.insert(t);
            }
          }
        }
      }
    }
    // return result
    for (Tuple t : mins) {
      result.add(t.nodeID);
      LearningProtocol p = ((LearningProtocol)((Node)g.getNode(t.nodeID)).getProtocol(pid));
      System.out.println("0M:\t" + (CommonState.getTime()/logtime) + "\t" + p.getModelHolder(t.holderID).getModel(t.modelID));
    }
    return result; 
  }

}
