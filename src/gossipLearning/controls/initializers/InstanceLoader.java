package gossipLearning.controls.initializers;

import gossipLearning.DataBaseReader;
import gossipLearning.InstanceHolder;
import gossipLearning.controls.observers.PredictionObserver;
import gossipLearning.interfaces.LearningProtocol;

import java.io.File;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

/**
 * This control reads the training and evaluation sets from files and stores them. 
 * The format of the files should be the Jochaims's file format. <br/>
 * Moreover this control loads the training instances to the nodes, and specifies the
 * evaluation set for the error observer. <br/>
 * The number of training instances per node can be parameterized, the default 
 * value is 1.
 * @author Róbert Ormándi
 *
 */
public class InstanceLoader implements Control {
  public static final String PAR_PROT = "protocol";
  public static final String PAR_TFILE = "trainingFile";
  public static final String PAR_EFILE = "evaluationFile";
  public static final String PAR_SIZE = "samplesPerNode";
  
  private final int pid;
  private final File tFile;
  private PredictionObserver observer;
  private final File eFile;
  private final int samplesPerNode;
    
  public InstanceLoader(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
    tFile = new File(Configuration.getString(prefix + "." + PAR_TFILE));
    eFile = new File(Configuration.getString(prefix + "." + PAR_EFILE));
    samplesPerNode = Configuration.getInt(prefix + "." + PAR_SIZE, 1);
  }
  
  public boolean execute(){
    try {
      // read instances
      DataBaseReader reader = DataBaseReader.createDataBaseReader(tFile, eFile);
      
      // InstanceLoader initializes the evaluation set of prediction observer
      observer.setEvalSet(reader.getEvalSet());
      
      // init the nodes by adding the instances read before
      int numOfSamples = reader.getTrainingSet().size();
      for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        Protocol protocol = node.getProtocol(pid);
        if (protocol instanceof LearningProtocol) {
          LearningProtocol learningProtocol = (LearningProtocol) protocol;
          InstanceHolder instances = new InstanceHolder();
          for (int j = 0; j < samplesPerNode; j++){
            instances.add(reader.getTrainingSet().getInstance((i * samplesPerNode + j) % numOfSamples), reader.getTrainingSet().getLabel((i * samplesPerNode + j) % numOfSamples));
          }
          
          // set the instances for current node
          learningProtocol.setInstenceHolder(instances);
        } else {
          throw new RuntimeException("The protocol " + pid + " have to implement LearningProtocol interface!");
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException("Exception has occurred in InstanceLoader!", ex);
    }
    
    return false;
  }
  
  public void setPredictionObserver(PredictionObserver observer) {
    this.observer = observer;
  }
}
