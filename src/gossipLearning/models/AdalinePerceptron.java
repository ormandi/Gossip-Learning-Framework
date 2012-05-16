/**
 *
 */
package gossipLearning.models;

import gossipLearning.interfaces.Model;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * @author csko
 * A Model for the Adaline Perceptron.
 * @note This implementation is provided for educational purposes. Another version of the Adaline
 * perceptron can be found in the weakLearners package.
 */
public class AdalinePerceptron implements Model {
  protected static final String PAR_LAMBDA = "adaline.lambda";
	/** @hidden */
	protected SparseVector w;
	protected double age;
	protected int numberOfClasses = 2;
	protected double lambda = 0.1;

	/**
	 * Returns a clone of this object.
	 * 
	 * @return the clone of this object
	 */
	public Object clone() {
		AdalinePerceptron tmp = new AdalinePerceptron();
		tmp.w = (SparseVector)w.clone();
		tmp.age = age;
    tmp.numberOfClasses = numberOfClasses;
    tmp.lambda = lambda;
		return tmp;
	}

	private static final long serialVersionUID = 6040117517300790150L;

	public AdalinePerceptron() {
		w = new SparseVector();
		age = 0.0;
	}

	@Override
	public void init(String prefix) {
		// w = 0 in sparse representation.
		age = 0.0;
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.1);
	}

	@Override
	/**
	 * Performs the update: w_{i+1} = w_i + eta_i * (y - w' * x) * x. 
	 */
	public void update(SparseVector instance, double label) {
	  // convert label
	  double l = (label == 0.0) ? -1.0 : label;
	      
	  // Update age.
		age++;
		double rate = 1.0 / age;
		
		// Calculate w' * x.
		double s = w.mul(instance);
		
	  // Calculate the update.
		w.mul(1 - rate);
		w.add(instance, rate / lambda * (l - s));
	}

	@Override
	/**
	 * Returns the prediction; signum(w' * x).
	 */
	public double predict(SparseVector instance) {
		// Calculate w' * x.
		double s = w.mul(instance);
	  return s >= 0 ? 1.0 : 0.0; 
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

