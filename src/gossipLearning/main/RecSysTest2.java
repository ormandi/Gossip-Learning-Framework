package gossipLearning.main;

import gossipLearning.DataBaseReader;
import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.models.clusterer.KMeans;
import gossipLearning.models.recSys.ItemFrequencies;
import gossipLearning.utils.SparseVector;

import java.io.File;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;

public class RecSysTest2 {

  protected static double numberOfRatings;
  protected static int numOfClusters;
  protected static int seed;
  protected static double divErr;
  
  protected static String modelName;
  protected static String prefix;
  protected static int iter;
  protected static int m;
  protected static int k;
  protected static int clusteringFreq;
  protected static int printFreq;
  
  public static void main(String[] args) throws Exception {
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    String dbReaderName = "gossipLearning.RecSysDataBaseReader";
    
    seed = Configuration.getInt("SEED");
    numOfClusters = Configuration.getInt("numOfClusters");
    divErr = Configuration.getDouble("divErr");
    numberOfRatings = Configuration.getDouble("numOfRatings");
    
    prefix = "model";
    modelName = Configuration.getString(prefix);
    iter = Configuration.getInt("iter");
    m = Configuration.getInt("m");
    k = Configuration.getInt("k");
    clusteringFreq = Configuration.getInt("clusteringFreq");
    printFreq = Configuration.getInt("printFreq");
    
    Random r = new Random(seed);
    DataBaseReader dbReader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    ItemFrequencies freqs = new ItemFrequencies((int)numberOfRatings, m, k, Integer.MAX_VALUE);
    
    KMeans kMeans = new KMeans(numOfClusters);
    Model[] models = new Model[numOfClusters];
    for (int i = 0; i < numOfClusters; i++) {
      models[i] = (Model)Class.forName(modelName).newInstance();
      models[i].init(prefix);
      models[i].setNumberOfClasses((int)numberOfRatings);
    }
    
    System.out.println("#iter\tMAE\tRMSE");
    for (int i = 1; i <= iter; i++) {
      int userID = r.nextInt(dbReader.getTrainingSet().size());
      SparseVector ratings = dbReader.getTrainingSet().getInstance(userID);
      //double uid = dbReader.getTrainingSet().getLabel(userID);
      double userAvg = 0.0;
      for (VectorEntry e : ratings) {
        userAvg += e.value;
      }
      userAvg /= ratings.size();
      
      for (VectorEntry e : ratings) {
        int itemID = e.index;
        double label = e.value;
        // bloom-filter update
        freqs.add(e.index, e.value, userAvg);
        
        //feature extraction
        double[] features = new double[11];
        computeUserSpecificFeatureVectorPart(ratings, freqs, features);
        computeItemSpecificFinalFeatureVector(freqs, itemID, features);
        SparseVector classificationInstance = new SparseVector(features);
        SparseVector clusterInstance = new SparseVector(3);
        for (int j = 0; j < 3; j++) {
          clusterInstance.put(j, features[j + 8]);
        }
        
        // update clusterer and classifier
        if (i % clusteringFreq == 0) {
          kMeans.update(clusterInstance, label);
          int clusterID = (int)kMeans.predict(clusterInstance);
          models[clusterID].update(classificationInstance, label);
        }
      }
      
      // evaluate on test set
      if (i % printFreq == 0) {
        double[] errors = evaluate(models, kMeans, freqs, dbReader.getEvalSet());
        System.out.println(i + "\t" + errors[0] + "\t" + errors[1]);
      }
    }
  }
  
