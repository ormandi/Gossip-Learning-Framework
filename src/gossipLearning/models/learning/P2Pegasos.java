package gossipLearning.models.learning;

import java.util.Arrays;

import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.interfaces.models.SimilarityComputable;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

public class P2Pegasos extends ProbabilityModel implements SimilarityComputable<P2Pegasos> {
  private static final long serialVersionUID = 5232458167435240109L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "P2Pegasos.lambda";
  protected double lambda;
  
  /** @hidden */
  protected SparseVector w;
  protected double[] distribution;
  protected int numberOfClasses = 2;
  
  /**
   * Creates a default model with age=0 and the separating hyperplane is the 0 vector.
   */
  public P2Pegasos(){
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
  
  public Object clone(){
    return new P2Pegasos(this);
  }

  /**
   * Initialize the age=0 and the separating hyperplane=0 vector.
   */
  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
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
