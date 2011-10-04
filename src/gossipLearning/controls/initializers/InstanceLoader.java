package gossipLearning.controls.initializers;

import gossipLearning.DataBaseReader;
import gossipLearning.controls.observers.PredictionObserver;

import java.io.File;

import peersim.config.Configuration;
import peersim.core.Control;

/**
 * This control reads the training and evaluation files and
 *  
 * 
 * @author Róbert Ormándi
 *
 */
public class InstanceLoader implements Control {
  public static final String PAR_PROT = "protocol";
  public static final String PAR_TFILE = "trainingFile";
  public static final String PAR_OBSERVER = "observer";
  public static final String PAR_EFILE = "evaluationFile";
  
  private final int pid;
  private final File tFile;
  private final PredictionObserver observer;
  private final File eFile;
    
  public InstanceLoader(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    tFile = new File(Configuration.getString(prefix + "." + PAR_TFILE));
    observer = (PredictionObserver) Configuration.getInstance(prefix + "." + PAR_OBSERVER);
    eFile = new File(Configuration.getString(prefix + "." + PAR_EFILE));
  }
  public boolean execute(){
    try {
      // read instances
      DataBaseReader reader = DataBaseReader.createDataBaseReader(tFile, eFile);
      // TODO: fill nodes and prediction observer
      /*
      
      // init the nodes by adding the instances read before
      
      for (int i = 0; i < Network.size() && i < instances.size(); i++) {
        Node node = Network.get(i);
        Protocol protocol = node.getProtocol(pid);
        if (protocol instanceof InstanceHolder) {
          InstanceHolder<I> instanceHolder = (InstanceHolder<I>) protocol;
          
          // set the current node
          instanceHolder.setInstance(instances.get(i));
          instanceHolder.setLabel(labels.get(i));
        } else if (protocol instanceof InstancesHolder) {
          InstancesHolder<I> instancesHolder = (InstancesHolder<I>) protocol;
          
          // set the current node
          instancesHolder.setNumberOfInstances(1);
          instancesHolder.setInstance(0, instances.get(i));
          instancesHolder.setLabel(0, labels.get(i));
          
        } else {
          throw new RuntimeException("The protocol " + pid + " have to implements InstanceHolder or InstancesHolder interfaces!");
        }
      }
      */
    } catch (Exception ex) {
      throw new RuntimeException("Exception has occurred in InstanceLoader!", ex);
    }
    
    return false;
  }


}
