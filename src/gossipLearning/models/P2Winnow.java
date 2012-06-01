package gossipLearning.models;

import gossipLearning.interfaces.Model;
import gossipLearning.interfaces.VectorEntry;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;

/**
 * <p>
 * The Positive Winnow algorithm in distributed environment.
 * This algorithm based on the Winnow concept. We assume, 
 * that the given examples (x) are not negatives 
 * (so x_j >= 0 for all j and for all samples). This assumption
 * is usually satisfied in NLP tasks.
 * The algorithm needs two parameters: a <code>theta</code> threshold 
 * and an <code>eta</code> learning constant. <code>theta</code>
 * will be used for prediction and <code>eta</code> for learning. 
 * </p><p>
 * The algorithm maintains a weight vector, which have to
 * be initialized to some positive number (e.g. 1.0).
 * </p><p>
 * By prediction, we calculate the inner product of the
 * weight vector and the given instance. If this value is 
 * greater than the <code>theta</code> threshold, we predict 
 * the instance as positive, otherwise as negative.
 * </p><p>
 * By learning, we predict the instance's label. If we predicted
 * it correctly, we have to do nothing (passive case). If we 
 * have misclassified the instance, we have to do the promotion
 * or demotion step. That is, if the correct label was positive 
 * (and we predicted as negative), we have to multiply the 
 * weight vector with 1+eta (1+eta > 1). As we predicted a
 * negative sample as positive, we have to multiply the weights
 * with a positive number, which lesser than 1 (e.g. 1/(1+eta))
 * We only have to modify that coordinates, which were presented
 * in the instance (so where x_j > 0).
 * </p><p>
 * This implementation based on article from 
 * Vitor R. Carvalho and William W. Cohen:
 * <a href="http://www.cs.cmu.edu/~vitor/papers/onlinetechreport.pdf">
 * Notes on Single-Pass Online Learning Algorithms
 * </a> article.
 * </p>
 * @author Sándor Bordé
 *
 */
public class P2Winnow implements Model  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The weight vector, which will be modified through 
	 * the learning process. Because of the multiplicative
	 * model, we have to set it to 1.0.
	 */
	private SparseVector w;

	/**
	 * A {0,1} vector. ith coordinate indicates, if the 
	 * ith coordinate of the weight vector was
	 * initialized. If yes, this coordinate will not
	 * set directly to 1.0.
	 */
	private SparseVector initialized;
	
	/**
	 * Number of classes.
	 */
	private int numberOfClasses;
	
	/**
	 * Value of eta, the learning constant.
	 */
	protected double eta = 1.0;
	protected static final String PAR_ETA = "eta";
	
	/**
	 * Value of theta, the threshold by prediction.
	 */
	protected double theta = 0.5;
	protected static final String PAR_THETA = "theta";
	
	/**
	 * Default constructor, creates a P2Winnow 
	 * object with 2 classes and makes the 
	 * vector attributes.
	 */
	public P2Winnow() {
		setNumberOfClasses(2);
		w = new SparseVector();
		initialized = new SparseVector();
	}
	
	/**
	 * Constructor which initializes the number of classes
	 * attribute with the specified parameter, and creates 
	 * the sparse vectors.
	 * @param numberOfClasses number of classes
	 */
	public P2Winnow(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
		w = new SparseVector();
		initialized = new SparseVector();
	}
	
	/**
	 * "Copy" constructor, makes a deep copy of the class for the clone method.
	 * Initializes all attributes with the specified parameters.
	 * @param w weight vector
	 * @param initialized initialization marker vector
	 * @param numberOfClasses number of classes
	 * @param eta learning constant
	 */
	public P2Winnow(SparseVector w, SparseVector initialized, int numberOfClasses, double eta, double theta) {
		this.numberOfClasses = numberOfClasses;
		this.w = (SparseVector)w.clone();
		this.initialized = (SparseVector)initialized.clone();
		this.eta = eta;
		this.theta = theta;
	}

	/**
	 * Clones the object.
	 */
	public Object clone(){
		return new P2Winnow(w, initialized, numberOfClasses, eta, theta);
	}
	
	@Override
	public void init(String prefix) {
		eta = Configuration.getDouble(prefix + "." + PAR_ETA, 5.0);
		theta = Configuration.getDouble(prefix + "." + PAR_THETA, 0.5);
		w = new SparseVector();
	}

	/**
	 * The update method for Positive Winnow algorithm.
	 * Predicts the label with the current model. If the
	 * label is correct, do nothing (passive mode). If there
	 * is misclassification, promote of demote the weights
	 * depending on the correct label.
	 */
	@Override
	public void update(SparseVector instance, double label) {
		double y = (label==0.0)?-1.0:1.0;		//convert the correct label to {-1.0,1.0}
		double y_pred = (predict(instance)==0.0)?-1.0:1.0;		//convert our prediction to {-1.0,1.0} too
		if ( y != y_pred ) {					//in case of misclassification
			for ( VectorEntry ve : instance ) {	//check if the ith weight was initialized 
				if ( initialized.get(ve.index) == 0.0 ) {	//if not, we initialize it
					initialized.put(ve.index, 1.0);
					w.put(ve.index, 1.0);
				}
				w.put(ve.index, w.get(ve.index)*Math.pow((1+eta), y));	//we do the promotion/demotion depending on the correct label
			}
		}
	}

	/**
	 * Prediction. If the inner product of the weight vector
	 * and the instance is greater than a given threshold, 
	 * predict 1.0, otherwise 0.0
	 */
	@Override
	public double predict(SparseVector instance) {
		double innerProduct = w.mul(instance);	//calculates the inner product of the weights and the instance
		return (innerProduct > theta)?1.0:0.0;		//predict 1.0 if the product greater than the threshold
	}

	/**
	 * Returns the number of classes.
	 * @return the number of classes
	 */
	@Override
	public int getNumberOfClasses() {
		return this.numberOfClasses;
	}

	/**
	 * Sets the number of classes given in parameter.
	 * @param numberOfClasses number of classes
	 */
	@Override
	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;		
	}

	
}

