package gossipLearning.models.adaptive;

import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class models the problem of drifting concepts using the moving hyperplane 
 * approach and measures the performance of some baseline algorithms. 
 * Defines samples in the d dimensional unit hypercube and two hypesrplanes 
 * , go through the origin, that are orthogonal to each other. The concept moves 
 * between these hyperplanes periodically and specifies the labelling of the samples. 
 * The move of the hyperplane can be sudden of incremental.
 * @author István Hegedűs
 *
 */
public class BaseLine {
  protected static final long seed = 1234567890;
  protected static final String modelName = "gossipLearning.models.multiClassLearners.MultiLogReg";
  
  protected final long numOfEvals;
  protected final double samplesPerEval;
  
  protected final double driftLength;
  protected final double driftLength1;
  
  protected final int dimension;
  protected final int numOfInstances;
  protected final boolean isSudden;
  protected final boolean isNoise;
  
  protected final int numOfLearners;
  
  protected final double[] from;
  protected final double[] to;
  protected SparseVector w;
  protected InstanceHolder training;
  protected InstanceHolder evaluation;
  protected final Random r;
  
  protected boolean[] isClear;
  protected Model[] classifiers;
  protected Model globalClassifier;
  protected Model[] cacheClassifiers;
  protected final int cacheLearnerCacheSize = 100;
  protected InstanceHolder[] localTrainSets;
  protected InstanceHolder globalTrainSet;
  protected InstanceHolder[] cacheTrainSets;
  protected boolean isNewCacheSample = true;
  
  protected List<Model>[] localModelCache;
  protected List<Model>[] cacheModelCache;
  
  protected int numOfClasses = 2;
  
  @SuppressWarnings("unchecked")
  public BaseLine(long numOfEvals, double driftsPerEval, double samplesPerEval, double asyncRate, int dimension, int numOfInstances, boolean isSudden, boolean isNoise, int numOfLearners) throws Exception {
    r = new Random(seed);
    this.numOfEvals = numOfEvals;
    this.samplesPerEval = samplesPerEval;
    
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
    training = new InstanceHolder(numOfClasses);
    evaluation = new InstanceHolder(numOfClasses);
    
    isClear = new boolean[numOfLearners];
    Arrays.fill(isClear, false);
    classifiers = new Model[numOfLearners];
    globalClassifier = (Model)Class.forName(modelName).newInstance();
    globalClassifier.setNumberOfClasses(numOfClasses);
    cacheClassifiers = new Model[numOfLearners];
    localTrainSets = new InstanceHolder[numOfLearners];
    globalTrainSet = new InstanceHolder(numOfClasses);
    cacheTrainSets = new InstanceHolder[numOfLearners];
    
    localModelCache = new List[numOfLearners];
    cacheModelCache = new List[numOfLearners];
    for (int i = 0; i < numOfLearners; i++){
      classifiers[i] = (Model)Class.forName(modelName).newInstance();
      classifiers[i].setNumberOfClasses(numOfClasses);
      cacheClassifiers[i] = (Model)Class.forName(modelName).newInstance();
      cacheClassifiers[i].setNumberOfClasses(numOfClasses);
      localTrainSets[i] = new InstanceHolder(numOfClasses);
      cacheTrainSets[i] = new InstanceHolder(numOfClasses);
      
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
  
  protected double prevAlpha = 0.0;
  protected void changeLabels(double alpha) {
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
      }
    }
  }
  
  private void trainModel(Model model, InstanceHolder samples) {
    // selects a random training sample 10 * sampesize times
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
        classifiers[i].setNumberOfClasses(numOfClasses);
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
        cacheClassifiers[i].setNumberOfClasses(numOfClasses);
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
      globalClassifier.setNumberOfClasses(numOfClasses);
      // train global model
      trainModel(globalClassifier, globalTrainSet);
      globalTrainSet.clear();
    }
    isNewCacheSample = false;
    
  }
  
  private void evaluate(int iteration, double alpha) {
    double prediction;
    double cachePrediction;
    double[] votedPrediction = new double[numOfClasses];
    double[] votedCachePrediction = new double[numOfClasses];
    double globalPrediction;
    double[] delayPrediction = new double[numOfClasses];
    double[] delayCahcePrediction = new double[numOfClasses];
    
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
      Arrays.fill(votedPrediction,0.0);
      Arrays.fill(votedCachePrediction,0.0);
      
      for (int modelId = 0; modelId < numOfLearners; modelId++) {
        // compute local prediction, error
        prediction = classifiers[modelId].predict(instance);
        if (prediction != label){
          error ++;
        }
        
        votedPrediction[(int)prediction] ++;
        // compute cache prediction, error
        cachePrediction = cacheClassifiers[modelId].predict(instance);
        if (cachePrediction != label){
          cahceError ++;
        }
        votedCachePrediction[(int)cachePrediction] ++;
        
        // compute prediction of local cached models
        Arrays.fill(delayPrediction, 0.0);
        delayPrediction[(int)prediction] ++;
        for (int m = 0; m < localModelCache[modelId].size(); m++){
          delayPrediction[(int)localModelCache[modelId].get(m).predict(instance)] ++;
        }
        double maxindex = 0;
        double maxval = 0.0;
        for (int mi = 0; mi < numOfClasses; mi++) {
          if (delayPrediction[mi] > maxval) {
            maxval = delayPrediction[mi];
            maxindex = mi;
          }
        }
        if (maxindex != label){
          delayError ++;
        }
        
        // compute prediction of cahced cache models
        Arrays.fill(delayCahcePrediction, 0.0);
        delayCahcePrediction[(int) cachePrediction] ++;
        for (int m = 0; m < cacheModelCache[modelId].size(); m++){
          delayCahcePrediction[(int)cacheModelCache[modelId].get(m).predict(instance)] ++;
        }
        maxindex = 0;
        maxval = 0.0;
        for (int mi = 0; mi < numOfClasses; mi++) {
          if (delayCahcePrediction[mi] > maxval) {
            maxval = delayCahcePrediction[mi];
            maxindex = mi;
          }
        }
        if (maxindex != label){
          delayCacheError ++;
        }
      }
      
      // compute voted error
      double maxindex = 0;
      double maxval = 0.0;
      for (int mi = 0; mi < numOfClasses; mi++) {
        if (votedPrediction[mi] > maxval) {
          maxval = votedPrediction[mi];
          maxindex = mi;
        }
      }
      if (maxindex != label){
        votedError ++;
      }
      
      //compute voted cache error
      maxindex = 0;
      maxval = 0.0;
      for (int mi = 0; mi < numOfClasses; mi++) {
        if (votedCachePrediction[mi] > maxval) {
          maxval = votedCachePrediction[mi];
          maxindex = mi;
        }
      }
      
      if (maxindex != label){
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
      
      double evalsPerTick = 1.0/numOfLearners;
      
      double prevEvalTime = evalsPerTick * (i - 1);
      double actualEvalTime = evalsPerTick * i;
      boolean isEval = Math.floor(actualEvalTime) - Math.floor(prevEvalTime) > 0.0;
      
      double prevSampleTime = evalsPerTick * samplesPerEval * (i - 1);
      double actualSampleTime = evalsPerTick * samplesPerEval * i;
      double amountOfIncomingSamples = actualSampleTime - prevSampleTime;
      
      if (isEval){
        // evaluates the models on nodes
        train();
        evaluate(i/numOfLearners, alpha);
        Arrays.fill(isClear, true);
      }
      
      changeInstances(amountOfIncomingSamples);
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
