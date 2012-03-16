package gossipLearning.controls;

import gossipLearning.DataBaseReader;
import gossipLearning.InstanceHolder;
import gossipLearning.controls.initializers.InstanceLoader;
import gossipLearning.controls.observers.PredictionObserver;
import gossipLearning.interfaces.LearningProtocol;

import java.io.IOException;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;


/**
 * This class handles the drift through dynamically changes the labels on the training 
 * and evaluation sets. The drifts on the data set is described by the number of evaluations, 
 * the number of drifts per evaluation, the number of incoming samples per evaluation and 
 * the ratio of the asynchrony.
 * 
 * @author István Hegedűs
 *
 */
public class DynamicInstanceLoader extends InstanceLoader {
  
  private static final String PAR_NUMOFEVALS = "numOfEvals";
  private static final String PAR_DRIFTSPEREVAL = "driftsPerEval";
  private static final String PAR_SAMPLESPEREVAL = "samplesPerEval";
  private static final String PAR_ASYNCRATE = "asyncRate";
  
  private final long numOfEvals;
  private final double driftsPerEval;
  private final double samplesPerEval;
  private final double asyncRate;
  
  private final double driftLength;
  private final double driftLength1;
  
  public DynamicInstanceLoader(String prefix) {
    super(prefix);
    
    numOfEvals = Configuration.getLong(prefix + "." + PAR_NUMOFEVALS);
    driftsPerEval = Configuration.getDouble(prefix + "." + PAR_DRIFTSPEREVAL);
    samplesPerEval = Configuration.getDouble(prefix + "." + PAR_SAMPLESPEREVAL);
    asyncRate = Configuration.getDouble(prefix + "." + PAR_ASYNCRATE);
    if (asyncRate <= 0.0 || asyncRate >= 1.0){
      throw new RuntimeException("Async rate should be between 0 and 1.");
    }
    
    driftLength = (double)(CommonState.getEndTime() - 1) / (double)numOfEvals / driftsPerEval;
    driftLength1 = asyncRate * driftLength;
    
    try {
      reader = DataBaseReader.createDataBaseReader(tFile, eFile);
    } catch (IOException e) {
      throw new RuntimeException("Exception in " + getClass().getCanonicalName(), e);
    }
    if (reader.getTrainingSet().getNumberOfClasses() == Integer.MAX_VALUE 
        || reader.getEvalSet().getNumberOfClasses() == Integer.MAX_VALUE) {
      throw new RuntimeException("This class cannot handle regression tasks! " + getClass().getCanonicalName());
    }
  }

  /**
   * Sets a new instance for nodes as the training set.
   */
  private void changeInstances(){
    InstanceHolder instanceHolder;
    int sampleIndex;
    for (int nId = 0; nId < Network.size(); nId++){
      instanceHolder = ((LearningProtocol)(Network.get(nId)).getProtocol(pid)).getInstanceHolder();
      instanceHolder.clear();
      sampleIndex = CommonState.r.nextInt(reader.getTrainingSet().size());
      instanceHolder.add(reader.getTrainingSet().getInstance(sampleIndex), reader.getTrainingSet().getLabel(sampleIndex));
    }
  }
  
  /**
   * Evaluates the models using the specified observer.
   */
  private void eval(){
    /*double nNew = 0.0;
    int c = 0;
    for (int i = 0; i < Network.size(); i++) {
      LearningProtocol model = (LearningProtocol)((Node) Network.get(i)).getProtocol(pid);
      for (int mId = 0; mId < model.getModelHolder(0).size(); mId++) {
        nNew += Double.parseDouble(model.getModelHolder(0).getModel(mId).toString());
        c++;
      }
    }
    System.out.println("#" + (nNew/c));*/
    for (PredictionObserver observer : observers) {
      observer.execute();
    }
  }
  
  /**
   * Changes the labels on the training and evaluation sets.
   */
  private void changeLabels(){
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
    for (PredictionObserver observer : observers) {
      observer.setEvalSet(reader.getEvalSet());
    }
    //System.out.println("DRIFT ");
  }
  
  private boolean isChangeLabels = false;
  @Override
  public boolean execute() {
    long i = CommonState.getTime();
    if (i == 0){
      super.execute();
      // at the first time set an instance for nodes and evaluate them
      changeInstances();
      eval();
    }
    
    // checks the time for changing labels and handles the assynchronity of drift lengths
    double relativeTime = CommonState.getTime() % (long) driftLength;
    if (!isChangeLabels && relativeTime > (long)driftLength1){
      // first part of drift
      isChangeLabels = true;
      changeLabels();
    }
    if (isChangeLabels && relativeTime <= (long)driftLength1){
      // second part of drift
      isChangeLabels = false;
      changeLabels();
    }
    
    double evalsPerTick = (double)numOfEvals / (double)CommonState.getEndTime();
    
    double prevEvalTime = evalsPerTick * i;
    double actualEvalTime = evalsPerTick * (i + 1);
    boolean isEval = Math.floor(actualEvalTime) - Math.floor(prevEvalTime) > 0.0;
    
    double prevSampleTime = evalsPerTick * samplesPerEval * i;
    double actualSampleTime = evalsPerTick * samplesPerEval * (i + 1);
    boolean isIncomingSample = Math.floor(actualSampleTime) - Math.floor(prevSampleTime) > 0.0;
    
    if (isIncomingSample){
      // change samples on nodes
      changeInstances();
      //System.out.println("NEW INSATNCE");
    }
    if (isEval){
      // evaluates the models on nodes
      eval();
    }
    return false;
  }
}