  public static double[] evaluate(Model[] model, KMeans kMeans, ItemFrequencies freqs, InstanceHolder evalSet) {
    double MAError = 0.0;
    double RMSError = 0.0;
    double counter = 0.0;
    for (int i = 0; i < evalSet.size(); i++) {
      SparseVector ratings = evalSet.getInstance(i);
      for (VectorEntry e : ratings) {
        int itemID = e.index;
        double label = e.value;
        
        //feature extraction
        double[] features = new double[11];
        computeUserSpecificFeatureVectorPart(ratings, freqs, features);
        computeItemSpecificFinalFeatureVector(freqs, itemID, features);
        SparseVector classificationInstance = new SparseVector(features);
        SparseVector clusterInstance = new SparseVector(3);
        for (int j = 0; j < 3; j++) {
          clusterInstance.put(j, features[j + 8]);
        }
        
        int clusterID = (int)kMeans.predict(clusterInstance);
        double predicted = model[clusterID].predict(classificationInstance);
        double expected = label;
        MAError += Math.abs(expected - predicted);
        RMSError += Math.pow(((expected - predicted)/divErr),2);
        counter ++;
      }
    }
    MAError /= counter * divErr;
    RMSError /= counter;
    RMSError = Math.sqrt(RMSError);
    return new double[]{MAError, RMSError};
  }
  
  protected static double numberOfAllUsers = 1.0;
  
  protected static void computeUserSpecificFeatureVectorPart(SparseVector userRatings, ItemFrequencies freqs, double[] featureVectorA) {
    numberOfAllUsers = freqs.numOfUpdates();
    
    // compute the averaged rating of the user
    double userAvgRating = 0.0, userNumberOfRatings = 0.0;
    for (VectorEntry itemRating : userRatings) {
      userAvgRating += itemRating.value;
      userNumberOfRatings ++;
    }
    userAvgRating = (userNumberOfRatings > 0.0) ? userAvgRating / userNumberOfRatings : (numberOfRatings + 1.0) / 2.0; // when no rating for a user, use the mean of rating range
    
    featureVectorA[0] = userAvgRating / numberOfRatings;
    featureVectorA[1] = userNumberOfRatings / numberOfAllUsers;
    double likeSize = 0.0, disLikeSize = 0.0;
    
    // extract features
    for (VectorEntry itemRating : userRatings) {
      int itemID = itemRating.index;
      double rating = itemRating.value; // classLabel
      
      double itemPop = freqs.getNumberOfUsers(itemID) / numberOfAllUsers;
      double itemAvg = freqs.getAverageRating(itemID) / numberOfRatings;
      
      // general
      featureVectorA[2] += itemPop;
      featureVectorA[3] += itemAvg;
      
      // like
      if (rating > userAvgRating) {
        featureVectorA[4] += itemPop;
        featureVectorA[5] += itemAvg;
        likeSize ++;
      }
      
      // dislike
      if (rating < userAvgRating) {
        featureVectorA[6] += itemPop;
        featureVectorA[7] += itemAvg;
        disLikeSize ++;
      }
    }
    
    // normalize features
    featureVectorA[2] = (userRatings.size() > 0) ? featureVectorA[2] / (double)userRatings.size() : 0.0;
    featureVectorA[3] = (userRatings.size() > 0) ? featureVectorA[3] / (double)userRatings.size() : 0.0;
    featureVectorA[4] = (likeSize > 0.0) ? featureVectorA[4] / likeSize : 0.0;
    featureVectorA[5] = (likeSize > 0.0) ? featureVectorA[5] / likeSize : 0.0; 
    featureVectorA[6] = (disLikeSize > 0.0) ? featureVectorA[6] / disLikeSize : 0.0;
    featureVectorA[7] = (disLikeSize > 0.0) ? featureVectorA[7] / disLikeSize : 0.0;
  }
  
  protected static void computeItemSpecificFinalFeatureVector(ItemFrequencies freqs, int itemID, double[] featureVectorA) {
    // get item specific values from set representation 
    featureVectorA[8] = freqs.getAverageRating(itemID) / numberOfRatings;
    featureVectorA[9] = freqs.getNumberOfUsers(itemID) / numberOfAllUsers;
    featureVectorA[10] = freqs.getNumberOfLikeableUsers(itemID) / numberOfAllUsers;
  }
  

}
