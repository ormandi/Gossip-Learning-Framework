package gossipLearning.main;

import gossipLearning.evaluators.ResultAggregator;
import gossipLearning.models.clustering.KMeans;
import gossipLearning.utils.AggregationResult;
import gossipLearning.utils.DataBaseReader;
import gossipLearning.utils.SparseVector;

import java.io.File;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;

public class KMeansBase {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Using: KMeansBase LocalConfig");
      System.exit(0);
    }
    
    // set up configuration parser
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    System.err.println("Loading parameters from " + configName);
    
    // parse general parameters
    int numIters = Configuration.getInt("ITER");
    //long seed = Configuration.getLong("SEED");
    long seed = System.currentTimeMillis();
    int K = Configuration.getInt("K");
    CommonState.r.setSeed(seed);
    
    // parse learning related parameters
    String dbReaderName = Configuration.getString("dbReader");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    
    String[] evalNames = Configuration.getString("evaluators").split(",");
    int printPrecision = Configuration.getInt("printPrecision");
    
    // read database
    System.err.println("Reading data set.");
    DataBaseReader reader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    reader.standardize();
    
    // initialize evaluator
    ResultAggregator aggregator = new ResultAggregator(new String[]{"KMeans"}, evalNames);
    aggregator.setEvalSet(reader.getEvalSet());
    AggregationResult.printPrecision = printPrecision;
    
    SparseVector[] centroids = new SparseVector[K];
    double[] counters = new double[K];
    KMeans model = new KMeans(K, 100);
    
    System.err.println("Initializing centroids.");
    // initialize initial centroids
    for (int i = 0; i < K; i++) {
      centroids[i] = new SparseVector(reader.getTrainingSet().getNumberOfFeatures());
      for (int j = 0; j < reader.getTrainingSet().getNumberOfFeatures(); j++) {
        centroids[i].put(j, CommonState.r.nextDouble() - 0.5);
      }
    }
    
    System.err.println("Running...");
    for (int i = 0; i < numIters; i++) {
      // set centroids for the model
      model.setCentroids(centroids);
      
      // evaluate model
      aggregator.push(-1, 0, model);
      // print results
      for (AggregationResult result : aggregator) {
        if (i == 0) {
          System.out.println("#iter\t" + result.getNames());
        }
        System.out.println(i + "\t" + result);
      }
      
      // clean centroids
      for (int j = 0; j < K; j++) {
        centroids[j].clear();
        counters[j] = 0.0;
      }
      
      // cluster assign and summing instances
      for (int j = 0; j < reader.getTrainingSet().size(); j++) {
        SparseVector instance = reader.getTrainingSet().getInstance(j);
        int index = (int)model.predict(instance);
        centroids[index].add(instance);
        counters[index] ++;
      }
      
      // recompute centroids
      for (int j = 0; j < K; j++) {
        centroids[j].mul(counters[j] == 0.0 ? 0.0 : 1.0 / counters[j]);
      }
    }
    System.err.println("Final result:");
    aggregator.push(-1, 0, model);
    System.err.println(aggregator);
  }
}
