package gossipLearning.observers;

import gossipLearning.interfaces.ModelHolder;
import gossipLearning.observers.errorComputation.AbstractErrorComputator;
import gossipLearning.utils.database.Database;
import gossipLearning.utils.database.DatabaseReader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

public class PredictionObserver<I> extends GraphObserver {
  private static final String PAR_PROT = "protocol";
  protected final int pid;
  private static final String PAR_FORMAT = "format";
  protected final String format;
  private static final String PAR_EVAL = "eval";
  protected final String eval;
  private static final String PAR_EC = "errorComputatorClass";
  
  protected AbstractErrorComputator<I> errorComputator;
    
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PredictionObserver(String prefix) throws Exception {
    super(prefix);
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    format = Configuration.getString(prefix + "." + PAR_FORMAT, "");
    eval = Configuration.getString(prefix + "." + PAR_EVAL);
    
    // read instances and convert them to inner sparse representation
    DatabaseReader<I> reader = DatabaseReader.createReader(new File(eval)); // FIXME: get a correct reader
    Database<I> db = reader.getDatabase(); 
    Vector<I> instances = db.getInstances();
    Vector<Double> labels = db.getLabels();
    
    // create error computator
    String errorComputatorClassName = Configuration.getString(prefix + "." + PAR_EC);
    Class<? extends AbstractErrorComputator> errorCompuatorClass = (Class<? extends AbstractErrorComputator>) Class.forName(errorComputatorClassName);
    Constructor<? extends AbstractErrorComputator> errorComputatorConstructor = errorCompuatorClass.getConstructor(int.class, instances.getClass(), labels.getClass());
    errorComputator = errorComputatorConstructor.newInstance(pid, instances, labels);
  }
  
  protected Set<Integer> generateIndices() {
    TreeSet<Integer> indices = new TreeSet<Integer>();
    for (int i = 0; i < g.size(); i ++) {
      indices.add(i);
    }
    return indices;
  }
  
  @SuppressWarnings("unchecked")
  public boolean execute() {
    updateGraph();
    
    double[] errorCounter = new double[errorComputator.numberOfComputedErrors()];
    double[] avgError = new double[errorComputator.numberOfComputedErrors()];
    double[] devError = new double[errorComputator.numberOfComputedErrors()];
    double[] minAvgError = new double[errorComputator.numberOfComputedErrors()];
    double[] maxAvgError = new double[errorComputator.numberOfComputedErrors()];
    
    for (int i = 0; i < errorComputator.numberOfComputedErrors(); i ++) {
      // printHeader
      if (format.equals("gpt") && CommonState.getTime() == 0) {
        System.out.println("#iter\tavgavgE\tdevavgE\tmaxAvgE\tminAvgE\t# " + errorComputator.getClass().getCanonicalName() + "[" + i + "]");
      }
      errorCounter[i] = 0.0;
      avgError[i] = 0.0;
      devError[i] = 0.0;
      minAvgError[i] = Double.POSITIVE_INFINITY;
      maxAvgError[i] = Double.NEGATIVE_INFINITY;
    }
    
    Set<Integer> idxSet = generateIndices();
    
    for (int i : idxSet) {
      Protocol p = ((Node) g.getNode(i)).getProtocol(pid);
      if (p instanceof ModelHolder) {
        // evaluating the model of the ith node
        ModelHolder<I> model = (ModelHolder<I>) p;
        
        double[] errorVecOfNodeI = errorComputator.computeError(model, i);
        for (int j = 0; j < errorComputator.numberOfComputedErrors(); j ++) {
          // aggregate the results of nodes in term of jth eror
          errorCounter[j] ++;
          devError[j] += errorVecOfNodeI[j] * errorVecOfNodeI[j];
          avgError[j] += errorVecOfNodeI[j];
          if (errorVecOfNodeI[j] > maxAvgError[j]) {
            maxAvgError[j] = errorVecOfNodeI[j];
          }
          if (errorVecOfNodeI[j] < minAvgError[j]) {
            minAvgError[j] = errorVecOfNodeI[j];
          }
        }
      }
    }
    for (int i = 0; i < errorComputator.numberOfComputedErrors(); i ++) {
      avgError[i] = (errorCounter[i] > 0.0) ? ( avgError[i]/errorCounter[i] ) : Double.NaN;
      double sqrt = devError[i] / errorCounter[i] - avgError[i] * avgError[i];
      devError[i] = (errorCounter[i] > 0.0) ? ( (sqrt < 0.0) ? 0.0 : Math.sqrt(sqrt) ) : Double.NaN;
      
      // print info
      if (CommonState.getTime() > 0) {
        if (format.equals("gpt")) {
          //System.out.println(CommonState.getTime() + "\t" + Configuration.getLong("simulation.logtime"));
          System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + avgError[i] + "\t" + devError[i] + "\t" + maxAvgError[i] + "\t" + minAvgError[i] + "\t# " + errorComputator.getClass().getCanonicalName() + "[" + i + "]");
        } else {
          System.out.println(errorComputator.getClass().getCanonicalName() + "[" + i + "]" + ":\tAvgE=" + avgError[i] + "\tDevE=" + devError[i] + "\tMaxE=" + maxAvgError[i] + "\tMinE=" + minAvgError[i]);
        }
      }
    }
    
    return false;
  }

}
