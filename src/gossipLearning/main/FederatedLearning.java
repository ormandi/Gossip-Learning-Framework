package gossipLearning.main;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.main.fedAVG.ModelEvaluatorTask;
import gossipLearning.main.fedAVG.ModelSendTask;
import gossipLearning.main.fedAVG.ModelUpdateTask;
import gossipLearning.main.fedAVG.TaskRunner;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;
import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;
import peersim.transport.ChurnProvider;

public class FederatedLearning {
  public static void main(String args[]) throws Exception {
    long time = System.currentTimeMillis();
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(args));
    System.err.println("Loading parameters from " + configName);
    int numThreads = Configuration.getInt("THREADS", 1);
    System.err.println("Simulation runs on " + numThreads + " thread(s).");
    
    int numIters = Configuration.getInt("ITER");
    System.err.println("\tNumber of iterations: " + numIters);
    long seed = Configuration.getLong("SEED", Utils.getSeed());
    System.err.println("\tRandom seed: " + seed);
    CommonState.r.setSeed(seed);
    int iterOffset = Configuration.getInt("ITEROFFSET", 0);
    System.err.println("\tIteration offset: " + iterOffset);
    
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    System.err.println("\ttraining file: " + tFile);
    File eFile = new File(Configuration.getString("evaluationFile"));
    System.err.println("\tevaluation file: " + eFile);
    int normalization = Configuration.getInt("NORMALIZATION", 0);
    System.err.println("\tNormalization method: " + LocalRun.NORMALIZATION.values()[normalization]);
    
    // number of clients
    int K = Configuration.getInt("CLIENTS");
    System.err.println("\tNumber of clients: " + K);
    // proportion of clients
    double C = Configuration.getDouble("FRACTION");
    System.err.println("\tFraction of clients: " + C);
    // number of local training passes
    int E = Configuration.getInt("EPOCH");
    System.err.println("\tNumber of epoch: " + E);
    // local minibatch size
    int B = Configuration.getInt("BATCH");
    System.err.println("\tLocal batch size: " + B);
    // number of classes per clients
    int cLabels = Configuration.getInt("C", 0);
    System.err.println("\tNumber of classes per clients: " + cLabels);
    // evaluate global model only
    boolean globalEval = Configuration.getInt("GLOBALEVAL", 1) != 0;
    System.err.println("\tEvaluate global model only: " + globalEval);
    // send slim model for clients
    boolean downSlim = Configuration.getInt("DOWNSLIM", 0) != 0;
    System.err.println("\tSend down slim model: " + downSlim);
    
    // init churn
    String churnClass = Configuration.getString("churn", null);
    System.err.println("\tChurn provider class: " + churnClass);
    long delay = Configuration.getInt("DELAY", 1);
    System.err.println("\tround-trip time: " + delay);
    double times = Configuration.getDouble("TIMES", 1.0);
    System.err.println("\ttraining set multiplier: " + times);
    
    // TODO: fraction permutation + churn provider
    
    ChurnProvider[] churnProvider = new ChurnProvider[K];
    ChurnProvider cp = churnClass == null ? null : (ChurnProvider)Class.forName(churnClass).getConstructor(String.class).newInstance("churn");
    long[] sessionEnd = new long[K];
    boolean[] isOnline = new boolean[K];
    for (int i = 0; i < K; i++) {
      sessionEnd[i] = 0;
      isOnline[i] = true;
      churnProvider[i] = cp == null ? null : cp.clone();
    }
    
    // init models
    String include = Configuration.getString("include", null);
    String[] includes = include == null ? null : include.split("\\s");
    String[] modelNames = Configuration.getNames("learner");
    if (includes != null) {
      modelNames = new String[includes.length];
      for (int i = 0; i < includes.length; i++) {
        modelNames[i] = "learner." + includes[i];
      }
    }
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision", 4);
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    System.err.println("\tsize: " + reader.getTrainingSet().size() + ", " + reader.getEvalSet().size() + " x " + reader.getTrainingSet().getNumberOfFeatures());
    
