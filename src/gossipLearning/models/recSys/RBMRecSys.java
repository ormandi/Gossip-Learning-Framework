package gossipLearning.models.recSys;

import gossipLearning.interfaces.LearningProtocol;
import gossipLearning.interfaces.Model;
import gossipLearning.utils.Pair;
import gossipLearning.utils.SparseVector;
import peersim.core.Network;

public class RBMRecSys extends AbstractRecSysModel {
  private static final long serialVersionUID = 1370247633525862204L;
  protected Model[] model;
  
  public RBMRecSys() {
  }
  
  public RBMRecSys(RBMRecSys o) {
    // copy the field of base class
    numberOfItems = o.numberOfItems;
    numberOfRatings = o.numberOfRatings;
    modelUpdateFrequency = o.modelUpdateFrequency;
    numberOfClusters = o.numberOfClusters;
    numberOfClusteringSteps = o.numberOfClusteringSteps;
    node = o.node;
    numberOfCounters = o.numberOfCounters;
    numberOfHashFunctions = o.numberOfHashFunctions;
    //itemFreqs = (ItemFrequencies) o.itemFreqs.clone();
    prefix = o.prefix;
    modelClassName = o.modelClassName;
    pid = o.pid;
    //clusterer = o.clusterer; // not necessary to clone it since in every update step it is rebuilt => reference is enough for predict
    age = o.age;
    
    // copy models
    int numClusters = numberOfClusters == 0 ? 1 : numberOfClusters;
    model = null;
    if (o.model != null) {
      model = new Model[numClusters];
      for (int i = 0; i < numClusters; i++) {
        model[i] = (o.model[i] != null) ? (Model) o.model[i].clone() : null;
      }
    }
  }
  
  @Override
  public Object clone() {
    return new RBMRecSys(this);
  }

  @Override
  public double predict(SparseVector instance) {
    if (node != null) {
      int userID = (int) node.getID();
      int itemID = instance.iterator().next().index;
      
      // get the number of all users
      int numberOfAllUsers = 10000;          // FIXME: correct normalization constant
      
      // get userRatings
      SparseVector userRatings = ((LearningProtocol)Network.get(userID).getProtocol(pid)).getInstanceHolder().getInstance(0);
      
      // compute user specific part of feature vector
      double[] featureVectorA = new double[NUMBER_OF_FEATURES];
      computeUserSpecificFeatureVectorPart(userRatings, featureVectorA, numberOfAllUsers, false, userID);
      
      // compute item specific part of feature vector
      Pair<SparseVector, Integer> fc = computeItemSpecificFinalFeatureVector(itemID, featureVectorA, numberOfAllUsers);
      SparseVector featureVector = fc.getKey();
      
      // evaluate models
      int clusterID = numberOfClusters == 0 ? 0 : fc.getValue();
      return model[clusterID].predict(featureVector) + 1;
    }
    return (getNumberOfClasses() + 1) / 2.0;
  }

  @Override
  protected void initializeModels(int numberOfClusters, int numberOfRatings) {
    int numClasters = numberOfClusters == 0 ? 1 : numberOfClusters;
    try {
      model = new Model[numClasters];
      for (int i = 0; i < numClasters; i++) {
        model[i] = (Model) Class.forName(modelClassName).newInstance();
        model[i].init(prefix + "." + PAR_MODEL_CLASS);
        model[i].setNumberOfClasses(numberOfRatings);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void updateModel(SparseVector featureVector, int clusterID, double rating) {
    int rateingID = (int) rating - 1;
    clusterID = numberOfClusters == 0 ? 0 : clusterID;
    model[clusterID].update(featureVector, rateingID);
  }

}
