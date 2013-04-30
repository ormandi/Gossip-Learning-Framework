package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.functions.Sigmoid;
import gossipLearning.interfaces.functions.SigmoidGradient;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

import java.util.Arrays;

import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * <b>Update rule: </b>
 * <ul>
 * <li>delta for output layer: d^(o) = a^(o) - Y </li>
 * <li>delta for layer (i): d^i = T^(i) * d^(i+1) .* g'(z^(i)) </li>
 * <li>gradient (i): D^(i) = d^(i+1) * a^(i) </li>
 * <li>update (i): T^(i) = T^(i) - D^(i) </li>
 * </ul>
 * <b>where</b>
 * <ul>
 * <li>T^(i): parameter matrix of layer i </li>
 * <li>a^(i): result of layer i </li>
 * <li>z^(i): result of layer i, without applying activation function </li>
 * <ul>
 * <li> z^(i) = a^(i-1) * T^(i), if i > 0</li>
 * <li> z^(0) = X * T^(0), for the input layer</li>
 * </ul>
 * </ul>
 * 
 * @author István Hegedűs
 */
public class ANN extends ProbabilityModel {
  private static final long serialVersionUID = 5187257180709173833L;
  protected static final String PAR_HIDDEN = "ANN.hiddenLayers";
  protected static final String PAR_LAMBDA = "ANN.lambda";
  
  protected double lambda;
  protected Function fAct;
  protected Function fGrad;

  protected int numberOfClasses;
  protected double[] distribution;
  
  /** parameter matrices of the layers */
  protected Matrix[] thetas;
  /** output of the layers without applying activation function */
  protected Matrix[] products;
  /** size of the layers including the number of features + 1 (first)
   * and the number of classes (last)*/
  protected int[] layersSizes;
  
  public ANN() {
    fAct = new Sigmoid();
    fGrad = new SigmoidGradient();
    age = 0.0;
  }
  
  public ANN(ANN a) {
    this();
    age = a.age;
    lambda = a.lambda;
    numberOfClasses = a.numberOfClasses;
    if (a.distribution != null) {
      distribution = Arrays.copyOf(a.distribution, a.distribution.length);
    }
    if (a.thetas != null) {
      thetas = new Matrix[a.thetas.length];
      products = new Matrix[a.products.length];
      for (int i = 0; i < a.thetas.length; i++) {
        thetas[i] = (Matrix)a.thetas[i].clone();
        products[i] = (Matrix)a.products[i].clone();
      }
    }
    if (a.layersSizes != null) {
      layersSizes = Arrays.copyOf(a.layersSizes, a.layersSizes.length);
    }
  }
  
  @Override
  public Object clone() {
    return new ANN(this);
  }

