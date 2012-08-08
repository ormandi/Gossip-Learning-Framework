package gossipLearning.main;

import gossipLearning.DataBaseReader;
import gossipLearning.InstanceHolder;
import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.models.clusterer.KMeans;
import gossipLearning.utils.SparseVector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.ParsedProperties;
import weka.classifiers.Classifier;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.LibSVMLoader;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class RecSysTest {

  protected static double numberOfRatings;
  protected static double numberOfAllUsers;
  protected static int numOfClusters;
  protected static int seed;
  protected static double divErr;
  
  protected static String classifierName = null;
  protected static String modelName = null;
  protected static String prefix;
  protected static String wekaOptions = null;
  
  protected static double[] getNumberOfUsers;
  protected static double[] getAverageRating;
  protected static double[] getNumberOfLikeableUsers;
  
  protected static double getNumberOfUsers(int itemID) {
    return getNumberOfUsers[itemID];
  }
  
  protected static double getAverageRating(int itemID) {
    return getAverageRating[itemID];
  }
  
  protected static double getNumberOfLikeableUsers(int itemID) {
    return getNumberOfLikeableUsers[itemID];
  }
  
  protected static void computeUserSpecificFeatureVectorPart(SparseVector userRatings, double[] featureVectorA) {
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
      
      double itemPop = getNumberOfUsers(itemID) / numberOfAllUsers;
      double itemAvg = getAverageRating(itemID) / numberOfRatings;
      
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
  
  protected static void computeItemSpecificFinalFeatureVector(int itemID, double[] featureVectorA) {
    // get item specific values from set representation 
    featureVectorA[8] = getAverageRating(itemID) / numberOfRatings;
    featureVectorA[9] = getNumberOfUsers(itemID) / numberOfAllUsers;
    featureVectorA[10] = getNumberOfLikeableUsers(itemID) / numberOfAllUsers;
  }
  
  protected static void weka(InstanceHolder trainingSet, InstanceHolder testSet) throws Exception {
    // loading weka instances
    //System.err.println("#Loading datasets to weka Instances");
    LibSVMLoader loader = new LibSVMLoader();
    loader.setSource(new ByteArrayInputStream(trainingSet.toString().getBytes()));
    Instances rawTrainInstances = loader.getDataSet();
    loader = new LibSVMLoader();
    loader.setSource(new ByteArrayInputStream(testSet.toString().getBytes()));
    Instances rawTestInstances = loader.getDataSet();
    
    /*System.out.println("#Transform class label to nominal");
    NumericToNominal classFilter = new NumericToNominal();
    classFilter.setAttributeIndices("12");
    classFilter.setInputFormat(rawTrainInstances);
    Instances trainInstances = Filter.useFilter(rawTrainInstances, classFilter);
    classFilter = new NumericToNominal();
    classFilter.setAttributeIndices("12");
    classFilter.setInputFormat(rawTestInstances);
    Instances testInstances = Filter.useFilter(rawTestInstances, classFilter);
    */
    Instances trainInstances = new Instances(rawTrainInstances);
    Instances testInstances = new Instances(rawTestInstances);
    
    //System.out.println(trainInstances.numInstances() + "\t" + trainingSet.size());
    //System.out.println(testInstances.numInstances() + "\t" + testSet.size());
    //System.out.println(trainInstances);
    //System.out.println(testInstances);
    
    
    //System.err.println("#Filtering datasets for clutering");
    Remove filter = new Remove();
    filter.setAttributeIndices("9,10,11");
    filter.setInvertSelection(true);
    filter.setInputFormat(trainInstances);
    Instances filteredTrain = Filter.useFilter(trainInstances, filter);
    filter = new Remove();
    filter.setAttributeIndices("9,10,11");
    filter.setInvertSelection(true);
    filter.setInputFormat(trainInstances);
    Instances filteredTest = Filter.useFilter(testInstances, filter);
    
    //System.err.println("#Run clustering");
    SimpleKMeans kMeans = new SimpleKMeans();
    kMeans.setNumClusters(numOfClusters);
    kMeans.setSeed(seed);
    kMeans.buildClusterer(filteredTrain);
    //System.err.println(kMeans);
    
    //System.err.println("#Separating instances");
    Instances[] trainings = new Instances[numOfClusters];
    for (int i = 0; i < numOfClusters; i++) {
      trainings[i] = new Instances(trainInstances, trainInstances.numInstances());
    }
    for (int i = 0; i < filteredTrain.numInstances(); i++) {
      int cluterID = kMeans.clusterInstance(filteredTrain.instance(i));
      trainings[cluterID].add(trainInstances.instance(i));
    }
    
    //System.err.print("#Building clussifiers");
    
    Classifier[] classifier = new Classifier[numOfClusters];
    for (int i = 0; i < numOfClusters; i++) {
      //System.err.print("\t" + i);
      classifier[i] = (Classifier)Class.forName(classifierName).newInstance();
      if (wekaOptions != null) {
        classifier[i].setOptions(Utils.splitOptions(wekaOptions));
      }
      classifier[i].buildClassifier(trainings[i]);
    }
    //System.err.println();
    
    //System.err.println("#Evaluation");
    double MAError = 0.0;
    double RMSError = 0.0;
    for (int i = 0; i < testInstances.numInstances(); i++) {
      int clusterID = kMeans.clusterInstance(filteredTest.instance(i));
      double expected = testInstances.instance(i).classValue();
      double predicted = classifier[clusterID].classifyInstance(testInstances.instance(i));
      //predicted = Math.round(predicted);
      //System.out.println(expected + "\t" + predicted);
      MAError += Math.abs(expected - predicted);
      RMSError += Math.pow(((expected - predicted)/divErr),2);
    }
    MAError /= testInstances.numInstances() * divErr;
    RMSError /= testInstances.numInstances();
    RMSError = Math.sqrt(RMSError);
    System.out.println("Weka MAE: " + MAError);
    System.out.println("Weka RMSE: " + RMSError);
    /*for (int i = 0; i < numOfClusters; i++) {
      System.out.println(classifier[i]);
    }*/
  }
  
  protected static void golf(InstanceHolder trainingSet, InstanceHolder testSet) throws Exception {
    int numatts = 3;
    int fromAtt = 8;
    InstanceHolder filteredTrain = new InstanceHolder(trainingSet.getNumberOfClasses(), numatts);
    InstanceHolder filteredTest = new InstanceHolder(testSet.getNumberOfClasses(), numatts);
    for (int i = 0; i < trainingSet.size(); i++) {
      SparseVector instance = new SparseVector(numatts);
      for (int j = 0; j < numatts; j++) {
        instance.put(j, trainingSet.getInstance(i).get(j + fromAtt));
      }
      filteredTrain.add(instance, trainingSet.getLabel(i));
    }
    for (int i = 0; i < testSet.size(); i++) {
      SparseVector instance = new SparseVector(numatts);
      for (int j = 0; j < numatts; j++) {
        instance.put(j, testSet.getInstance(i).get(j + fromAtt));
      }
      filteredTest.add(instance, testSet.getLabel(i));
    }
    KMeans kMeans = new KMeans(numOfClusters);
    Random r = new Random(seed);
    for (int i = 0; i < 2*filteredTrain.size(); i++) {
      int index = r.nextInt(filteredTrain.size());
      kMeans.update(filteredTrain.getInstance(index), filteredTrain.getLabel(index));
    }
    
    Model[] models = new Model[numOfClusters];
    for (int i = 0; i < numOfClusters; i++) {
      models[i] = (Model)Class.forName(modelName).newInstance();
      models[i].init(prefix);
      models[i].setNumberOfClasses((int)numberOfRatings);
    }
    
    for (int i = 0; i < 10*trainingSet.size(); i++) {
      int index = r.nextInt(trainingSet.size());
      int clusterID = (int)kMeans.predict(filteredTrain.getInstance(index));
      models[clusterID].update(trainingSet.getInstance(index), trainingSet.getLabel(index));
      //if (i%100 == 0) System.out.println(i + "\t" + evaluate(models[clusterID], trainingSet) + "\t" + evaluate(models[clusterID], testSet));// + "\t" + models[clusterID]);
    }
    
    double MAError = 0.0;
    double RMSError = 0.0;
    for (int i = 0; i < testSet.size(); i++) {
      int clusterID = (int)kMeans.predict(filteredTest.getInstance(i));
      double predicted = models[clusterID].predict(testSet.getInstance(i));
      double expected = testSet.getLabel(i);
      //System.out.println(expected + "\t" + predicted);
      MAError += Math.abs(expected - predicted);
      RMSError += Math.pow(((expected - predicted)/divErr),2);
    }
    MAError /= testSet.size() * divErr;
    RMSError /= testSet.size();
    RMSError = Math.sqrt(RMSError);
    System.out.println("GoLF MAE: " + MAError);
    System.out.println("GoLF RMSE: " + RMSError);
    /*for (int i = 0; i < numOfClusters; i++) {
      System.out.println(models[i]);
    }*/
  }
  
  public static double evaluate(Model model, InstanceHolder evalSet) {
    double MAError = 0.0;
    double RMSError = 0.0;
    for (int i = 0; i < evalSet.size(); i++) {
      double predicted = model.predict(evalSet.getInstance(i));
      double expected = evalSet.getLabel(i);
      MAError += Math.abs(expected - predicted);
      RMSError += Math.pow(((expected - predicted)/divErr),2);
    }
    MAError /= evalSet.size() * divErr;
    RMSError /= evalSet.size();
    RMSError = Math.sqrt(RMSError);
    //System.out.print("\tMAE: " + MAError);
    //System.out.println("RMSE: " + RMSError);
    return MAError;
  }
  
  public static void main(String[] args) throws Exception{
    String configName = args[0];
    Configuration.setConfig(new ParsedProperties(configName));
    //System.err.println("#Reading datasets from files");
    File tFile = new File(Configuration.getString("trainingFile"));
    File eFile = new File(Configuration.getString("evaluationFile"));
    String dbReaderName = "gossipLearning.RecSysDataBaseReader";
    
    seed = Configuration.getInt("SEED");
    numOfClusters = Configuration.getInt("numOfClusters");
    divErr = Configuration.getDouble("divErr");
    numberOfRatings = Configuration.getDouble("numOfRatings");
    numberOfAllUsers = Configuration.getDouble("numOfAllUsers");
    
    classifierName = Configuration.getString("classifier", null);
    wekaOptions = Configuration.getString("wekaOptions", null);
    
    prefix = "model";
    modelName = Configuration.getString(prefix, null);
    
    boolean isNormalize = Configuration.getBoolean("normalize");
    int polyDegree = Configuration.getInt("polyDegree");
    
    DataBaseReader dbReader = DataBaseReader.createDataBaseReader(dbReaderName, tFile, eFile);
    
    //System.err.println("#Preprocessing datasets");
    // pre-processing database
    getNumberOfUsers = new double[dbReader.getTrainingSet().getNumberOfFeatures()];
    getAverageRating = new double[dbReader.getTrainingSet().getNumberOfFeatures()];
    getNumberOfLikeableUsers = new double[dbReader.getTrainingSet().getNumberOfFeatures()];
    for (int i = 0; i < dbReader.getTrainingSet().size(); i++) {
      for (VectorEntry e : dbReader.getTrainingSet().getInstance(i)) {
        getNumberOfUsers[e.index] ++;
        getAverageRating[e.index] += e.value;
      }
    }
    for (int i = 0; i < dbReader.getTrainingSet().getNumberOfFeatures(); i++) {
      getAverageRating[i] = getNumberOfUsers[i] == 0.0 ? 0.0 : getAverageRating[i] / getNumberOfUsers[i];
    }
    
    for (int i = 0; i < dbReader.getTrainingSet().size(); i++) {
      for (VectorEntry e : dbReader.getTrainingSet().getInstance(i)) {
        if (e.value > getAverageRating[e.index]) {
          getNumberOfLikeableUsers[e.index] ++;
        }
      }
    }
    
    //System.err.println("#Extracting features");
    // feature extraction
    InstanceHolder trainingSet = new InstanceHolder((int)numberOfRatings, 11);
    for (int userID = 0; userID < dbReader.getTrainingSet().size(); userID ++) {
      for (VectorEntry e : dbReader.getTrainingSet().getInstance(userID)) {
        int itemID = e.index;
        double label = e.value;
        double[] features = new double[11];
        computeUserSpecificFeatureVectorPart(dbReader.getTrainingSet().getInstance(userID), features);
        computeItemSpecificFinalFeatureVector(itemID, features);
        SparseVector instance = new SparseVector(features);
        trainingSet.add(instance, label);
      }
    }
    InstanceHolder testSet = new InstanceHolder((int) numberOfRatings, 11);
    for (int userID = 0; userID < dbReader.getEvalSet().size(); userID ++) {
      for (VectorEntry e : dbReader.getEvalSet().getInstance(userID)) {
        int itemID = e.index;
        double label = e.value;
        double[] features = new double[11];
        computeUserSpecificFeatureVectorPart(dbReader.getTrainingSet().getInstance(userID), features);
        computeItemSpecificFinalFeatureVector(itemID, features);
        SparseVector instance = new SparseVector(features);
        testSet.add(instance, label);
      }
    }
    
    File tout = new File("tr.dat");
    File eout = new File("ev.dat");
    
    trainingSet.writeToFile(tout);
    testSet.writeToFile(eout);
    
    DataBaseReader dbr = DataBaseReader.createDataBaseReader("gossipLearning.DataBaseReader", tout, eout);
    
    dbr.polynomize(polyDegree);
    //dbr.writeToFile(new File("tr_poly4.dat"), new File("ev_poly4.dat"));
    //System.exit(0);

    if (isNormalize) {
      dbr.standardize();
    }
    
    if (classifierName != null) {
      weka(dbr.getTrainingSet(), dbr.getEvalSet());
      //weka(trainingSet, testSet);
    }
    
    if (modelName != null) {
      golf(dbr.getTrainingSet(), dbr.getEvalSet());
      //golf(trainingSet, testSet);
    }
    
    tout.deleteOnExit();
    eout.deleteOnExit();
  }

}
