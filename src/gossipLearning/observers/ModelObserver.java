package gossipLearning.observers;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.interfaces.SimilarityComputableModel;

import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

public class ModelObserver extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  private final int pid;
  private static final String PAR_FORMAT = "format";
  private final String format;
  
  
  public ModelObserver(String prefix) {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    format = Configuration.getString(prefix + "." + PAR_FORMAT, "");
  }
  
  /**
   * It observes the min and max similarities of the models of the nodes.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public boolean execute() {
    updateGraph();
    if (format.equals("gpt") && CommonState.getTime() == 0) {
      System.out.println("#iter\tavgSim\tdevSim\tminSim\tmaxSim\t# " + getClass().getCanonicalName());
    }
    double maxSim = Double.NEGATIVE_INFINITY, minSim = Double.POSITIVE_INFINITY;
    double avgSim = 0.0;
    Vector<Double> sims = new Vector<Double>();
    for (int i = 0; i < g.size(); i ++) {
      Protocol pI = ((Node) g.getNode(i)).getProtocol(pid);
      if (pI instanceof ModelHolder/*<? extends SimilarityComputableModel<I>>*/) {
        ModelHolder modelI = (ModelHolder) pI;
        for (int j = i + 1; j < g.size(); j ++) {
          ModelHolder modelJ = (ModelHolder) ((Node) g.getNode(j)).getProtocol(pid);
          double sim = Double.NaN;
          if (modelI.getModel() instanceof SimilarityComputableModel && modelJ.getModel() instanceof SimilarityComputableModel) {
            sim = ((SimilarityComputableModel)modelI.getModel()).similarity((SimilarityComputableModel) modelJ.getModel());
          }
          if (maxSim < sim) {
            maxSim = sim;
          }
          if (minSim > sim){
            minSim = sim;
          }
          avgSim += sim;
          sims.add(sim);
        }
      }
    }
    avgSim /= sims.size();
    double devSim = 0.0;
    for (double sim : sims) {
      devSim += (sim - avgSim) * (sim - avgSim); 
    }
    devSim = Math.sqrt(devSim / sims.size());
    if (CommonState.getTime() > 0) {
      if (format.equals("gpt")) {
        System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + avgSim + "\t" + devSim + "\t" + minSim + "\t" + maxSim + "\t# " + getClass().getCanonicalName());
      } else {
        System.out.println(getClass().getCanonicalName() + ":\tavgS=" + avgSim + "\tdevS=" + devSim + "\tSMin=" + minSim + "\tSMax=" + maxSim);
      }
    }
    return false;
  }

}
