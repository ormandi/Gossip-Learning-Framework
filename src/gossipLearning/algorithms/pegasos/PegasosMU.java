package gossipLearning.algorithms.pegasos;

import gossipLearning.algorithms.pegasos.model.PegasosModel;
import gossipLearning.messages.ModelMessage;

import java.util.Map;
import java.util.TreeMap;

import peersim.core.Node;


public class PegasosMU extends P2Pegasos {
  protected PegasosModel previousNonUpdatedModel = null;
  protected PegasosModel currentNonUpdatedModel = null;
  protected String prefix;
  
  public PegasosMU(String prefix) {
    super(prefix);
    this.prefix = prefix;
  }
  
  public PegasosMU clone() {
    return new PegasosMU(prefix);
  }
  
  @SuppressWarnings("unchecked")
  protected void passiveThread(Node currentNode, int currentProtocolID, Object messageObj) {
    // passive thread => receive & process incomming message
    ModelMessage<PegasosModel> message = (ModelMessage<PegasosModel>) messageObj;
    
    //
    //-------------------- begin of work ------------------
    //
    
    // process incomming message
    previousNonUpdatedModel = currentNonUpdatedModel;
    currentNonUpdatedModel = message.getModel();
    
    // do a gradient update on the received model and send it
    createModel(null, currentNode, currentProtocolID, true, false);
    
    //
    //-------------------- end of work --------------------
    //
  }
  
  protected void createModel(PegasosModel model, Node currentNode, int currentProtocolID, boolean isUpdateAndStore, boolean isSend) {
    if (isUpdateAndStore) {
      // get the previous model which is not updated yet OR the currentModel if it is not available
      PegasosModel mj = (previousNonUpdatedModel != null) ? previousNonUpdatedModel : currentModel ;
      long agej = mj.getAge();
      Map<Integer, Double> wj = mj.getW();
      
      // get the currently stored model which cannot be null
      PegasosModel mi = (currentNonUpdatedModel != null) ? currentNonUpdatedModel : currentModel;
      long agei = mi.getAge() ;
      Map<Integer, Double> wi = mi.getW();
      
      // compute the averaged model
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
      
      currentModel = new PegasosModel(w, 0.0, Math.max(agei, agej));
      updateModel(currentModel);
      
      // add the model stored in currentModel variable to the model queue
      storeModel(currentModel);
    }
    
    if (isSend ) {
      // send model stored in currentModel variable to a random neighbor
      sendModel(currentModel, currentNode, currentProtocolID);
    }
  }
}
