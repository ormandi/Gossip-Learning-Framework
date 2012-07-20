package gossipLearning.models.recSys;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.models.clusterer.KMeans;
import gossipLearning.utils.Pair;
import gossipLearning.utils.SparseVector;

import java.util.Collections;
import java.util.Vector;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

/**
 * 
 * @author Róbert Ormándi
 */
public abstract class AbstractRecSysModel implements RecSysModel {
  protected static final long serialVersionUID = 8437206766184831672L;
  
  /**
   * This parameter defines how often perform model update steps relative to the 
   * item set updates. The default value is 10 which means the set update is 10 times
   * more frequent than the model updates.
   */
  protected static final String PAR_MODEL_UPDATE_FREQUENCY = "recsys.modelUpdateFrequency";
  protected static final int DEFAULT_MODEL_UPDATE_FREQUENCY = 10;
  
  /**
   * It defines the number of counters applied in the Bloom Filters that represents the
   * item sets. 
   */
  protected static final String PAR_NUMBER_OF_COUNTERS = "recsys.numberOfCounters";
  protected static final int DEFAULT_NUMBER_OF_COUNTERS = 30;
  
  /**
   * It defines the number of hash functions used in the Bloom Filters that describes the item
   * related features.
   */
  protected static final String PAR_NUMBER_OF_HASH_FUNCTIONS = "recsys.numberOfHashFunctions";
  protected static final int DEFAULT_NUMBER_OF_HASH_FUNCTIONS = 4;
  
  /**
   * We use a concrete feature set for model based learning in which there is a fixed number of feature. 
   * Since the number of features can be considered to constant.
   */
  protected static final int NUMBER_OF_FEATURES = 11;
  
  /**
   * It defines the number of clusters. The default value of this parameter is zero meaning no clustering performed.
   */
  protected static final String PAR_NUMBER_OF_CLUSTERS = "recsys.clusters";
  protected static final int DEFAULT_NUMBER_OF_CLUSTERS = 0;  // it means no clustering performed
  
  /**
   * Defines how many steps should be taken by the clusterer.
   */
  protected static final String PAR_NUMBER_OF_CLUSTERING_STEPS = "recsys.clusteringSteps";
  protected static final int DEFAULT_NUMBER_OF_CLUSTERING_STEPS = 200;
  
  /**
   * Defines the class of used model.
   */
  protected static final String PAR_MODEL_CLASS = "recsys.model";
  protected static final String DEFAULT_MODEL_CLASS = "gossipLearning.models.P2Pegasos";
  
  protected int numberOfItems = 0;
  protected int numberOfRatings = 0;
  protected int modelUpdateFrequency = DEFAULT_MODEL_UPDATE_FREQUENCY;
  protected int numberOfClusters = DEFAULT_NUMBER_OF_CLUSTERS;
  protected int numberOfClusteringSteps = DEFAULT_NUMBER_OF_CLUSTERING_STEPS;
  protected String prefix = "";
  protected String modelClassName = DEFAULT_MODEL_CLASS;
  protected Node node = null;
  protected int numberOfCounters = DEFAULT_NUMBER_OF_COUNTERS;
  protected int numberOfHashFunctions = DEFAULT_NUMBER_OF_HASH_FUNCTIONS;
  
  // set representation
  protected ItemFrequencies itemFreqs;
  
  // clusterer representation
  protected Model clusterer;
  
  @Override
  public Object clone() {
    throw new RuntimeException("Cloning the " + getClass().getCanonicalName() + " is not possible since it is abstract!");
  }
  
  @Override
  public void init(String prefix) {
    this.prefix = prefix;
    modelUpdateFrequency = Configuration.getInt(prefix + "." + PAR_MODEL_UPDATE_FREQUENCY, DEFAULT_MODEL_UPDATE_FREQUENCY);
    numberOfClusters = Configuration.getInt(prefix + "." + PAR_NUMBER_OF_CLUSTERS, DEFAULT_NUMBER_OF_CLUSTERS);
    numberOfClusteringSteps = Configuration.getInt(prefix + "." + PAR_NUMBER_OF_CLUSTERING_STEPS, DEFAULT_NUMBER_OF_CLUSTERING_STEPS);
    numberOfCounters = Configuration.getInt(prefix + "." + PAR_NUMBER_OF_COUNTERS, DEFAULT_NUMBER_OF_COUNTERS);
    numberOfHashFunctions = Configuration.getInt(prefix + "." + PAR_NUMBER_OF_HASH_FUNCTIONS, DEFAULT_NUMBER_OF_HASH_FUNCTIONS);
    modelClassName = Configuration.getString(prefix + "." + PAR_MODEL_CLASS, DEFAULT_MODEL_CLASS);
  }

