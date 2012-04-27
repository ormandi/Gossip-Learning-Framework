package gossipLearning.models.adaptive;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BaseLine {
  private static final long seed = 1234567890;
  private static final String modelName = "gossipLearning.models.multiClassLearners.MultiLogReg";
  
  private final long numOfEvals;
  //private final double driftsPerEval;
  private final double samplesPerEval;
  //private final double asyncRate;
  
  private final double driftLength;
  private final double driftLength1;
  
  private final int dimension;
  protected final int numOfInstances;
  private final boolean isSudden;
  private final boolean isNoise;
  
  private final int numOfLearners;
  
  private final double[] from;
  private final double[] to;
  private SparseVector w;
  protected InstanceHolder training;
  protected InstanceHolder evaluation;
  private final Random r;
  
  private boolean[] isClear;
  private Model[] classifiers;
  private Model globalClassifier;
  private Model[] cacheClassifiers;
  private final int cacheLearnerCacheSize = 100;
  private InstanceHolder[] localTrainSets;
  private InstanceHolder globalTrainSet;
  private InstanceHolder[] cacheTrainSets;
  private boolean isNewCacheSample = true;
  
  private List<Model>[] localModelCache;
  private List<Model>[] cacheModelCache;
  
  @SuppressWarnings("unchecked")
  public BaseLine(long numOfEvals, double driftsPerEval, double samplesPerEval, double asyncRate, int dimension, int numOfInstances, boolean isSudden, boolean isNoise, int numOfLearners) throws Exception {
    r = new Random(seed);
    this.numOfEvals = numOfEvals;
    //this.driftsPerEval = driftsPerEval;
    this.samplesPerEval = samplesPerEval;
    //this.asyncRate = asyncRate;
    
    //driftLength = 1.0 / driftsPerEval;
    driftLength = numOfLearners / driftsPerEval;
    driftLength1 = asyncRate * driftLength;
    
    this.dimension = dimension;
    this.numOfInstances = numOfInstances;
    this.isSudden = isSudden;
    this.isNoise = isNoise;
    
    this.numOfLearners = numOfLearners;
    
    from = new double[dimension];
    to = new double[dimension];
    w = new SparseVector(dimension);
    training = new InstanceHolder(2);
    evaluation = new InstanceHolder(2);
    
    isClear = new boolean[numOfLearners];
    Arrays.fill(isClear, false);
    classifiers = new Model[numOfLearners];
    globalClassifier = (Model)Class.forName(modelName).newInstance();
    globalClassifier.setNumberOfClasses(2);
    cacheClassifiers = new Model[numOfLearners];
    localTrainSets = new InstanceHolder[numOfLearners];
    globalTrainSet = new InstanceHolder(2);
    cacheTrainSets = new InstanceHolder[numOfLearners];
    
    localModelCache = new List[numOfLearners];
    cacheModelCache = new List[numOfLearners];
    for (int i = 0; i < numOfLearners; i++){
      classifiers[i] = (Model)Class.forName(modelName).newInstance();
      classifiers[i].setNumberOfClasses(2);
      cacheClassifiers[i] = (Model)Class.forName(modelName).newInstance();
      cacheClassifiers[i].setNumberOfClasses(2);
      localTrainSets[i] = new InstanceHolder(2);
      cacheTrainSets[i] = new InstanceHolder(2);
      
      localModelCache[i] = new LinkedList<Model>();
      cacheModelCache[i] = new LinkedList<Model>();
    }
    
    // generate from and to hyperPlanes
    from[0] = 1.0;
    from[1] = 0.0;
    to[0] = 0.0;
    to[1] = 1.0;
    for (int d = 2; d < dimension; d++){
      if (r.nextBoolean()){
        from[d] = 1.0;
        to[d] = 0.0;
      }else{
        from[d] = 0.0;
        to[d] = 1.0;
      }
      w.put(d, from[d]);
    }
    // generate random instances in N[-1.0,1.0]^d
    double label;
    double dotProd;
    SparseVector instance;
    for (int i = 0; i < numOfInstances; i++){
      instance = new SparseVector(dimension);
      for (int d = 0; d < dimension; d++){
        if (d == 0) {
          instance.put(d, r.nextDouble());
        } else {
          instance.put(d, (r.nextDouble() * 2.0) - 1.0);
        }
        //instance.put(d, (CommonState.r.nextDouble() * 2.0) - 1.0);
      }
      dotProd = w.mul(instance);
      label = dotProd < 0.0 ? 0.0 : 1.0;
      if (isNoise && Math.abs(dotProd / w.norm())*10.0 < r.nextDouble()){
        label = 1.0 - label;
      }
      if (r.nextDouble() < 0.2) {
        evaluation.add(instance, label);
      } else {
        training.add(instance, label);
      }
    }
    
  }
  
  private double prevAlpha = 0.0;
  private void changeLabels(double alpha) {
    //System.out.println("CHANGE LABELS " + alpha + " " + prevAlpha);
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
      if (isNoise && Math.abs(dotProd / w.norm())*10.0 < r.nextDouble()){
        label = 1.0 - label;
        //System.out.println("#NOISE");
      }
      training.setLabel(i, label);
    }
    for (int i = 0; i < evaluation.size(); i++){
      dotProd = w.mul(evaluation.getInstance(i));
      value = dotProd < 0.0 ? 0.0 : 1.0;
      label = value;
      if (isNoise && Math.abs(dotProd / w.norm())*10.0 < r.nextDouble()){
        label = 1.0 - label;
        //System.out.println("#NOISE");
      }
      evaluation.setLabel(i, label);
    }
    //System.err.println("NUMOFLABELCHANGES=" + numOfChanges + "\tSIMILARITY=" + Utils.computeSimilarity(wOld, w) + "\tPOSITIVERATIO=" + numOfPosLabels/numOfInstances);
  }
  
  /*private void clear(){
    // clear the local train sets
    for (int i = 0; i < numOfLearners; i++){
      localTrainSets[i].clear();
    }
    // clear the global train set
    globalTrainSet.clear();
  }*/
  
  /*private static TreeMap<Integer, Double> cloneMap(Map<Integer, Double> map) {
    TreeMap<Integer, Double> result = new TreeMap<Integer, Double>();
    for (int k : map.keySet()) {
      result.put(k, map.get(k).doubleValue());
    }
    
    return result;
  }*/
  
  private void changeInstances(double n) {
    // select a random sample for local and global learners
    int sampleIndex;
    SparseVector instance;
    double label;
    int numSamples = (int)Math.floor(n);
    int inc = 0;
    for (int i = 0; i < numOfLearners; i++){
      if (r.nextDouble() < n - numSamples) {
        inc = 1;
      } else {
        inc = 0;
      }
      if (isClear[i]) {
        if (numSamples + inc > 0) {
          localTrainSets[i].clear();
          isClear[i] = false;
        }
      }
      for (int j = 0; j < numSamples + inc; j++) {
        sampleIndex = r.nextInt(training.size());
        instance = training.getInstance(sampleIndex);
        label = training.getLabel(sampleIndex);
        cacheTrainSets[i].add((SparseVector)instance.clone(), label);
        isNewCacheSample = true;
        if (cacheTrainSets[i].size() > cacheLearnerCacheSize){
          cacheTrainSets[i].remove(0);
        }
        localTrainSets[i].add((SparseVector)instance.clone(), label);
        //globalTrainSet.add((SparseVector)instance.clone(), label);
      }
    }
  }
  
  private void trainModel(Model model, InstanceHolder samples) {
    double numOfIters = 10 * samples.size();
    SparseVector instance;
    double label;
    for (int iter = 0; iter < numOfIters; iter++){
      instance = samples.getInstance(iter % samples.size());
      label = samples.getLabel(iter % samples.size());
      model.update(instance, label);
    }
  }
  
  private void train() throws Exception {
    for (int i = 0; i < numOfLearners; i++){
      // reset learner i
      if (localTrainSets[i].size() > 0){
        classifiers[i] = (Model)Class.forName(modelName).newInstance();
        classifiers[i].setNumberOfClasses(2);
        // train local model i
        trainModel(classifiers[i], localTrainSets[i]);
        int nodeId = r.nextInt(numOfLearners);
        localModelCache[nodeId].add((Model)classifiers[i].clone());
        if (localModelCache[nodeId].size() > numOfLearners){
          localModelCache[nodeId].remove(0);
        }
        for (int iid = 0; iid < localTrainSets[i].size(); iid++) {
          globalTrainSet.add(localTrainSets[i].getInstance(iid), localTrainSets[i].getLabel(iid));
        }
      }
      // reset cache learner i
      if (isNewCacheSample){
        cacheClassifiers[i] = (Model)Class.forName(modelName).newInstance();
        cacheClassifiers[i].setNumberOfClasses(2);
        // train cache model i
        trainModel(cacheClassifiers[i], cacheTrainSets[i]);
      }
      int nodeId = r.nextInt(numOfLearners);
      cacheModelCache[nodeId].add((Model)cacheClassifiers[i].clone());
      if (cacheModelCache[nodeId].size() > numOfLearners){
        cacheModelCache[nodeId].remove(0);
      }
    }
    
    if (globalTrainSet.size() > 0){
      // reset global learner
      globalClassifier = (Model)Class.forName(modelName).newInstance();
      globalClassifier.setNumberOfClasses(2);
      // train global model
      trainModel(globalClassifier, globalTrainSet);
      globalTrainSet.clear();
    }
    isNewCacheSample = false;
    
  }
  
  private void evaluate(int iteration, double alpha) {
    double prediction;
    double cachePrediction;
    double votedPrediction;
    double votedCachePrediction;
    double globalPrediction;
    double delayPrediction;
    double delayCahcePrediction;
    
    double error = 0.0;
    double cahceError = 0.0;
    double votedError = 0.0;
    double votedCacheError = 0.0;
    double globalError = 0.0;
    double delayError = 0.0;
    double delayCacheError = 0.0;
    
    if (iteration == 1){
      System.out.println("#iter\tError\tCacheError\tVotedError\tVotedCacheError\tGlobalError\tDelayError\tDelayCacheError\talpha");
    }
    
    double label;
    SparseVector instance;
    double evalSize = evaluation.size();
    for (int i = 0; i < evalSize; i++) {
      instance = evaluation.getInstance(i);
      label = evaluation.getLabel(i);
      votedPrediction = 0.0;
      votedCachePrediction = 0.0;
      
      for (int modelId = 0; modelId < numOfLearners; modelId++) {
        // compute local prediction, error
        prediction = classifiers[modelId].predict(instance);
        if (prediction != label){
          error ++;
        }
        votedPrediction += prediction;
        
        // compute cache prediction, error
        cachePrediction = cacheClassifiers[modelId].predict(instance);
        if (cachePrediction != label){
          cahceError ++;
        }
        votedCachePrediction += cachePrediction;
        
        // compute prediction of local cached models
        delayPrediction = prediction;
        for (int m = 0; m < localModelCache[modelId].size(); m++){
          delayPrediction += localModelCache[modelId].get(m).predict(instance);
        }
        delayPrediction = Math.round(delayPrediction / numOfLearners);
        if (delayPrediction != label){
          delayError ++;
        }
        
        // compute prediction of cahced cache models
        delayCahcePrediction = cachePrediction;
        for (int m = 0; m < cacheModelCache[modelId].size(); m++){
          delayCahcePrediction += cacheModelCache[modelId].get(m).predict(instance);
        }
        delayCahcePrediction = Math.round(delayCahcePrediction / numOfLearners);
        if (delayCahcePrediction != label){
          delayCacheError ++;
        }
      }
      
      // compute voted error
      votedPrediction = Math.round(votedPrediction / numOfLearners);
      if (votedPrediction != label){
        votedError ++;
      }
      
      //compute voted cache error
      votedCachePrediction = Math.round(votedCachePrediction / numOfLearners);
      if (votedCachePrediction != label){
        votedCacheError ++;
      }
      
      // compute global prediction
      globalPrediction = globalClassifier.predict(instance);
      if (globalPrediction != label){
        globalError ++;
      }
    }
    
    error /= evalSize * numOfLearners;
    cahceError /= evalSize * numOfLearners;
    votedError /= evalSize;
    votedCacheError /= evalSize;
    globalError /= evalSize;
    delayError /= evalSize * numOfLearners;
    delayCacheError /= evalSize * numOfLearners;
    System.out.println(iteration + "\t" + error + "\t" + cahceError + "\t" + votedError + "\t" + votedCacheError + "\t" + globalError + "\t" + delayError + "\t" + delayCacheError + "\t" + alpha);
    
  }
  
  private boolean isChangeLabels = false;
  public void run() throws Exception{
    double alpha = 0.0;
    for (int i = 1; i <= numOfEvals*numOfLearners; i++) {
      // checks the time for changing labels and handles the assynchronity of drift lengths
      double relativeTime = i % (long) driftLength;
      if (isSudden) {
        if (!isChangeLabels && relativeTime > (long)driftLength1){
          // first part of drift
          isChangeLabels = true;
          alpha = 1.0;
          changeLabels(alpha);
        }
        if (isChangeLabels && relativeTime <= (long)driftLength1){
          // second part of drift
          isChangeLabels = false;
          alpha = 0.0;
          changeLabels(alpha);
        }
      } else {
        if (relativeTime > (long)driftLength1) {
          alpha = 1.0 - ((relativeTime - driftLength1) / (driftLength - driftLength1));
          changeLabels(alpha);
        } else {
          alpha = relativeTime / driftLength1;
          changeLabels(alpha);
        }
      }
      
      //double evalsPerTick = (double)numOfEvals / (double)CommonState.getEndTime();
      double evalsPerTick = 1.0/numOfLearners;
      
      double prevEvalTime = evalsPerTick * (i - 1);
      double actualEvalTime = evalsPerTick * i;
      boolean isEval = Math.floor(actualEvalTime) - Math.floor(prevEvalTime) > 0.0;
      
      double prevSampleTime = evalsPerTick * samplesPerEval * (i - 1);
      double actualSampleTime = evalsPerTick * samplesPerEval * i;
      //boolean isIncomingSample = Math.floor(actualSampleTime) - Math.floor(prevSampleTime) > 0.0;
      //double numOfIncomingSamples = Math.floor(actualSampleTime) - Math.floor(prevSampleTime);
      double amountOfIncomingSamples = actualSampleTime - prevSampleTime;
      
      if (isEval){
        // evaluates the models on nodes
        train();
        evaluate(i/numOfLearners, alpha);
        Arrays.fill(isClear, true);
        //clear();
      }
      //if (isIncomingSample){
        // change samples on nodes
        //for (int s = 0; s < numOfIncomingSamples; s++) {
          changeInstances(amountOfIncomingSamples);
          //System.out.println("NEW INSATNCE");
        //}
      //}
    }
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 9){
      System.err.println("NumOfEvals DriftsPerEval SamplesPerEval AsyncRate Dimension NumOfInstances IsSudden IsNoise NumOfLearners");
      return;
    }
    int numOfEvals = Integer.parseInt(args[0]);
    double driftsPerEval = Double.parseDouble(args[1]);
    double samplesPerEval = Double.parseDouble(args[2]);
    double asyncRate = Double.parseDouble(args[3]);
    
    int dimension = Integer.parseInt(args[4]);
    int numOfInstances = Integer.parseInt(args[5]);
    boolean isSudden = Boolean.parseBoolean(args[6]);
    boolean isNoise = Boolean.parseBoolean(args[7]);
    
    int numOfLearners = Integer.parseInt(args[8]);
    
    BaseLine bl = new BaseLine(numOfEvals, driftsPerEval, samplesPerEval, asyncRate, dimension, numOfInstances, isSudden, isNoise, numOfLearners);
    bl.run();
  }
}
