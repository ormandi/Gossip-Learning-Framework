package gossipLearning.models.learning.multiclass;

import gossipLearning.interfaces.Function;
import gossipLearning.interfaces.functions.Sigmoid;
import gossipLearning.interfaces.functions.SigmoidGradient;
import gossipLearning.interfaces.models.ProbabilityModel;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;
import peersim.config.Configuration;
import peersim.core.CommonState;

public class ANN extends ProbabilityModel {
  private static final long serialVersionUID = 5187257180709173833L;
  protected static final String PAR_HIDDEN = "ANN.hiddenLayers";
  protected static final String PAR_LAMBDA = "ANN.lambda";
  
  protected double lambda = 0.0001;
  protected Function fAct;
  protected Function fGrad;

  protected int numberOfClasses;
  protected double[] distribution;
  
  protected Matrix[] thetas;
  protected Matrix[] products;
  protected int[] layersSizes;
  
  public ANN() {
    fAct = new Sigmoid();
    fGrad = new SigmoidGradient();
    age = 0.0;
  }
  
  @Override
  public Object clone() {
    // TODO Auto-generated method stub
    return null;
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
    
    // update
    update(instance, expected);
  }
  
  private void update(SparseVector instance, Matrix expected) {
    age ++;
    double nu = 1.0 / (lambda * age);
    Matrix gradient;
    
    // evaluated vector
    Matrix predicted = evaluate(instance);
    Matrix delta = predicted.subtract(expected);
    
    // hidden layers
    for (int i = thetas.length - 1; i > 0; i--) {
      gradient = products[i - 1].apply(fAct).transpose().mul(delta);
      delta = thetas[i].mul(delta.transpose()).transpose().pointMul(products[i - 1].apply(fAct).apply(fGrad));
      thetas[i].mul(1.0 - nu * lambda);
      thetas[i].fillRow(layersSizes[i] - 1, 0.0);
      thetas[i].addEquals(gradient, -nu);
    }
    
    // input layer
    gradient = new Matrix(instance, delta.getRow(0), layersSizes[0]);
    thetas[0].mul(1.0 - nu * lambda);
    thetas[0].fillRow(layersSizes[0] - 1, 0.0);
    thetas[0].addEquals(gradient, -nu);
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
    activations = products[0].apply(fAct);
    
    // hidden layers
    for (int i = 1; i < thetas.length; i++) {
      // last value is the bias
      activations.set(0, layersSizes[i] - 1, 1.0);
      products[i] = activations.mul(thetas[i]);
      activations = products[i].apply(fAct);
    }
    return activations;
  }
  
  private void adjustLayers() {
    for (int i = 0; i < thetas.length; i++) {
      if (thetas[i] == null) {
        // thetas are initialized uniform randomly from [-0.05 : 0.05]
        thetas[i] = new Matrix(layersSizes[i], layersSizes[i + 1], CommonState.r).addEquals(-0.5).mulEquals(0.1);
      } else if (i == 0 && (layersSizes[i]) >= thetas[i].getRowDimension()){
        // resize input layer
        thetas[i] = new Matrix(layersSizes[i], layersSizes[i + 1], CommonState.r).addEquals(-0.5).mulEquals(0.1).setMatrix(thetas[i]);
      }
    }
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