  /**
   * It receives a special type of training example which can be described as follows:<br/>
   * The instance contains the rates of a user in sparse format i.e. the keys of the instance
   * are the item IDs and the values are the ratings. Where the rating equals to 0 it means
   * no rating is given.<br/>
   * The label refers to the user who assigned the given ratings to the items i.e. the label contains
   * the user ID.
   */
  @Override
  public void update(SparseVector instance, double label) {
    // store the node to which the current model arrived
    final int userID = (int) label; 
    node = Network.get(userID);
    
    // compute the averaged rating of the user
    double userAvgRating = 0.0, userNumberOfRatings = 0.0;
    for (VectorEntry itemRating : instance) {
      userAvgRating += itemRating.value;
      userNumberOfRatings ++;
    }
    userAvgRating = (userNumberOfRatings > 0.0) ? userAvgRating / userNumberOfRatings : (numberOfRatings + 1.0) / 2.0; // when no rating for a user, use the mean of rating range
    
    // perform item set update
    for (VectorEntry itemRating : instance) {
      int itemID = itemRating.index;
      double rating = itemRating.value;
      itemFreqs.add(itemID, rating, userAvgRating);
    }
    
    // check whether model update is needed or not
    if (CommonState.getTime() % modelUpdateFrequency == 0) {
      // get the number of all users
      int numberOfAllUsers = 10000;          // FIXME: correct normalization constant
      
      // perform user specific feature extraction
      double[] featureVectorA = new double[NUMBER_OF_FEATURES];
      computeUserSpecificFeatureVectorPart(instance, featureVectorA, numberOfAllUsers, true);
      
      // continue feature extraction on item specific features and update models
      for (VectorEntry itemRating : instance) {
        int itemID = itemRating.index;
        double rating = itemRating.value; // classLabel
        
        // get feature vector
        Pair<SparseVector, Integer> fc = computeItemSpecificFinalFeatureVector(itemID, featureVectorA, numberOfAllUsers);
        SparseVector featureVector = fc.getKey();
        int clusterID = fc.getValue();
          
        // update model
        updateModel(featureVector, clusterID, rating);
      }
    }
  }
  
