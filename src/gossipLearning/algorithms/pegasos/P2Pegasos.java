package gossipLearning.algorithms.pegasos;

import gossipLearning.algorithms.pegasos.model.PegasosModel;
import gossipLearning.interfaces.MapBasedAlgorithm;
import gossipLearning.messages.ModelMessage;
import gossipLearning.utils.Utils;

import java.util.LinkedList;
import java.util.TreeMap;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

public class P2Pegasos extends MapBasedAlgorithm<PegasosModel> {
  
  protected P2Pegasos(double lambda, double delayMean, double delayVar, int memorySize) {
    observedModels = new LinkedList<PegasosModel>();
    this.lambda = lambda;
    this.delayMean = delayMean;
    this.delayVar = delayVar;
    this.memorySize = memorySize;
  }
  
  public P2Pegasos(String prefix) {
    observedModels = new LinkedList<PegasosModel>();
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
    delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 0.0);
    memorySize = Configuration.getInt(prefix + "." + PAR_MEMORYSIZE, 1);
  }
  
  public P2Pegasos clone() {
    return new P2Pegasos(lambda, delayMean, delayVar, memorySize);    
  }
  
  protected void sendModel(PegasosModel model, Node currentNode, int currentProtocolID) {
    // select a uniform random node and send my model to him
    Node neighbor = selectNeighbor();
    Transport transport = getTransport(currentNode, currentProtocolID);
    transport.send(currentNode, neighbor, new ModelMessage<PegasosModel>(currentNode, model), currentProtocolID);
  }
  
  protected void storeModel(PegasosModel model) {
    // store the new model in the limited sized model queue
    observedModels.offer(new PegasosModel(model.getW(), model.getBias(), model.getAge()));
    while (observedModels.size() > memorySize) {
      observedModels.poll();
    }
  }
  
  protected PegasosModel updateModel(PegasosModel model) {
    model.setAge(model.getAge() + 1);
    double nu = 1.0/(lambda * (double) (model.getAge()));
    boolean isSV = y * Utils.innerProduct(model.getW(), x) < 1.0;
    int max = findMaxIdx();
    for (int i = 0; i <= max; i ++) {
      Double wOldCompD = model.getW().get(i);
      Double xCompD = x.get(i);
      if (wOldCompD != null || xCompD != null) {
        double wOldComp = (wOldCompD == null) ? 0.0 : wOldCompD.doubleValue();
        double xComp = (xCompD == null) ? 0.0 : xCompD.doubleValue();
        if (isSV) {
          // the current point in the current model is a SV
          // => applying the SV-based update rule
          model.getW().put(i, (1.0 - 1.0/((double)model.getAge())) * wOldComp + nu * y * xComp);
        } else {
          // the current point is not a SV in the currently stored model
          // => applying the normal update rule
          if (wOldCompD != null) {
            model.getW().put(i, (1.0 - 1.0/((double)model.getAge())) * wOldComp);
          }
        }
      }
    }
    return model;
  }
  
  public void initModel() {
    currentModel = new PegasosModel(new TreeMap<Integer, Double>(), 0.0, 1);
  }
  
  protected int findMaxIdx() {
    int max = - Integer.MAX_VALUE;
    for (int d : currentModel.getW().keySet()) {
      if (d > max) {
        max = d;
      }
    }
    for (int d : x.keySet()) {
      if (d > max) {
        max = d;
      }
    }
    return max;
  }
}