  @Override
  public void init(String prefix) {
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA);
    String layers = Configuration.getString(prefix + "." + PAR_HIDDEN, null);
    String[] layersSizes = null;
    int numLayers = layers == null ? 0 : (layersSizes = layers.split(",")).length;
    thetas = new Matrix[numLayers + 1];
    products = new Matrix[numLayers + 1];
    // first is numOfFeatures + 1, last is numOfClasses
    this.layersSizes = new int[numLayers + 2];
    for (int i = 0; i < numLayers; i++) {
      // plus 1 for the bias
      this.layersSizes[i + 1] = Integer.parseInt(layersSizes[i]) + 1;
    }
  }

  @Override
  public double[] distributionForInstance(SparseVector instance) {
    if (distribution == null || distribution.length != numberOfClasses) {
      distribution = new double[numberOfClasses];
    }
    Matrix predicted = evaluate(instance);
    if (predicted != null) {
      for (int i = 0; i < numberOfClasses; i++) {
        distribution[i] = predicted.get(0, i);
      }
    }
    return distribution;
  }
  
  @Override
  public void update(SparseVector instance, double label) {
    // the instance is a row vector (1Xd)
    int numOfFeatures = instance.maxIndex() + 1;
    if (layersSizes[0] <= numOfFeatures) {
      // plus 1 for the bias
      layersSizes[0] = numOfFeatures + 1;
    }
    layersSizes[layersSizes.length - 1] = numberOfClasses;
    
    // allocates and initializes layers if necessary
    adjustLayers();
    
    // expected vector
    Matrix expected = new Matrix(1, numberOfClasses).set(0, (int)label, 1.0);
    
    // update layers
    update(instance, expected);
  }
  
  private void update(SparseVector instance, Matrix expected) {
    age ++;
    double nu = 1.0 / (lambda * age);
    Matrix gradient;
    
    // evaluate instance
    Matrix predicted = evaluate(instance);
    // delta for computing gradient
    Matrix delta = predicted.subtract(expected);
    
    // hidden layers
    for (int i = thetas.length - 1; i > 0; i--) {
      gradient = products[i - 1].apply(fAct).transpose().mul(delta);
      // next delta
      delta = thetas[i].mul(delta.transpose()).transpose().pointMulEquals(products[i - 1].applyEquals(fGrad));
      // avoiding bias regularization
      thetas[i].mulEquals(0, layersSizes[i] - 2, 0, layersSizes[i + 1] - 1, 1.0 - nu * lambda);
      // scaling with learning rate
      gradient.mulEquals(0, layersSizes[i] - 2, 0, layersSizes[i + 1] - 1, nu);
      gradient.mulEquals(layersSizes[i] - 1, layersSizes[i] - 1, 0, layersSizes[i + 1] - 1, nu * lambda);
      // update
      thetas[i].addEquals(gradient, -1.0);
    }
    
    // input layer
    gradient = new Matrix(instance, delta.getRow(0), layersSizes[0]);
    // avoiding bias regularization
    thetas[0].mulEquals(0, layersSizes[0] - 2, 0, layersSizes[0 + 1] - 1, 1.0 - nu * lambda);
    // scaling with learning rate
    gradient.mulEquals(0, layersSizes[0] - 2, 0, layersSizes[0 + 1] - 1, nu);
    gradient.mulEquals(layersSizes[0] - 1, layersSizes[0] - 1, 0, layersSizes[0 + 1] - 1, nu * lambda);
    // update
    thetas[0].addEquals(gradient, -1.0);
  }
  
  private Matrix evaluate(SparseVector instance) {
    if (thetas[0] == null) {
      return null;
    }
    Matrix activations;
    
    // input layer
    products[0] = thetas[0].mulLeft(instance);
    // add bias (last row of thata_0)
    products[0].addEquals(thetas[0].getMatrix(layersSizes[0] - 1, layersSizes[0] - 1, 0, layersSizes[1] - 1));
    // apply activation function
    activations = products[0].apply(fAct);
    
    // hidden layers
    for (int i = 1; i < thetas.length; i++) {
      // last value is for adding bias
      activations.set(0, layersSizes[i] - 1, 1.0);
      products[i] = activations.mul(thetas[i]);
      activations = products[i].apply(fAct);
    }
    return activations;
  }
  
  private void adjustLayers() {
    // thetas are initialized uniform randomly from [-scale : scale] (ML-Class)
    for (int i = 0; i < thetas.length; i++) {
      if (thetas[i] == null) {
        // initialize thetas
        double scale = Math.sqrt(6)/Math.sqrt(layersSizes[i] + layersSizes[i+1]);
        thetas[i] = new Matrix(layersSizes[i], layersSizes[i + 1], CommonState.r).mulEquals(2.0 * scale).addEquals(-scale);
      } else if (i == 0 && (layersSizes[i]) >= thetas[i].getRowDimension()){
        // resize input layer
        double scale = Math.sqrt(6)/Math.sqrt(layersSizes[i] + layersSizes[i+1]);
        thetas[i] = new Matrix(layersSizes[i], layersSizes[i + 1], CommonState.r).mulEquals(2.0 * scale).addEquals(-scale).setMatrix(thetas[i]);
      }
    }
  }
  
  public double computeCostFunction(SparseVector x, double label) {
    return computeCostFunction(x, label, lambda);
  }
  
  public double computeCostFunction(SparseVector x, double label, double lv) {
    // create y
    Matrix y = new Matrix(getNumberOfClasses(), 1);
    y.set((int) label, 0, 1.0);
    
    // predict y
    Matrix h = new Matrix(distributionForInstance(x), false);
    
    // compute cost
    double cost = 0.0;
    for (int i = 0; i < getNumberOfClasses(); i ++) {
      cost += - y.get(i, 0) * ((h.get(i, 0) > 1.0E-6) ? Math.log(h.get(i, 0)) : -1E10) - (1.0 - y.get(i, 0)) * ((1.0 - h.get(i, 0) > 1.0E-6) ?  Math.log(1.0 - h.get(i, 0)) : -1E10);
    }
    
    // adding regularization term
    double reg = 0.0;
    for (int l = 0; l < thetas.length; l ++) {
      for (int i = 0; i < thetas[l].getNumberOfRows(); i ++) {
        for (int j = 1; j < thetas[l].getNumberOfColumns(); j ++) {
          reg += thetas[l].get(i, j) * thetas[l].get(i, j);
        }
      }
    }
    reg *= (lv / 2.0);
    cost += reg;
    
    // return cost
    return cost;
  }

  @Override
  public int getNumberOfClasses() {
    return numberOfClasses;
  }

  @Override
  public void setNumberOfClasses(int numberOfClasses) {
    this.numberOfClasses = numberOfClasses;
  }
  
}
