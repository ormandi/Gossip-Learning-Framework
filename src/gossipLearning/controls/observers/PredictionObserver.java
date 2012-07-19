package gossipLearning.controls.observers;

import gossipLearning.InstanceHolder;
import gossipLearning.controls.observers.errorComputation.AbstractErrorComputator;
import gossipLearning.controls.observers.errorComputation.ErrorFunction;
import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.ModelHolder;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

/**
 * This class computes the prediction error of the nodes in the network
 * based on the specified type of error computator and error function. The
 * computer prediction error will be written on the output channel.
 * @author István Hegedűs
 * @has 1 "" 1 InstanceHolder
 * 
 */
public class PredictionObserver extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  /**
   * The protocol ID.
   */
  protected final int pid;
  private static final String PAR_FORMAT = "format";
  /**
   * The type of print format.
   * @hidden
   */
  protected final String format;
  private static final String PAR_EC = "errorComputatorClass";
  private static final String PAR_EF = "errorFunctionClass";
  
  /**
   * The error computator class used.
   * @hidden
   */
  protected AbstractErrorComputator errorComputator;
  
  /**
   * The error function used.
   * @hidden
   */
  protected ErrorFunction errorFunction;
  
  /** @hidden */
  protected Constructor<? extends AbstractErrorComputator> errorComputatorConstructor;
  /** @hidden */
  protected InstanceHolder eval;
  /** @hidden */
  private static final String PAR_SUFFIX = "suffix";
  protected String printSuffix = "";
    
  @SuppressWarnings("unchecked")
  public PredictionObserver(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    format = Configuration.getString(prefix + "." + PAR_FORMAT, "");
    printSuffix = Configuration.getString(prefix + "." + PAR_SUFFIX, "");
    
    // create error computator
    String errorComputatorClassName = Configuration.getString(prefix + "." + PAR_EC);
    Class<? extends AbstractErrorComputator> errorCompuatorClass = (Class<? extends AbstractErrorComputator>) Class.forName(errorComputatorClassName);
    errorComputatorConstructor = errorCompuatorClass.getConstructor(InstanceHolder.class, ErrorFunction.class);
    String errorFunctionClassName = Configuration.getString(prefix + "." + PAR_EF);
    errorFunction = (ErrorFunction) Class.forName(errorFunctionClassName).newInstance();
  }
  
  /**
   * Returns the set of the node indices in the graph.
   * @return set of node indices
   */
  protected Set<Integer> generateIndices() {
    TreeSet<Integer> indices = new TreeSet<Integer>();
    for (int i = 0; i < g.size(); i ++) {
      indices.add(i);
    }
    return indices;
  }
  
  public boolean execute() {
    try {
      errorComputator = errorComputatorConstructor.newInstance(eval, errorFunction);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    updateGraph();
    if (format.equals("gpt") && CommonState.getTime() == 0) {
      System.out.println("#iter\tavgavgE\tdevavgE\tmaxAvgE\tminAvgE" + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# ") + errorComputator.getClass().getCanonicalName() + "[NumOfVotes]" + "\t[HolderIndex]");
    }
    
    Vector<Vector<Double>> errorCounter = new Vector<Vector<Double>>();
    Vector<Vector<Double>> avgError = new Vector<Vector<Double>>();
    Vector<Vector<Double>> devError = new Vector<Vector<Double>>();
    Vector<Vector<Double>> minAvgError = new Vector<Vector<Double>>();
    Vector<Vector<Double>> maxAvgError = new Vector<Vector<Double>>();
    
    Set<Integer> idxSet = generateIndices();
    
    for (int i : idxSet) {
      Protocol p = ((Node) g.getNode(i)).getProtocol(pid);
      if (p instanceof LearningProtocol) {
        // evaluating the model(s) of the ith node
        int numOfHolders = ((LearningProtocol)p).size();
        for (int holderIndex = 0; holderIndex < numOfHolders; holderIndex++){
          // initializing the statistics container
          if (errorCounter.size() <= holderIndex){
            errorCounter.add(new Vector<Double>());
            avgError.add(new Vector<Double>());
            devError.add(new Vector<Double>());
            maxAvgError.add(new Vector<Double>());
            minAvgError.add(new Vector<Double>());
          }
          ModelHolder modelHolder = ((LearningProtocol)p).getModelHolder(holderIndex);
          double[] errorVecOfNodeI = errorComputator.computeError(modelHolder);
          for (int j = 0; j < errorVecOfNodeI.length; j ++) {
            // aggregate the results of nodes in term of jth error
            if (errorCounter.get(holderIndex).size() <= j){
              errorCounter.get(holderIndex).add(1.0);
              avgError.get(holderIndex).add(errorVecOfNodeI[j]);
              devError.get(holderIndex).add(errorVecOfNodeI[j] * errorVecOfNodeI[j]);
              maxAvgError.get(holderIndex).add(errorVecOfNodeI[j]);
              minAvgError.get(holderIndex).add(errorVecOfNodeI[j]);
            } else {
              errorCounter.get(holderIndex).set(j, errorCounter.get(holderIndex).get(j) + 1.0);
              avgError.get(holderIndex).set(j, avgError.get(holderIndex).get(j) + errorVecOfNodeI[j]);
              devError.get(holderIndex).set(j, devError.get(holderIndex).get(j) + (errorVecOfNodeI[j] * errorVecOfNodeI[j]));
              if (maxAvgError.get(holderIndex).get(j) < errorVecOfNodeI[j]){
                maxAvgError.get(holderIndex).set(j, errorVecOfNodeI[j]);
              }
              if (minAvgError.get(holderIndex).get(j) > errorVecOfNodeI[j]){
                minAvgError.get(holderIndex).set(j, errorVecOfNodeI[j]);
              }
            }
          }
        }
      }
    }
    for (int i = 0; i < errorCounter.size(); i ++) {
      for (int j = 0; j < errorCounter.get(i).size(); j++){
        // errorCounter.get(i).get(j) is never less or equal to 0
        avgError.get(i).set(j, avgError.get(i).get(j) / errorCounter.get(i).get(j));
        double sqrt = devError.get(i).get(j) / errorCounter.get(i).get(j) - avgError.get(i).get(j) * avgError.get(i).get(j);
        sqrt = sqrt < 0.0 ? 0.0 : Math.sqrt(sqrt);
        devError.get(i).set(j, sqrt);
        
        // print info
        if (CommonState.getTime() > 0) {
          if (format.equals("gpt")) {
            //System.out.println(CommonState.getTime() + "\t" + Configuration.getLong("simulation.logtime"));
            System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + avgError.get(i).get(j) + "\t" + devError.get(i).get(j) + "\t" + maxAvgError.get(i).get(j) + "\t" + minAvgError.get(i).get(j) + ((printSuffix.length() > 0) ? "\t# " + printSuffix + " ": "\t# ") +  getClass().getCanonicalName() +  " - " + errorComputator.getClass().getCanonicalName() + "[" + j + "]\t[" + i + "]");
          } else {
            System.out.println(getClass().getCanonicalName() + " - " + errorComputator.getClass().getCanonicalName() + "[" + j + "]\t[" + i + "]" + ":\tAvgE=" + avgError.get(i).get(j) + "\tDevE=" + devError.get(i).get(j) + "\tMaxE=" + maxAvgError.get(i).get(j) + "\tMinE=" + minAvgError.get(i).get(j) + ((printSuffix.length() > 0) ? "\t# " + printSuffix : "") );
          }
        }
      }
    }
    return false;
  }

  /**
   * Returns the instances and corresponding class labels of the evaluation set.
   * @return evaluation set.
   */
  public InstanceHolder getEvalSet() {
    return eval;
  }

  /**
   * Stores the specified instances and corresponding labels as evaluation set.
   * @param instances instances for evaluation.
   */
  public void setEvalSet(InstanceHolder eval) {
    this.eval = eval;
  }

  /**
   * Returns the current value of print suffix which is appended to each result line.
   * By default it is an empty string
   * @return print suffix
   */
  public String getPrintSuffix() {
    return printSuffix;
  }
  
  /**
   * Sets the value of print suffix. This will be appended to each result line (after a '#').
   * @param printSuffix new value of result line suffix
   */
  public void setPrintSuffix(String printSuffix) {
    this.printSuffix = printSuffix;
  }
}
