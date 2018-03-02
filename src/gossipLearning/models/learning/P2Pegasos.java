package gossipLearning.models.learning;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.interfaces.models.SimilarityComputable;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class P2Pegasos extends ProbabilityModel implements SimilarityComputable<P2Pegasos> {
  private static final long serialVersionUID = 5232458167435240109L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "P2Pegasos.lambda";
  protected final double lambda;
  
  /** @hidden */
  protected SparseVector w;
  protected SparseVector gradient;
  
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
    gradient = new SparseVector();
  }
  
  /**
   * Returns a new P2Pegasos object that initializes its variables with 
   * the deep copy of the specified parameter.
   * @param a learner to be cloned
   */
  protected P2Pegasos(P2Pegasos a){
    super(a);
    w = (SparseVector)a.w.clone();
    lambda = a.lambda;
    gradient = (SparseVector)a.gradient.clone();
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
    //bias -= nu * biasGradient;
  }
  
  protected void gradient(SparseVector instance, double label) {
    gradient.set(w).mul(lambda);
    label = (label == 0.0) ? -1.0 : label;
    boolean isSV = label * w.mul(instance) < 1.0;
    if (isSV) {
      gradient.add(instance, -label);
    }
  }
  
  protected void gradient(InstanceHolder instances) {
    gradient.set(w).mul(lambda * instances.size());
    for (int i = 0; i < instances.size(); i++) {
      SparseVector instance = instances.getInstance(i);
      double label = instances.getLabel(i);
      
      label = (label == 0.0) ? -1.0 : label;
      boolean isSV = label * w.mul(instance) < 1.0;
      if (isSV) {
        gradient.add(instance, -label);
      }
    }
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

}
