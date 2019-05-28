package gossipLearning.main;

import gossipLearning.evaluators.RecSysResultAggregator;
import gossipLearning.interfaces.models.Addable;
import gossipLearning.interfaces.models.FeatureExtractor;
import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.main.fedAVG.ModelSumTask;
import gossipLearning.main.fedAVG.RecSysModelUpdateTask;
import gossipLearning.main.fedAVG.TaskRunner;
import gossipLearning.models.extraction.DummyExtractor;
import gossipLearning.models.factorization.MergeableRecSys;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.Utils;

import java.io.File;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;
import peersim.transport.ChurnProvider;

public class FederatedRecSys {

  public static void main(String[] args) throws Exception {
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
    
    // number of clients
    int K = Configuration.getInt("CLIENTS");
    System.err.println("\tNumber of clients: " + K);
    // proportion of clients
    double C = Configuration.getDouble("FRACTION");
    System.err.println("\tFraction of clients: " + C);
    
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
    
    // create models
    MatrixBasedModel[] globalModels = new MatrixBasedModel[modelNames.length];
    MatrixBasedModel[] avgModels = new MatrixBasedModel[modelNames.length];
    double[][][] userModels = new double[modelNames.length][reader.getTrainingSet().size()][];
    for (int i = 0; i < modelNames.length; i++) {
      globalModels[i] = (MatrixBasedModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
      avgModels[i] = (MatrixBasedModel)Class.forName(Configuration.getString(modelNames[i])).getConstructor(String.class).newInstance(modelNames[i]);
    }
    
    if (K != reader.getTrainingSet().size()) {
      throw new NumberFormatException("Client size has to be equal to data size: " + K + " != " + reader.getTrainingSet().size());
    }
    
    // initialize evaluator
    RecSysResultAggregator resultAggregator = new RecSysResultAggregator(modelNames, evalNames);
    resultAggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    // set local instances for clients
    MatrixBasedModel[] localModels = new MatrixBasedModel[K];
    
    
    // learning
    System.err.println("Start learning.");
    FeatureExtractor extractor = new DummyExtractor("");
    TaskRunner taskRunner = new TaskRunner(numThreads);
    RecSysModelUpdateTask[] updateTasks = new RecSysModelUpdateTask[K];
    ModelSumTask[] modelSumTask = new ModelSumTask[numThreads];
    MatrixBasedModel[] tmpAvgModels = new MatrixBasedModel[numThreads];
    int evalTime = 1;
    
    for (int t = 0; t <= numIters; t++) {
      FederatedLearning.updateState(t * delay, sessionEnd, isOnline, churnProvider, C);
      
      if (t % evalTime == 0) {
        // evaluate
        for (int i = 0; i < globalModels.length; i++) {
          for (int j = 0; j < K; j++) {
            resultAggregator.push(-1, i, j, userModels[i][j], globalModels[i], extractor);
          }
        }
        
        // print results
        for (AggregationResult result : resultAggregator) {
          if (t == 0) {
            System.out.println("#iter\t" + result.getNames());
          }
          System.out.println(t + "\t" + result);
        }
      }
      if (t == evalTime * 10) {
        evalTime *= 10;
      }
      
      for (int m = 0; m < globalModels.length; m++) {
        // send global model to clients
        for (int i = 0; i < K; i++) {
          if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
            continue;
          }
          localModels[i] = (MatrixBasedModel)globalModels[m].clone();
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
          updateTasks[i] = new RecSysModelUpdateTask(localModels[i], userModels[m][i], reader.getTrainingSet().getInstance(i));
          taskRunner.add(updateTasks[i]);
        }
        taskRunner.run();
        for (int i = 0; i < K; i++) {
          userModels[m][i] = updateTasks[i].rowModel;
        }
        
        // sum up models in parallel
        // push updated model
        double part = K / (double)numThreads;
        for (int core = 0; core < numThreads; core++) {
          int from = (int)Math.round(core * part);
          int to = (int)Math.round((core + 1) * part);
          //System.out.println(core + "\t" + from + "\t" + to);
          tmpAvgModels[core] = (MatrixBasedModel)avgModels[m].clone();
          modelSumTask[core] = new ModelSumTask(tmpAvgModels[core], globalModels[m], localModels, from, to, 1.0, isOnline, sessionEnd, t, delay);
          taskRunner.add(modelSumTask[core]);
        }
        taskRunner.run();
        for (int core = 0; core < numThreads; core++) {
          ((Addable)avgModels[m]).add(tmpAvgModels[core]);
        }
        
        //System.exit(0);
        /*for (int i = 0; i < K; i++) {
          if (!isOnline[i] || sessionEnd[i] <= (t + 1) * delay) {
            continue;
          }
          // averaging updated models
          ((Addable)avgModels[m]).add(((Partializable)((Addable)localModels[i]).add(globalModels[m], -1.0)).getModelPart(), 1.0);
        }*/
        
        // normalize by weights
        ((MergeableRecSys)avgModels[m]).normalize();
        // update global model
        ((Addable)globalModels[m]).add(avgModels[m]);
      }
    }
    
    // evaluate on the end of the learning again
    System.err.println("Final result:");
    for (int i = 0; i < globalModels.length; i++) {
      for (int j = 0; j < reader.getTrainingSet().size(); j++) {
        resultAggregator.push(-1, i, j, userModels[i][j], globalModels[i], extractor);
      }
    }
    System.err.println(resultAggregator);
    System.err.println("ELAPSED TIME: " + (System.currentTimeMillis() - time));
  }

}
