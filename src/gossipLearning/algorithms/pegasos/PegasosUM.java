package gossipLearning.algorithms.pegasos;

import gossipLearning.algorithms.pegasos.model.PegasosModel;
import gossipLearning.algorithms.pegasos.model.PegasosModelComparator;

import java.util.Map;
import java.util.TreeMap;

import peersim.core.Node;

public class PegasosUM extends PegasosMU {
  protected PegasosModel previousNonUpdatedModel = null;
  protected PegasosModel currentNonUpdatedModel = null;
  private static final PegasosModelComparator mc = new PegasosModelComparator();
  
  public PegasosUM(String prefix) {
    super(prefix);
  }
  
  public PegasosUM clone() {
    return new PegasosUM(prefix);
  }
  
  protected void createModel(PegasosModel model, Node currentNode, int currentProtocolID, boolean isUpdateAndStore, boolean isSend) {
    if (isUpdateAndStore) {
      // get the previous model which is not updated yet OR the currentModel if it is not available
      PegasosModel mj = (previousNonUpdatedModel != null) ? previousNonUpdatedModel : currentModel ;
      // update it
      updateModel(mj);
      long agej = mj.getAge();
      Map<Integer, Double> wj = mj.getW();
      
      
      // get the currently stored model which cannot be null
      PegasosModel mi = (currentNonUpdatedModel != null) ? currentNonUpdatedModel : currentModel;
      // update it
      if ( mc.compare(mj, mi) != 0 ) {
        updateModel(mi);
      }
      long agei = mi.getAge();
      Map<Integer, Double> wi = mi.getW();
      
      // compute the averaged model of updated ones
      Map<Integer, Double> w = new TreeMap<Integer,Double>();
      for (int xID : wj.keySet()) {
        if (wi.containsKey(xID)) {
          w.put(xID, (wi.get(xID) + wj.get(xID)) / 2.0);
        } else {
          w.put(xID, wj.get(xID) / 2.0);
        }
      }
      for (int xID : wi.keySet()) {
        if (!wj.containsKey(xID)) {
          w.put(xID, wi.get(xID) / 2.0);
        }
      }
      
      // create current model as the average of the updated ones
      currentModel = new PegasosModel(w, 0.0, Math.max(agei, agej));
      
      // add the model stored in currentModel variable to the model queue
      storeModel(currentModel);
    }
    
    if (isSend ) {
      // send model stored in currentModel variable to a random neighbor
      sendModel(currentModel, currentNode, currentProtocolID);
    }
  }

}
