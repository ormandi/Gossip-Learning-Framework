package gossipLearning.models.learning;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.interfaces.models.SimilarityComputable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;

import peersim.config.Configuration;

public class P2Pegasos extends ProbabilityModel implements SimilarityComputable<P2Pegasos> {
  private static final long serialVersionUID = 5232458167435240109L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "P2Pegasos.lambda";
  protected final double lambda;
  
  /** @hidden */
  protected SparseVector w;
  protected double[] distribution;
  protected int numberOfClasses = 2;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public P2Pegasos(String prefix){
    this(prefix, PAR_LAMBDA);
  }
  
  /**
   * This constructor is for initializing the member variables of the Model. </br>
   * And special configuration parameters can be set.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   * @param PAR_LAMBDA learning rate configuration string
   */
  public P2Pegasos(String prefix, String PAR_LAMBDA) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    w = new SparseVector();
    age = 0.0;
    distribution = new double[numberOfClasses];
  }
  
  /**
   * Returns a new P2Pegasos object that initializes its variables with 
   * the deep copy of the specified parameter.
   * @param a learner to be cloned
   */
  protected P2Pegasos(P2Pegasos a){
    w = (SparseVector)a.w.clone();
    age = a.age;
    distribution = Arrays.copyOf(a.distribution, a.numberOfClasses);
    lambda = a.lambda;
    numberOfClasses = a.numberOfClasses;
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param w hyperplane
   * @param age number of updates
   * @param distribution template variable for the class distribution
   * @param lambda learning parameter
   * @param numberOfClasses number of classes
   */
  protected P2Pegasos(SparseVector w, double age, double[] distribution, double lambda, int numberOfClasses) {
    this.w = w;
    this.age = age;
    this.distribution = distribution;
    this.lambda = lambda;
    this.numberOfClasses = numberOfClasses;
  }
  
  public Object clone(){
    return new P2Pegasos(this);
  }

  /**
   * The official Pegasos update with the specified instances and corresponding label.
   */
  @Override
  public void update(final SparseVector instance, double label) {
    /*label = (label == 0.0) ? -1.0 : label;
    age ++;
    double nu = 1.0 / (lambda * age);
    boolean isSV = label * w.mul(instance) < 1.0;
    
    w.mul(1.0 - nu * lambda);
    if (isSV) {
      w.add(instance, nu * label);
    }*/
    age ++;
    double nu = 1.0 / (lambda * age);
    
    gradient(instance, label);
    w.add(gradient, -nu);
  }
  
  public void update(InstanceHolder instances) {
    age += instances.size();
    double nu = 1.0 / (lambda * age);
    
    gradient(instances);
    w.add(gradient, -nu);
  }
  
  protected SparseVector gradient = new SparseVector();
  protected void gradient(SparseVector instance, double label) {
    gradient.clear();
    label = (label == 0.0) ? -1.0 : label;
    boolean isSV = label * w.mul(instance) < 1.0;
    if (isSV) {
      gradient.add(instance, -label);
    }
    gradient.add(w, lambda);
  }
  
  protected void gradient(InstanceHolder instances) {
    gradient.clear();
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      label = (label == 0.0) ? -1.0 : label;
      boolean isSV = label * w.mul(instance) < 1.0;
      if (isSV) {
        gradient.add(instance, -label);
      }
    }
    gradient.add(w, lambda * instances.size());
  }
  
  /**
   * Computes the inner product of the hyperplane and the specified instance. 
   * If it is greater than 0 then the label is positive (1.0), otherwise the label is
   * negative (0.0).</br></br>
   * The first value of the result vector is 0.0, the second is the value of 
   * the inner product.
   */
  @Override
  public double[] distributionForInstance(SparseVector instance) {
    double innerProd = w.mul(instance);
    distribution[0] = 0.0;
    distribution[1] = innerProd;
    return distribution;
  }
  
  /**
   * Returns the cosine similarity of the hyperplanes of the current and the specified models. 
   */
  @Override
  public double computeSimilarity(P2Pegasos model) {
    return w.cosSim(model.w);
  }
  
  /**
   * It returns the string representation of the hyperplane.
   * 
   * @return String representation
   */
  public String toString() {
    return w.toString() + ", age: " + age;
  }
  
  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    if (numberOfClasses != 2) {
      throw new RuntimeException("Not supported number of classes in " + getClass().getCanonicalName() + " which is " + numberOfClasses + "!");
    }
    this.numberOfClasses = numberOfClasses;
  }
}
