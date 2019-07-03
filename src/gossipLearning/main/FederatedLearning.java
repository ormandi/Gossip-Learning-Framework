package gossipLearning.main;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.interfaces.models.Model;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.interfaces.models.SlimModel;
import gossipLearning.main.fedAVG.ModelUpdateTask;
import gossipLearning.main.fedAVG.TaskRunner;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import gossipLearning.utils.Utils;

import java.io.File;
import java.util.LinkedList;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;
import peersim.transport.ChurnProvider;

public class FederatedLearning {
  public static void main(String args[]) throws Exception {
    long time = System.currentTimeMillis();
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    int numThreads = Configuration.getInt("THREADS", 1);
    System.err.println("Simulation runs on " + numThreads + " thread(s).");
    
    int numIters = Configuration.getInt("ITER");
    System.err.println("\tNumber of iterations: " + numIters);
    long seed = Configuration.getLong("SEED", Utils.getSeed());
    System.err.println("\tRandom seed: " + seed);
    CommonState.r.setSeed(seed);
    
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
    int E = Configuration.getInt("EPOCHS");
    System.err.println("\tNumber of epoch: " + E);
    // local minibatch size
    int B = Configuration.getInt("BATCH");
    System.err.println("\tLocal batch size: " + B);
    // number of classes per clients
    int cLabels = Configuration.getInt("C");
    System.err.println("\tNumber of classes per clients: " + cLabels);
    
    // init churn
    String churnClass = Configuration.getString("churn", null);
    System.err.println("\tChurn provider class: " + churnClass);
    long delay = Configuration.getInt("DELAY");
    System.err.println("\tround-trip time: " + delay);
    
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
    for (int i = 0; i < modelNames.length; i++) {
      globalModels[i] = (LearningModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      globalModels[i].setParameters(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
      avgModels[i] = (LearningModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      avgModels[i].setParameters(reader.getTrainingSet().getNumberOfClasses(), reader.getTrainingSet().getNumberOfFeatures());
    }
    
    // initialize evaluator
    ResultAggregator resultAggregator = new ResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // shuffle instances
    InstanceHolder instances = reader.getTrainingSet();
    int[] instanceIndices = new int[instances.size()];
    for (int i = 0; i < instances.size(); i++) {
      instanceIndices[i] = i;
    }
    Utils.arrayShuffle(CommonState.r, instanceIndices);
    
    // set local instances for clients
    LearningModel[] localModels = new LearningModel[K];
    InstanceHolder[] localInstances = new InstanceHolder[K];
    for (int i = 0; i < K; i++) {
      localInstances[i] = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
    }
    LinkedList<Integer>[] map = Utils.mapLabelsToNodes(instances.getNumberOfClasses(), K, cLabels);
    for (int i = 0; i < instances.size(); i++) {
      double label = instances.getLabel(instanceIndices[i]);
      SparseVector instance = instances.getInstance(instanceIndices[i]);
      int clientIdx = i % K;
      clientIdx = map[(int)label].poll();
      map[(int)label].add(clientIdx);
      localInstances[clientIdx].add(instance, label);
    }
    
    FeatureExtractor extractor = new DummyExtractor("");
    TaskRunner taskRunner = new TaskRunner(numThreads);
    for (int t = 0; t <= numIters; t++) {
      updateState(t * delay, sessionEnd, isOnline, churnProvider, C);
      
      // evaluate global model
      for (int m = 0; m < globalModels.length; m++) {
        resultAggregator.push(-1, m, globalModels[m], extractor);
      }
      for (AggregationResult result : resultAggregator) {
        if (t == 0) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println(t + "\t" + result);
      }
      
      for (int m = 0; m < globalModels.length; m++) {
        // send global model to clients
        for (int i = 0; i < K; i++) {
          if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
            continue;
          }
          localModels[i] = globalModels[m].clone();
        }
        
        // check online sessions
        double recvModels = 0.0;
        for (int i = 0; i < K; i++) {
          if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
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
        
        // update local models (multi thread)
        for (int i = 0; i < K; i++) {
          if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
            continue;
          }
          taskRunner.add(new ModelUpdateTask(localModels[i], localInstances[i], E, B));
        }
        taskRunner.run();
        
        // push updated model
        for (int i = 0; i < K; i++) {
          if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
            continue;
          }
          double coef = 1.0 / recvModels;
          // keep gradients only
          Model model = ((Partializable)((Addable)localModels[i]).add(globalModels[m], -1)).getModelPart();
          // averaging updated models
          if (avgModels[m] instanceof SlimModel) {
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
      resultAggregator.push(-1, i, globalModels[i], extractor);
    }
    System.err.println(resultAggregator);
    System.err.println("ELAPSED TIME: " + (System.currentTimeMillis() - time));
  }
  
  public static void updateState(long t, long[] sessionEnd, boolean[] isOnline, ChurnProvider[] provider, double fraction) {
    for (int i = 0; i < sessionEnd.length; i++) {
      if (provider[i] == null) {
        // uniform selection
        sessionEnd[i] = Long.MAX_VALUE;
        isOnline[i] = CommonState.r.nextDouble() < fraction;
      } else {
        while(sessionEnd[i] <= t) {
          sessionEnd[i] += provider[i].nextSession();
          isOnline[i] = !isOnline[i];
        }
      }
    }
  }
}