    // normalize database
    if (normalization == 2) {
      System.err.println("Standardizing data set.");
      reader.standardize();
    } else if (normalization == 1) {
      System.err.println("Normalizing data set.");
      reader.normalize();
    }
    
    // create models
    LearningModel[] globalModels = new LearningModel[modelNames.length];
    LearningModel[] avgModels = new LearningModel[modelNames.length];
    LearningModel[][] localModels = new LearningModel[modelNames.length][K];
    for (int i = 0; i < modelNames.length; i++) {
      globalModels[i] = (LearningModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      globalModels[i].setParameters(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
      avgModels[i] = globalModels[i].clone();
      for (int j = 0; j < K; j++) {
        localModels[i][j] = globalModels[i].clone();
      }
    }
    
    // initialize evaluator
    ResultAggregator resultAggregator = new ResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    ResultAggregator[] aggrs = new ResultAggregator[K];
    for (int i = 0; i < K; i++) {
      aggrs[i] = resultAggregator.clone();
    }
    
    // shuffle instances
    InstanceHolder instances = reader.getTrainingSet();
    int[] instanceIndices = new int[instances.size()];
    for (int i = 0; i < instances.size(); i++) {
      instanceIndices[i] = i;
    }
    Utils.arrayShuffle(CommonState.r, instanceIndices);
    
    // set local instances for clients
    InstanceHolder[] localInstances = new InstanceHolder[K];
    for (int i = 0; i < K; i++) {
      localInstances[i] = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
    }
    LinkedList<Integer>[] map = Utils.mapLabelsToNodes(instances.getNumberOfClasses(), K, cLabels);
    for (int i = 0; i < instances.size() * times; i++) {
      double label = instances.getLabel(instanceIndices[i % instances.size()]);
      SparseVector instance = instances.getInstance(instanceIndices[i % instances.size()]);
      int clientIdx = i % K;
      if (map != null) {
        clientIdx = map[(int)label].poll();
        map[(int)label].add(clientIdx);
      }
      localInstances[clientIdx].add(instance, label);
      if (i % instances.size() == instances.size() - 1) {
        Utils.arrayShuffle(CommonState.r, instanceIndices);
      }
    }
    
    TaskRunner sendTasks = new TaskRunner(numThreads);
    TaskRunner evalTasks = new TaskRunner(numThreads);
    TaskRunner updateTasks = new TaskRunner(numThreads);
    Random[] r = new Random[K];
    long[] seeds = new long[K];
    
    // client order
    int[] perm = new int[K];
    for (int i = 0; i < K; i++) {
      perm[i] = i;
      r[i] = new Random();
    }
    //Utils.arrayShuffle(CommonState.r, perm);
    for (int t = iterOffset; t <= numIters; t++) {
      updateState(t, delay, sessionEnd, isOnline, churnProvider, C, perm);
      
      for (int m = 0; m < globalModels.length; m++) {
        // evaluate global model
        if (globalEval) {
          resultAggregator.push(-1, m, globalModels[m]);
        }
        
        // send global model to clients
        for (int i = 0; i < K; i++) {
          //System.out.println(i + "\t" + isOnline(i, t, delay, sessionEnd, isOnline, C, true, perm));
          
          seeds[i] = CommonState.r.nextLong();
          if (!isOnline(i, t, delay, sessionEnd, isOnline, C, true, perm)) {
            continue;
          }
          //localModels[m][i] = globalModels[m].clone();
          //localModels[m][i].set(globalModels[m]);
          r[i].setSeed(seeds[i]);
          sendTasks.add(new ModelSendTask(localModels[m][i], globalModels[m], r[i], downSlim));
          
          // evaluate local models (multi thread)
          if (!globalEval) {
            evalTasks.add(new ModelEvaluatorTask(aggrs[i], localModels[m][i], -1, m));
          }
        }
        sendTasks.run();
        evalTasks.run();
      }
      
      // print result of evaluation
      for (AggregationResult result : resultAggregator) {
        if (t == iterOffset) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println(t + "\t" + result);
      }
      
      for (int m = 0; m < globalModels.length; m++) {  
        // update local models (multi thread)
        for (int i = 0; i < K; i++) {
          if (!isOnline(i, t, delay, sessionEnd, isOnline, C, true, perm)) {
            continue;
          }
          //localModels[m][i].update(localInstances[i], E, B);
          updateTasks.add(new ModelUpdateTask(localModels[m][i], localInstances[i], E, B));
        }
        updateTasks.run();
        
        // count recv models check online sessions
        double recvModels = 0.0;
        for (int i = 0; i < K; i++) {
          if (!isOnline(i, t, delay, sessionEnd, isOnline, C, false, perm)) {
            continue;
          }
          recvModels ++;
        }
        // continue if no online client
        if (recvModels == 0.0) {
          continue;
        }
        
        // reset model collector
        avgModels[m].clear();
        
        // push updated model
        for (int i = 0; i < K; i++) {
          if (!isOnline(i, t, delay, sessionEnd, isOnline, C, false, perm)) {
            continue;
          }
          r[i].setSeed(seeds[i]);
          double coef = 1.0 / recvModels;
          // keep gradients only
          r[i].setSeed(seeds[i]);
          //Model model = ((Partializable)((Addable)localModels[m][i].clone()).add(globalModels[m], -1)).getModelPart(r);
          Model model = ((Addable)localModels[m][i].clone()).add(globalModels[m], -1);
          if (model instanceof Partializable) {
            model = ((Partializable)model).getModelPart(r[i]);
          }
          // averaging updated models
          if (avgModels[m] instanceof SlimModel) {
            // scale parameters only (no bias, no age)
            double size = ((SlimModel)model).getSize();
            ((SlimModel)model).scale(1.0 / (1.0 - Math.pow(1.0 - size, recvModels)));
            ((SlimModel)avgModels[m]).weightedAdd(model, coef);
          } else {
            ((Addable)avgModels[m]).add(model, coef);
          }
        }
        
        // update global model
        ((Addable)globalModels[m]).add(avgModels[m]);
      }
    }
    System.err.println("Final result:");
    for (int i = 0; i < globalModels.length; i++) {
      resultAggregator.push(-1, i, globalModels[i]);
    }
    System.err.println(resultAggregator);
    System.err.println("ELAPSED TIME: " + (System.currentTimeMillis() - time));
  }
  
  public static void updateState(long t, long delay, long[] sessionEnd, boolean[] isOnline, ChurnProvider[] provider, double fraction, int[] perm) {
    int from = (int)Math.round(t * fraction * isOnline.length);
    int to = (int)Math.round((t + 1) * fraction  * isOnline.length);
    if (fraction == ((t + 1) % isOnline.length) || to % isOnline.length < from % isOnline.length) {
      Utils.arrayShuffle(CommonState.r, perm);
    }
    
    for (int i = 0; i < isOnline.length; i++) {
      if (provider[i] == null) {
        sessionEnd[i] = Long.MAX_VALUE;
      } else {
        while(sessionEnd[i] <= t * delay) {
          sessionEnd[i] += provider[i].nextSession();
          isOnline[i] = !isOnline[i];
        }
      }
    }
  }
  public static boolean isOnline(int idx, long t, long delay, long[] sessionEnd, boolean[] isOnline, double fraction, boolean down, int[] perm) {
    int from = (int)Math.round(t * fraction * isOnline.length);
    int to = (int)Math.round((t + 1) * fraction  * isOnline.length);
    boolean result = false;
    if (fraction == 0.0) {
      if (perm[idx] == (int)(t % isOnline.length)) {
        result = isOnline[idx];
      }
    } else {
      from %= isOnline.length;
      to %= isOnline.length;
      if ((from < to && from <= perm[idx] && perm[idx] < to) ||
          (to < from && (from <= perm[idx] || perm[idx] < to)) ||
          fraction == 1.0) {
        result = isOnline[idx];
      }
    }
    result = result && (t + 0.5 + (down ? 0.0 : 0.5)) * delay < sessionEnd[idx];
    return result;
  }
}
