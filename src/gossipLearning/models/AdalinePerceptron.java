/**
 *
 */
package gossipLearning.models;

import java.util.Map;
import java.util.TreeMap;

import gossipLearning.interfaces.Model;
import gossipLearning.utils.Utils;

/**
 * @author csko
 * A Model for the Adaline Perceptron.
 */
public class AdalinePerceptron implements Model {
	/** @hidden */
	protected Map<Integer, Double> w;
	protected double age;
	protected int numberOfClasses = 2;

	/**
	 * Returns a clone of this object.
	 * 
	 * @return the clone of this object
	 */
	public Object clone() {
		AdalinePerceptron tmp = new AdalinePerceptron();
		tmp.w = new TreeMap<Integer, Double>();
		for (int k : w.keySet()) {
			tmp.w.put(k, (double) w.get(k));
		}
		tmp.age = age;
		return tmp;
	}

	private static final long serialVersionUID = 6040117517300790150L;

	public AdalinePerceptron() {
		w = new TreeMap<Integer, Double>();
		age = 0.0;
	}

	@Override
	public void init(String prefix) {
		// w = 0 in sparse representation.
		age = 0.0;
	}

	@Override
	/**
	 * Performs the update: w_{i+1} = w_i + eta_i * (y - w' * x) * x. 
	 */
	public void update(Map<Integer, Double> instance, double label) {
	  // convert label
	  double l = (label == 0.0) ? -1.0 : label;
    
	  // Update age.
		age++;
		double rate = 1.0 / age;
		
		// Calculate w' * x.
		int max = Utils.findMaxIdx(w, instance);
		double s = 0.0;
	    for (int i = 0; i <= max; i++) {
	        Double wi = w.get(i);
	        Double xi = instance.get(i);
	        if(wi != null && xi != null){
	        	s += xi * wi;
	        }
	    }
	    // Calculate the update.
	    for (int i = 0; i <= max; i++) {
	        Double wi = w.get(i);
	        Double xi = instance.get(i);
	        if(xi != null){ // No change if this component is 0.
		        if(wi == null){
		        	wi = 0.0;
		        }
	        	w.put(i, wi + rate * (l - s) * xi);
	        }
	    }
	}

	@Override
	/**
	 * Returns the prediction; signum(w' * x).
	 */
	public double predict(Map<Integer, Double> instance) {
		// Calculate w' * x.
		int max = Utils.findMaxIdx(w, instance);
		double s = 0.0;
	    for (int i = 0; i <= max; i ++) {
	        Double wi = w.get(i);
	        Double xi = instance.get(i);
	        if(wi != null && xi != null){
	        	s += xi * wi;	        	
	        }
	    }
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

