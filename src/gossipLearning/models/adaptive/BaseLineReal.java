package gossipLearning.models.adaptive;

import gossipLearning.DataBaseReader;
import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class models the problem of drifting concepts using a real dataset 
 * and measures the performance of some baseline algorithms.
 * The drift is modeled by changing the class labels cyclically.
 * @author István Hegedűs
 *
 */
public class BaseLineReal extends BaseLine {
  
  private DataBaseReader reader;

  @SuppressWarnings("unchecked")
  public BaseLineReal(long numOfEvals, double driftsPerEval, double samplesPerEval, double asyncRate, int numOfLearners, String trainName, String testName) throws Exception {
    super(numOfEvals, driftsPerEval, samplesPerEval, asyncRate, 2, 0, true, false, numOfLearners);
    
    reader = DataBaseReader.createDataBaseReader(new File(trainName), new File(testName));
    training = reader.getTrainingSet();
    evaluation = reader.getEvalSet();
    numOfClasses = training.getNumberOfClasses();
    
    
    w = null;
    isClear = new boolean[numOfLearners];
    Arrays.fill(isClear, false);
    classifiers = new Model[numOfLearners];
    globalClassifier = (Model)Class.forName(modelName).newInstance();
    globalClassifier.setNumberOfClasses(numOfClasses);
    cacheClassifiers = new Model[numOfLearners];
    localTrainSets = new InstanceHolder[numOfLearners];
    globalTrainSet = new InstanceHolder(numOfClasses, training.getNumberOfFeatures());
    cacheTrainSets = new InstanceHolder[numOfLearners];
    
    localModelCache = new List[numOfLearners];
    cacheModelCache = new List[numOfLearners];
    for (int i = 0; i < numOfLearners; i++){
      classifiers[i] = (Model)Class.forName(modelName).newInstance();
      classifiers[i].setNumberOfClasses(numOfClasses);
      cacheClassifiers[i] = (Model)Class.forName(modelName).newInstance();
      cacheClassifiers[i].setNumberOfClasses(numOfClasses);
      localTrainSets[i] = new InstanceHolder(numOfClasses, training.getNumberOfFeatures());
      cacheTrainSets[i] = new InstanceHolder(numOfClasses, training.getNumberOfFeatures());
      
      localModelCache[i] = new LinkedList<Model>();
      cacheModelCache[i] = new LinkedList<Model>();
    }
    
  }
  
  protected void changeLabels(double alpha){
    if (alpha == prevAlpha || alpha != Math.floor(alpha)) {
      return;
    }
    prevAlpha = alpha;
    
    double label;
    int nc;
    for (int i = 0; i < reader.getTrainingSet().size(); i++){
      label = reader.getTrainingSet().getLabel(i);
      nc = reader.getTrainingSet().getNumberOfClasses();
      reader.getTrainingSet().setLabel(i, ((int)label + 1.0)%nc);
    }
    for (int i = 0; i < reader.getEvalSet().size(); i++){
      label = reader.getEvalSet().getLabel(i);
      nc = reader.getTrainingSet().getNumberOfClasses();
      reader.getEvalSet().setLabel(i, ((int)label + 1.0)%nc);
    }
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 7){
      System.err.println("NumOfEvals DriftsPerEval SamplesPerEval AsyncRate NumOfLearners TrainFileName TestFileName");
      return;
    }
    int numOfEvals = Integer.parseInt(args[0]);
    double driftsPerEval = Double.parseDouble(args[1]);
    double samplesPerEval = Double.parseDouble(args[2]);
    double asyncRate = Double.parseDouble(args[3]);
    
    int numOfLearners = Integer.parseInt(args[4]);
    String trainName = args[5];
    String testName = args[6];
    
    BaseLineReal bl = new BaseLineReal(numOfEvals, driftsPerEval, samplesPerEval, asyncRate, numOfLearners, trainName, testName);
    bl.run();
  }

}
