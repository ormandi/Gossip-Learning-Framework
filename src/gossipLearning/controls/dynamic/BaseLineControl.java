package gossipLearning.controls.dynamic;

import gossipLearning.protocols.ExtractionProtocol;
import gossipLearning.protocols.LearningProtocol;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

/**
 * This class models the problem of drifting concepts using the moving hyperplane 
 * approach and measures the performance by calls the execute procedure of the 
 * defined observers.</br>
 * Defines samples in the d dimensional unit hypercube and two hypesrplanes 
 * , go through the origin, that are orthogonal to each other. The concept moves 
 * between these hyperplanes periodically and specifies the labelling of the samples. 
 * The move of the hyperplane can be sudden of incremental.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>extractionProtocol - the extraction protocol</li>
 * <li>learningProtocols - the learning protocols, separated by comma</li>
 * <li>numOfEvals - the number of evaluations</li>
 * <li>driftsPerEval - the number of drifts per evaluations (drift rate)</li>
 * <li>samplesPerEval - the new sample sampling rate</li>
 * <li>asyncRate - the asynchrony rate of the drift</li>
 * <li>dimension - the dimension of the samples</li>
 * <li>numOfInstances - the number of the used instances</li>
 * <li>isSudden - the drift will be sudden of continuous</li>
 * <li>isNoise - adding noise or not</li>
 * <li>printPrecision - the number of floating points of the evaluation metric results</li>
 * </ul>
 * @author István Hegedűs
 */
public class BaseLineControl implements Control {
  
  private static final String PAR_PIDE = "extractionProtocol";
  protected final int pidE;
  private static final String PAR_PIDLS = "learningProtocols";
  protected final int[] pidLS;
  
  private static final String PAR_NUMOFEVALS = "numOfEvals";
  private static final String PAR_DRIFTSPEREVAL = "driftsPerEval";
  private static final String PAR_SAMPLESPEREVAL = "samplesPerEval";
  private static final String PAR_ASYNCRATE = "asyncRate";
  
  private static final String PAR_DIMENSION = "dimension";
  private static final String PAR_NUMOFINSTANCES = "numOfInstances";
  private static final String PAR_ISSUDDEN = "isSudden";
  private static final String PAR_ISNOISE = "isNoise";
  private static final String PAR_PRINTPRECISION = "printPrecision";
  private static final String PAR_ISPRINTAGES = "isPrintAges";
  
  private final long numOfEvals;
  private final double driftsPerEval;
  private final double samplesPerEval;
  private final double asyncRate;
  
  private final double driftLength;
  private final double driftLength1;
  
  private final int dimension;
  private final int numOfInstances;
  private final boolean isSudden;
  private final boolean isNoise;
  
  private InstanceHolder training;
  private InstanceHolder evaluation;
  
  private double[] from;
  private double[] to;
  private SparseVector w;
  
  /**
   * Constructor that initializes the parameters from the configuration file based on 
   * the specified prefix.
   * @param prefix
   */
  public BaseLineControl(String prefix){
    pidE = Configuration.getPid(prefix + "." + PAR_PIDE);
    String[] pidLSS = Configuration.getString(prefix + "." + PAR_PIDLS).split(",");
    pidLS = new int[pidLSS.length];
    for (int i = 0; i < pidLSS.length; i++) {
      pidLS[i] = Configuration.lookupPid(pidLSS[i]);
    }
    
    numOfEvals = Configuration.getLong(prefix + "." + PAR_NUMOFEVALS);
    driftsPerEval = Configuration.getDouble(prefix + "." + PAR_DRIFTSPEREVAL);
    samplesPerEval = Configuration.getDouble(prefix + "." + PAR_SAMPLESPEREVAL);
    asyncRate = Configuration.getDouble(prefix + "." + PAR_ASYNCRATE);
    if (asyncRate <= 0.0 || asyncRate >= 1.0){
      throw new RuntimeException("Async rate should be between 0 and 1");
    }
    
    driftLength = (double)(CommonState.getEndTime() - 1) / (double)numOfEvals / driftsPerEval;
    driftLength1 = asyncRate * driftLength;
    
    dimension = Configuration.getInt(prefix + "." + PAR_DIMENSION);
    numOfInstances = Configuration.getInt(prefix + "." + PAR_NUMOFINSTANCES);
    isSudden = Configuration.getBoolean(prefix + "." + PAR_ISSUDDEN);
    isNoise = Configuration.getBoolean(prefix + "." + PAR_ISNOISE);
    
    AggregationResult.printPrecision = Configuration.getInt(prefix + "." + PAR_PRINTPRECISION);
    AggregationResult.isPrintAges = Configuration.getBoolean(prefix + "." + PAR_ISPRINTAGES, false);
    
    if (dimension < 2) {
      throw new RuntimeException("The number of dimensions should be greater then 1: " + dimension);
    }
    
    from = new double[dimension];
    to = new double[dimension];
    w = new SparseVector(dimension);
    
    // generate from and to hyperPlanes
    from[0] = 1.0;
    from[1] = 0.0;
    to[0] = 0.0;
    to[1] = 1.0;
    for (int d = 2; d < dimension; d++){
      if (CommonState.r.nextBoolean()){
        from[d] = 1.0;
        to[d] = 0.0;
      }else{
        from[d] = 0.0;
        to[d] = 1.0;
      }
      w.put(d, from[d]);
    }
    // generate random instances in N[-1.0,1.0]^d
    training = new InstanceHolder(2, dimension);
    evaluation = new InstanceHolder(2, dimension);
    SparseVector instance;
    double label;
    double dotProd;
    for (int i = 0; i < numOfInstances; i++){
      instance = new SparseVector(dimension);
      for (int d = 0; d < dimension; d++){
        if (d == 0) {
          instance.put(d, CommonState.r.nextDouble());
        } else {
          instance.put(d, (CommonState.r.nextDouble() * 2.0) - 1.0);
        }
        //instance.put(d, (CommonState.r.nextDouble() * 2.0) - 1.0);
      }
      dotProd = w.mul(instance);
      label = dotProd < 0.0 ? 0.0 : 1.0;
      if (isNoise && Math.abs(dotProd / w.norm())*10.0 < CommonState.r.nextDouble()){
        label = 1.0 - label;
      }
      if (CommonState.r.nextDouble() < 0.2) {
        evaluation.add(instance, label);
      } else {
        training.add(instance, label);
      }
    }
  }
  
