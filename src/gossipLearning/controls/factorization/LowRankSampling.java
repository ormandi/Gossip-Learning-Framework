package gossipLearning.controls.factorization;

import gossipLearning.protocols.ExtractionProtocol;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Utils;

import java.util.TreeSet;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class LowRankSampling implements Control {
  private static final String PAR_PROTE = "extractionProtocol";
  private static final String PAR_SIZE = "sampleSize";
  private static final String PAR_ISWEIGHT = "isWeighted";
  
  /** The protocol ID of the extraction protocol.*/
  protected final int pidE;
  protected final int size;
  protected final boolean isWeighted;
  
  public LowRankSampling(String prefix) {
    pidE = Configuration.getPid(prefix + "." + PAR_PROTE);
    size = Configuration.getInt(prefix + "." + PAR_SIZE);
    isWeighted = Configuration.getBoolean(prefix + "." + PAR_ISWEIGHT);
  }
  
  @Override
  public boolean execute() {
    double[] fNorms = new double[Network.size()];
    double[] cumfNorms = new double[Network.size()];
    double fNorm = 0.0;
    for (int i = 0; i < Network.size(); i++) {
      Node node = Network.get(i);
      Protocol protocol = node.getProtocol(pidE);
      if (protocol instanceof ExtractionProtocol) {
        ExtractionProtocol extractionProtocol = (ExtractionProtocol) protocol;
        InstanceHolder instances = extractionProtocol.getInstanceHolder();
        double norm = 0.0;
        for (int index = 0; index < instances.size(); index++) {
          norm = Utils.hypot(norm, instances.getInstance(index).norm());
        }
        fNorms[i] = norm * norm;
        fNorm = Utils.hypot(fNorm, norm);
        cumfNorms[i] = fNorms[i];
        if (i > 0) {
          cumfNorms[i] += cumfNorms[i-1];
        }
      }
    }
    fNorm *= fNorm;
    TreeSet<Integer> indices = new TreeSet<Integer>();
    double prob = 1.0 / Network.size();
    //double sum = 0.0;
    for (int i = 0; i < Network.size(); i++) {
      double rnd = CommonState.r.nextDouble();
      //System.out.println(i + "\t" + (fNorms[i] / fNorm));
      //sum += fNorms[i];
      if (isWeighted) {
        prob = fNorms[i] / fNorm;
      }
      if (size * prob > rnd) {
        indices.add(i);
      }
    }
    
    /*Arrays.sort(fNorms);
    for (int i = fNorms.length -1; i >= 0; i--) {
      System.out.println((fNorms.length-i) + "\t" + fNorms[i]/fNorm);
    }
    System.exit(0);
    */
    //System.out.println(indices.size());
    //System.exit(0);
    /*while (indices.size() < size) {
      double rnd = CommonState.r.nextDouble();
      for (int i = 0; i < Network.size(); i++) {
        if (cumfNorms[i] / fNorm >= rnd) {
          indices.add(i);
          break;
        }
      }
    }*/
    System.out.println("#nodes: " + indices);
    for (int i = 0; i < Network.size(); i++) {
      if (!indices.contains(i)) {
        Node node = Network.get(i);
        Protocol protocol = node.getProtocol(pidE);
        if (protocol instanceof ExtractionProtocol) {
          ExtractionProtocol extractionProtocol = (ExtractionProtocol) protocol;
          extractionProtocol.getInstanceHolder().clear();
        }
        node.setFailState(Fallible.DOWN);
      }
    }
    return false;
  }

}