  /**
   * Computes all user specific feature value from the ratings of the given user
   * 
   * @param userRatings user ratings
   * @param featureVectorA feature vector
   * @return The clusterer model of the items or null if no clustering was performed
   */
  protected void computeUserSpecificFeatureVectorPart(SparseVector userRatings, double[] featureVectorA, int numberOfAllUsers, boolean computeClusterer) {
    // compute the averaged rating of the user
    double userAvgRating = 0.0, userNumberOfRatings = 0.0;
    for (VectorEntry itemRating : userRatings) {
      userAvgRating += itemRating.value;
      userNumberOfRatings ++;
    }
    userAvgRating = (userNumberOfRatings > 0.0) ? userAvgRating / userNumberOfRatings : (numberOfRatings + 1.0) / 2.0; // when no rating for a user, use the mean of rating range
    
    featureVectorA[0] = userAvgRating;
    featureVectorA[1] = userNumberOfRatings;
    double likeSize = 0.0, disLikeSize = 0.0;
    
    // check whether building clusterer is necessary
    if (numberOfClusters > 0 && computeClusterer) {
      // create database for clustering
      Vector<SparseVector> clusteringDB = new Vector<SparseVector>();
      for (VectorEntry itemRating : userRatings) {
        int itemID = itemRating.index;
        double[] clusteringDBVec = new double[3];
        
        clusteringDBVec[0] = itemFreqs.getAverageRating(itemID);
        clusteringDBVec[1] = (double)itemFreqs.getNumberOfUsers(itemID) / (double)numberOfAllUsers;
        clusteringDBVec[2] = itemFreqs.getNumberOfLikeableUsers(itemID);
        
        clusteringDB.add(new SparseVector(clusteringDBVec));
      }
      // shuffling clustering database 
      Collections.shuffle(clusteringDB);
      
      // perform clustering
      clusterer = new KMeans(numberOfClusters);
      for (int step = 1; step <= numberOfClusteringSteps; step ++) {
        clusterer.update(clusteringDB.get(clusteringDB.size() % step), 0.0);
      }
    }
    
    
    // extract features
    for (VectorEntry itemRating : userRatings) {
      int itemID = itemRating.index;
      double rating = itemRating.value; // classLabel
      
      double itemPop = (double)itemFreqs.getNumberOfUsers(itemID) / (double)numberOfAllUsers;
      double itemAvg = itemFreqs.getAverageRating(itemID);
      
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
  
  protected Pair<SparseVector, Integer> computeItemSpecificFinalFeatureVector(int itemID, double[] featureVectorA, int numberOfAllUsers) {
    // get item specific values from set representation 
    featureVectorA[8] = itemFreqs.getAverageRating(itemID);
    featureVectorA[9] = (double)itemFreqs.getNumberOfUsers(itemID) / (double)numberOfAllUsers;
    featureVectorA[10] = itemFreqs.getNumberOfLikeableUsers(itemID);
    
    // get the cluster ID if necessary
    int clusterID = itemID;
    if (numberOfClusters > 0) {
      SparseVector clusteringInstance = new SparseVector(3);
      clusteringInstance.put(0, featureVectorA[8]);
      clusteringInstance.put(1, featureVectorA[9]);
      clusteringInstance.put(2, featureVectorA[10]);
      clusterID = (int) clusterer.predict(clusteringInstance);
    }
    
    // create final feature vector (the corresponding class label is in variable rating)
    //SparseVector featureVector = new SparseVector(featureVectorA);
    return new Pair<SparseVector, Integer>(new SparseVector(featureVectorA), clusterID);
  }

  /**
   * Returns the number of ratings.
   * 
   * @return number of ratings
   */
  @Override
  public int getNumberOfClasses() {
    return numberOfRatings;
  }
  
  /**
   * Sets the number of ratings. Since - in some cases - the model structure depends on
   * the number of ratings (see RBM recSys) this call reinitialize the model structure.
   * Moreover the item set structure also depends on the number of ratings the set structure
   * is reinitialized as well.
   * 
   * @param numberOfClasses new number of ratings 
   */
  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses > 0) {
      numberOfRatings = numberOfClasses;
      
      // reinitialize set structure
      itemFreqs = new ItemFrequencies(numberOfRatings, numberOfCounters, numberOfHashFunctions);
      
      // reinitialize model structure
      initializeModels(numberOfClusters, numberOfRatings);
    } else {
      throw new RuntimeException("The number of ratings shouldn't be negative of zero!");
    }
  }
  
  /**
   * Returns the number of items.
   * 
   * @return number of items
   */
  @Override
  public int getNumberOfItems() {
    return numberOfItems;
  }
  
  /**
   * Sets the number of items.
   * 
   * @param numberOfClasses new number of ratings 
   */
  @Override
  public void setNumberOfItems(int items) {
    numberOfItems = items;
  }
  
  /**
   * Returns a reference to the node which currently stores this model. The node update is
   * performed in the updateModel method which is called always when a model arrives to a new node.
   * 
   * @return reference to the node which holds this model
   */
  @Override
  public Node getNode() {
    return node;
  }
  
  // --------------- Abstract Methods ---------------
  
  /**
   * Initializes the model structure.
   */
  protected abstract void initializeModels(int numberOfClusters, int numberOfRatings);
  
  /**
   * Returns the model that is responsible for handling the given clusterID and rating value.<br/>
   * In some implementation the model selection depends on either of the parameters. In these cases
   * the value of the non-dependent parameter can be arbitrary.
   * 
   * @param featureVector training example
   * @param clusterID itemID of the model (only in case of item based models)
   * @param rating rating of the model (only in case of rate based models)
   * @return responsible model
   */
  protected abstract void updateModel(SparseVector featureVector, int clusterID, double rating);

}