  private double prevAlpha = 0.0;
  private void changeLabels(double alpha) {
    if (alpha == prevAlpha) {
      return;
    }
    prevAlpha = alpha;
    // change the hyperPlane
    for (int d = 0; d < dimension; d++){
      w.put(d, (1.0 - alpha)*from[d] + alpha*to[d]);
    }
    double value;
    double label;
    double dotProd;
    // actualize the class labels
    for (int i = 0; i < training.size(); i++){
      dotProd = w.mul(training.getInstance(i));
      value = dotProd < 0.0 ? 0.0 : 1.0;
      label = value;
      if (isNoise && Math.abs(dotProd / w.norm())*10.0 < CommonState.r.nextDouble()){
        label = 1.0 - label;
        //System.out.println("#NOISE");
      }
      training.setLabel(i, label);
    }
    for (int i = 0; i < evaluation.size(); i++){
      dotProd = w.mul(evaluation.getInstance(i));
      value = dotProd < 0.0 ? 0.0 : 1.0;
      label = value;
      if (isNoise && Math.abs(dotProd / w.norm())*10.0 < CommonState.r.nextDouble()){
        label = 1.0 - label;
        //System.out.println("#NOISE");
      }
      evaluation.setLabel(i, label);
    }
    for (int i = 0; i < Network.size(); i++) {
      for (int j = 0; j < pidLS.length; j++) {
        LearningProtocol protocol = (LearningProtocol)Network.get(i).getProtocol(pidLS[j]);
        protocol.getResults().setEvalSet(evaluation);
      }
    }
    //System.err.println("NUMOFLABELCHANGES=" + numOfChanges + "\tSIMILARITY=" + Utils.computeSimilarity(wOld, w) + "\tPOSITIVERATIO=" + numOfPosLabels/numOfInstances);
  }
  
  /**
   * Adds the specified number of new training instances to the nodes in the network.
   * @param n number of samples to be added
   */
  protected void changeInstances(double n){
    InstanceHolder instanceHolder;
    int sampleIndex;
    int numSamples = (int)Math.floor(n);
    int inc = 0;
    for (int nId = 0; nId < Network.size(); nId++){
      instanceHolder = ((ExtractionProtocol)(Network.get(nId)).getProtocol(pidE)).getInstanceHolder();
      if (instanceHolder == null) {
        instanceHolder = new InstanceHolder(2, dimension);
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
          sampleIndex = CommonState.r.nextInt(training.size());
          instanceHolder.add(training.getInstance(sampleIndex), training.getLabel(sampleIndex));
        }
      }
    }
  }
  
  /**
   * Prints the classification performance of the models.
   * @param isPrintPrefix prints evaluation informations if it is true
   */
  protected void eval(boolean isPrintPrefix){
    LearningProtocol protocol = (LearningProtocol)Network.get(0).getProtocol(pidLS[0]);
    if (isPrintPrefix) {
      for (AggregationResult result : protocol.getResults()) {
        System.out.println("#iter\t" + result.getNames());
      }
      isPrintPrefix = false;
    }
    for (AggregationResult result : protocol.getResults()) {
      System.out.println((CommonState.getTime()/Configuration.getLong("simulation.logtime")) + "\t" + result);
    }
  }
  
  private boolean isChangeLabels = false;
  public boolean execute() {
    long i = CommonState.getTime();
    if (i == 0){
      for (int id = 0; id < Network.size(); id ++) {
        for (int j = 0; j < pidLS.length; j++) {
          LearningProtocol protocol = (LearningProtocol)Network.get(id).getProtocol(pidLS[j]);
          protocol.getResults().setEvalSet(evaluation);
          protocol.setNumberOfClasses(training.getNumberOfClasses());
        }
      }
      // at the first time set an instance for nodes and evaluate them
      changeInstances(1.0);
      eval(true);
    }
    
    // checks the time for changing labels and handles the assynchronity of drift lengths
    double relativeTime = CommonState.getTime() % (long) driftLength;
    if (isSudden) {
      if (!isChangeLabels && relativeTime > (long)driftLength1){
        // first part of drift
        isChangeLabels = true;
        changeLabels(1.0);
      }
      if (isChangeLabels && relativeTime <= (long)driftLength1){
        // second part of drift
        isChangeLabels = false;
        changeLabels(0.0);
      }
    } else {
      if (relativeTime > (long)driftLength1) {
        changeLabels(1.0 - ((relativeTime - driftLength1) / (driftLength - driftLength1)));
      } else {
        changeLabels(relativeTime / driftLength1);
      }
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
