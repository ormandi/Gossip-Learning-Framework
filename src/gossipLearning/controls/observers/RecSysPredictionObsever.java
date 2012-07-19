package gossipLearning.controls.observers;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.SparseVector;

import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;

public class RecSysPredictionObsever extends PredictionObserver {

  public RecSysPredictionObsever(String prefix) throws Exception {
    super(prefix);
  }
  
  public boolean execute() {
    updateGraph();
    if (format.equals("gpt") && CommonState.getTime() == 0) {
      System.out.println("#iter\tavgE\tdevE" + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# ") + errorComputator.getClass().getCanonicalName() + "\t[HolderIndex]");
    }
    Vector<Double> avgError = new Vector<Double>();
    Vector<Double> devError = new Vector<Double>();
    Vector<Double> counters = new Vector<Double>();
    
    double avgErr = 0.0;
    double devErr = 0.0;
    double counter = 0.0;
    
    for (int userId = 0; userId < eval.size(); userId++) {
      SparseVector instances = eval.getInstance(userId);
      LearningProtocol prot = (LearningProtocol)Network.get(userId).getProtocol(pid);
      
      for (int holderIndex = 0; holderIndex < prot.size(); holderIndex ++){
        ModelHolder modelHolder = prot.getModelHolder(holderIndex);
        Model model = modelHolder.getModel(modelHolder.size() - 1);
        if (counters.size() <= holderIndex) {
          avgError.add(0.0);
          devError.add(0.0);
          counters.add(0.0);
        }
        
        for (VectorEntry e : instances) {
          SparseVector instance = new SparseVector(1);
          instance.put(e.index, -1.0);
          double predicted = model.predict(instance);
          double expected = e.value;
          
          double error = errorFunction.computeError(expected, predicted);
          avgError.add(holderIndex, avgError.get(holderIndex) + error);
          devError.add(holderIndex, devError.get(holderIndex) + (error * error));
          counters.add(holderIndex, counters.get(holderIndex) + 1.0);
        }
      }
    }
    
    for (int i = 0; i < counters.size(); i++) {
      counter = counters.get(i);
      avgErr = avgError.get(i) / counter;
      devErr = devError.get(i) / counter;
      devErr -= avgErr * avgErr;
      devErr = devErr < 0.0 ? 0.0 : Math.sqrt(devErr);
      
      // print info
      if (CommonState.getTime() > 0) {
        if (format.equals("gpt")) {
          //System.out.println(CommonState.getTime() + "\t" + Configuration.getLong("simulation.logtime"));
          System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + avgErr + "\t" + devErr + "\t" + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# ") +  getClass().getCanonicalName() +  " - " + errorComputator.getClass().getCanonicalName() + "\t[" + i + "]");
        } else {
          System.out.println(getClass().getCanonicalName() + " - " + errorComputator.getClass().getCanonicalName() + "\t[" + i + "]" + ":\tAvgE=" + avgErr + "\tDevE=" + devErr + ((printSuffix.length() > 0) ? "\t# " + printSuffix : "") );
        }
      }
    }
    return false;
  }

}
