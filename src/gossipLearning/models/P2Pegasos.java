package gossipLearning.models;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.ProbabilityModel;
import gossipLearning.interfaces.SimilarityComputable;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class P2Pegasos extends ProbabilityModel implements Model, SimilarityComputable<P2Pegasos> {
  private static final long serialVersionUID = 5232458167435240109L;
  
  /**
   * The learning parameter is 0.0001 by default.
   */
  protected static final String PAR_LAMBDA = "lambda";
  protected double lambda = 0.0001;
  
  /** @hidden */
  protected SparseVector w;
  protected double age;
  protected int numberOfClasses = 2;
  
  /**
   * Creates a default model with age=0 and the separating hyperplane is the 0 vector.
   */
  public P2Pegasos(){
    w = new SparseVector();
    age = 0.0;
  }
  
  /**
   * Returns a new P2Pegasos object that initializes its variables with 
   * the deep copy of the specified parameters.
   * @param w hyperplane
   * @param age model age
   * @param lambda learning parameter
   */
  protected P2Pegasos(SparseVector w, double age, double lambda, int numberOfClasses){
    this.w = (SparseVector)w.clone();
    this.age = age;
    this.lambda = lambda;
    this.numberOfClasses = numberOfClasses;
  }
  
  public Object clone(){
    return new P2Pegasos(w, age, lambda, numberOfClasses);
  }

  /**
   * Initialize the age=0 and the separating hyperplane=0 vector.
   */
  @Override
  public void init(String prefix) {
    w = new SparseVector();
    age = 0.0;
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.0001);
  }

  /**
   * The official Pegasos update with the specified instances and corresponding label.
   */
  @Override
  public void update(final SparseVector instance, double label) {
    label = (label == 0.0) ? -1.0 : label;
    age ++;
    double nu = 1.0 / (lambda * age);
    boolean isSV = label * w.mul(instance) < 1.0;
    
    w.mul(1.0 - 1.0 / age);
    if (isSV) {
      w.add(instance, nu * label);
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
    return new double[]{0.0, innerProd};
  }
  /*@Override
  public double predict(final SparseVector instance) {
    double innerProd = w.mul(instance);
    return innerProd > 0.0 ? 1.0 : 0.0;
  }*/

  /**
   * Returns the cosine similarity of the hyperplanes of the current and the specified models. 
   */
  @Override
  public double computeSimilarity(final P2Pegasos model) {
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
