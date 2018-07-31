package gossipLearning.temp;

import gossipLearning.interfaces.models.LearningModel;
import gossipLearning.utils.InstanceHolder;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * This class represents the linear regression learner.
 * <br/><br/>
 * Required configuration parameters:<br/>
 * <ul>
 * <li>LinearRegression.lambda - learning rate</li>
 * </ul>
 * @author István Hegedűs
 */
public class LinearRegression implements LearningModel {
  private static final long serialVersionUID = -1468280308189482885L;
  
  /** @hidden */
  protected static final String PAR_LAMBDA = "LinearRegression.lambda";
  protected final double lambda;
  
  /** @hidden */
  private SparseVector w;
  private double bias;
  private double age;
  
  /**
   * This constructor is for initializing the member variables of the Model.
   * 
   * @param prefix The ID of the parameters contained in the Peersim configuration file.
   */
  public LinearRegression(String prefix){
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    w = new SparseVector();
    bias = 0.0;
    age = 0.0;
  }
  
  protected LinearRegression(LinearRegression a){
    w = (SparseVector)a.w.clone();
    bias = a.bias;
    age = a.age;
    lambda = a.lambda;
  }
  
  /**
   * Constructs an object and sets the specified parameters.
   * @param w hyperplane
   * @param bias bias variable
   * @param age number of updates
   * @param lambda learning parameter
   * @param numberOfClasses number of classes
   */
  protected LinearRegression(SparseVector w, double bias, double age, double lambda, int numberOfClasses) {
    this.w = w;
    this.bias = bias;
    this.age = age;
    this.lambda = lambda;
  }
  
  public Object clone(){
    return new LinearRegression(this);
  }

  @Override
  public void update(SparseVector instance, double label) {
    age ++;
    double err = label - predict(instance);
    //System.out.println(label + "\t" + predict(instance));
    
    double nu = 1.0 / (lambda * age);
    w.mul(1.0 - nu * lambda);
    w.add(instance, nu * err);
    bias += nu * lambda * err;
  }
  
  public void update(InstanceHolder instances) {
    for (int i = 0; i < instances.size(); i++) {
      update(instances.getInstance(i), instances.getLabel(i));
    }
  }

  /**
   * In case of linear regression the prediction is w*x + b.
   */
  @Override
  public double predict(SparseVector instance) {
    return w.mul(instance) + bias;
  }

  @Override
  public void setParameters(int numberOfClasses, int numberOfFeatures) {
  }

  @Override
  public String toString() {
    return w.toString() + "\t" + bias;
  }

  @Override
  public double getAge() {
    return age;
  }
  
  @Override
  public void setAge(double age) {
    this.age = age;
  }

  @Override
  public void update(InstanceHolder instances, int epoch, int batchSize) {
    if (batchSize == 0) {
      // full batch update
      for (int e = 0; e < epoch; e++) {
        update(instances);
      }
    } else {
      // mini-batch/SGD update
      InstanceHolder batch = new InstanceHolder(instances.getNumberOfClasses(), instances.getNumberOfFeatures());
      for (int e = 0; e < epoch; e++) {
        for (int i = 0; i < instances.size(); i++) {
          batch.add(instances.getInstance(i), instances.getLabel(i));
          if (0 < batchSize && batch.size() % batchSize == 0) {
            update(batch);
            batch.clear();
          }
        }
      }
      if (0 < batch.size()) {
        update(batch);
        batch.clear();
      }
    }
  }

  @Override
  public void clear() {
    age = 0.0;
    bias = 0.0;
    w.clear();
  }

}
