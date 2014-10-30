package gossipLearning.controls.factorization;

import gossipLearning.evaluators.LowRankResultAggregator;
import gossipLearning.protocols.ExtractionProtocol;
import gossipLearning.protocols.LearningProtocol;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.io.IOException;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

/**
 * This class loads the rows of a matrix (A) to be factorized.<br/>
 * A is constructed from matrices U, S and V as A=USV^T.<br/>
 * U, S and V matrices are loaded from the specified files.
 * @author István Hegedűs
 */
public class LowRankLoaderUSV implements Control {
  
  private static final String PAR_PIDE = "extractionProtocol";
  protected final int pidE;
  private static final String PAR_PIDLS = "learningProtocols";
  protected final int[] pidLS;
  private static final String PAR_U = "U";
  private static final String PAR_S = "S";
  private static final String PAR_V = "V";
  private static final String PAR_PRINTPREC = "printPrecision";
  
  protected final Matrix U;
  protected final Matrix V;
  protected final Matrix S;
  protected final Matrix M;

  public LowRankLoaderUSV(String prefix) throws IOException {
    pidE = Configuration.getPid(prefix + "." + PAR_PIDE);
    String[] pidLSS = Configuration.getString(prefix + "." + PAR_PIDLS).split(",");
    pidLS = new int[pidLSS.length];
    for (int i = 0; i < pidLSS.length; i++) {
      pidLS[i] = Configuration.lookupPid(pidLSS[i]);
    }
    AggregationResult.printPrecision = Configuration.getInt(prefix + "." + PAR_PRINTPREC);
    
    // loading matrices and constructing M
    S = new Matrix(new File(Configuration.getString(prefix + "." + PAR_S)));
    U = new Matrix(new File(Configuration.getString(prefix + "." + PAR_U)));
    V = new Matrix(new File(Configuration.getString(prefix + "." + PAR_V)));
    M = U.mul(S).mul(V.transpose());
    V.transpose();
  }
  
  @Override
  public boolean execute() {
 // load rows of the matrix to the nodes
    InstanceHolder instanceHolder;
    SparseVector instance;
    double label = 0.0;
    if (M.getRowDimension() != Network.size()) {
      throw new RuntimeException("The row dimension (" + M.getRowDimension() + ") of the matrix and the number of nodes (" + Network.size() + ") should be the same!");
    }
    for (int nId = 0; nId < Network.size(); nId++){
      instanceHolder = ((ExtractionProtocol)(Network.get(nId)).getProtocol(pidE)).getInstanceHolder();
      if (instanceHolder == null) {
        instanceHolder = new InstanceHolder(0, Network.size());
        ((ExtractionProtocol)(Network.get(nId)).getProtocol(pidE)).setInstanceHolder(instanceHolder);
      }
      instanceHolder.clear();
      instance = new SparseVector(M.getRow(nId));
      instanceHolder.add(instance, label);
    }
    // load decomposed matrices as the evalset
    for (int i = 0; i < Network.size(); i++) {
      for (int j = 0; j < pidLS.length; j++) {
        LearningProtocol protocol = (LearningProtocol)Network.get(i).getProtocol(pidLS[j]);
        ((LowRankResultAggregator)protocol.getResults()).setEvalSet(U, V, S);
      }
    }
    return false;
  }
  
}
