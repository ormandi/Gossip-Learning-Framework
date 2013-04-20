package gossipLearning.controls.dynamic;

import gossipLearning.controls.InstanceLoader;
import gossipLearning.protocols.ExtractionProtocol;
import gossipLearning.protocols.LearningProtocol;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;


/**
 * This class handles the drift through dynamically changes the labels on the training 
 * and evaluation sets. The drifts on the data set is described by the number of evaluations, 
 * the number of drifts per evaluation, the number of incoming samples per evaluation and 
 * the ratio of the asynchrony.
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
 * <li>numOfEvals - the number of evaluations</li>
 * <li>driftsPerEval - the number of drifts per evaluations (drift rate)</li>
 * <li>samplesPerEval - the new sample sampling rate</li>
 * <li>asyncRate - the asynchrony rate of the drift</li>
 * </ul>
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
  
  private final long logTime;
  
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
    
    logTime = Configuration.getLong("simulation.logtime");
    
    try {
      reader = DataBaseReader.createDataBaseReader(readerClassName, tFile, eFile);
    } catch (Exception e) {
      throw new RuntimeException("Exception in " + getClass().getCanonicalName(), e);
    }
    if (reader.getTrainingSet().getNumberOfClasses() == Integer.MAX_VALUE 
        || reader.getEvalSet().getNumberOfClasses() == Integer.MAX_VALUE) {
      throw new RuntimeException("This class cannot handle regression tasks! " + getClass().getCanonicalName());
    }
  }

  /**
   * Sets new instances for nodes as the training set.<br/>
   * The floor of the specified value number of samples will be set 
   * and plus one with the remaining probability (n - floor(n)).
   * @param n amount of samples
   */
  private void changeInstances(double n){
    InstanceHolder instanceHolder;
    int sampleIndex;
    int numSamples = (int)Math.floor(n);
    int inc = 0;
    for (int nId = 0; nId < Network.size(); nId++){
      instanceHolder = ((ExtractionProtocol)(Network.get(nId)).getProtocol(pidE)).getInstanceHolder();
      if (instanceHolder == null) {
        instanceHolder = new InstanceHolder(2, reader.getTrainingSet().getNumberOfFeatures());
        ((ExtractionProtocol)(Network.get(nId)).getProtocol(pidE)).setInstanceHolder(instanceHolder);
      }
      if (CommonState.r.nextDouble() < n - numSamples) {
        inc = 1;
      } else {
        inc = 0;
      }
      if (numSamples + inc > 0) {
        instanceHolder.clear();
        for (int i = 0; i < numSamples + inc; i++) {
          sampleIndex = CommonState.r.nextInt(reader.getTrainingSet().size());
          instanceHolder.add(reader.getTrainingSet().getInstance(sampleIndex), reader.getTrainingSet().getLabel(sampleIndex));
        }
      }
    }
  }
  
  /**
   * Prints the classification performance of the models.
   * @param isPrintPrefix prints evaluation informations if it is true
   */
  private void eval(boolean isPrintPrefix){
    LearningProtocol protocol = (LearningProtocol)Network.get(0).getProtocol(pidLS[0]);
    if (isPrintPrefix) {
      for (AggregationResult result : protocol.getResults()) {
        System.out.println("#iter\t" + result.getNames());
        System.out.println((CommonState.getTime()/logTime) + "\t" + result);
      }
      isPrintPrefix = false;
    } else {
      for (AggregationResult result : protocol.getResults()) {
        System.out.println((CommonState.getTime()/logTime) + "\t" + result);
      }
    }
  }
  
  /**
   * Changes the labels on the training and evaluation sets.<br/>
   * The new label is: (label + 1) modulo numOfClasses
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
    for (int i = 0; i < Network.size(); i++) {
      for (int j = 0; j < pidLS.length; j++) {
        LearningProtocol protocol = (LearningProtocol)Network.get(i).getProtocol(pidLS[j]);
        protocol.getResults().setEvalSet(reader.getEvalSet());
      }
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
      changeInstances(1.0);
      eval(true);
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
    double amountOfIncomingSamples = actualSampleTime - prevSampleTime;
    
    changeInstances(amountOfIncomingSamples);
    
    if (isEval){
      // evaluates the models on nodes
      eval(false);
    }
    return false;
  }
}
