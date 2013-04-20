package gossipLearning.controls;

import gossipLearning.protocols.ExtractionProtocol;
import gossipLearning.protocols.LearningProtocol;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;

import java.io.File;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

/**
 * This control reads the training and evaluation sets from files and stores them.
 * The format of the files should be the Joachims' file format. <br/>
 * Moreover, this control loads the training instances onto the nodes, and specifies the
 * evaluation set for the error observer. <br/>
 * The number of training instances per node can be parameterized, the default
 * value is 1.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>extractionProtocol - the extraction protocol</li>
 * <li>learningProtocols - the learning protocols, separated by comma</li>
 * <li>readerClass - the database reader class name</li>
 * <li>trainingFile - the name of the training file</li>
 * <li>evaluationFile - the name of the evaluation file</li>
 * <li>samplesPerNode - the number of loaded samples per nodes</li>
 * <li>printPrecision - the number of floating points of the evaluation metric results</li>
 * <li>isPrintAges - the age of the model is printed or not</li>
 * </ul>
 * @author Róbert Ormándi
 *
 * @navassoc - - - ExtractionProtocol
 * @navassoc - - - LearningProtocol
 */
public class InstanceLoader implements Control {
  private static final String PAR_PROTE = "extractionProtocol";
  private static final String PAR_PROTLS = "learningProtocols";
  private static final String PAR_READERCLASS = "readerClass";
  private static final String PAR_TFILE = "trainingFile";
  private static final String PAR_EFILE = "evaluationFile";
  private static final String PAR_SIZE = "samplesPerNode";
  private static final String PAR_PRINTPRECISION = "printPrecision";
  private static final String PAR_ISPRINTAGES = "isPrintAges";
  
  /** The protocol ID of the extraction protocol.*/
  protected final int pidE;
  /** The array of protocol ID(s) of the learning protocol(s).*/
  protected final int[] pidLS;
  /** @hidden */
  protected final File tFile;
  /** @hidden */
  protected String readerClassName;
  protected DataBaseReader reader;
  /** @hidden */
  protected final File eFile;
  /** Specifies the number of training samples per node.*/
  protected final int samplesPerNode;
    
  /**
   * Reads the parameters from the configuration file based on the specified prefix.
   * @param prefix prefix of parameters of this class
   */
  public InstanceLoader(String prefix) {
    pidE = Configuration.getPid(prefix + "." + PAR_PROTE);
    String[] pidLSS = Configuration.getString(prefix + "." + PAR_PROTLS).split(",");
    pidLS = new int[pidLSS.length];
    for (int i = 0; i < pidLSS.length; i++) {
      pidLS[i] = Configuration.lookupPid(pidLSS[i]);
    }
    tFile = new File(Configuration.getString(prefix + "." + PAR_TFILE));
    eFile = new File(Configuration.getString(prefix + "." + PAR_EFILE));
    samplesPerNode = Configuration.getInt(prefix + "." + PAR_SIZE);
    readerClassName = Configuration.getString(prefix + "." + PAR_READERCLASS);
    AggregationResult.printPrecision = Configuration.getInt(prefix + "." + PAR_PRINTPRECISION);
    AggregationResult.isPrintAges = Configuration.getBoolean(prefix + "." + PAR_ISPRINTAGES, false);
  }
  
  public boolean execute(){
    try {
      // read instances
      reader = DataBaseReader.createDataBaseReader(readerClassName, tFile, eFile);
      
      // init the nodes by adding the instances read before
      int numOfSamples = reader.getTrainingSet().size();
      for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        Protocol protocol = node.getProtocol(pidE);
        if (protocol instanceof ExtractionProtocol) {
          ExtractionProtocol extractionProtocol = (ExtractionProtocol) protocol;
          InstanceHolder instances = new InstanceHolder(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
          for (int j = 0; j < samplesPerNode; j++){
            instances.add(reader.getTrainingSet().getInstance((i * samplesPerNode + j) % numOfSamples), reader.getTrainingSet().getLabel((i * samplesPerNode + j) % numOfSamples));
          }
          
          // set the instances for current node
          extractionProtocol.setInstanceHolder(instances);
        } else {
          throw new RuntimeException("The protocol " + pidE + " has to implement the ExtractionProtocol interface!");
        }
        
        // sets the number of classes for the learning protocols and the evaluation set for the evaluator.
        for (int j = 0; j < pidLS.length; j++) {
          protocol = node.getProtocol(pidLS[j]);
          if (protocol instanceof LearningProtocol) {
            LearningProtocol learningProtocol = (LearningProtocol) protocol;
            learningProtocol.getResults().setEvalSet(reader.getEvalSet());
            learningProtocol.setNumberOfClasses(reader.getTrainingSet().getNumberOfClasses());
          } else {
            throw new RuntimeException("The protocol " + pidE + " has to implement the LearningProtocol interface!");
          }
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException("Exception has occurred in InstanceLoader!", ex);
    }
    
    return false;
  }

}
