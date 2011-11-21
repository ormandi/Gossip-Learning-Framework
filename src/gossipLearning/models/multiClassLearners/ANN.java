package gossipLearning.models.multiClassLearners;

import gossipLearning.interfaces.ProbabilityModel;
import gossipLearning.utils.Matrix;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import peersim.config.Configuration;
import peersim.core.CommonState;

/**
 * This is the gossip learning based implementation of the Backpropagation algorithm which
 * can teach an feedforward artificial neural network. 
 * 
 * @author Róbert Ormándi
 */
public class ANN extends ProbabilityModel {
  private static final long serialVersionUID = -255109832539443249L;
  protected static final String PAR_HIDDEN = "ann.hiddenLayers";
  protected static final String PAR_GAMMA = "ann.gammas";
  protected static final String PAR_LAMBDA = "ann.lambda";
  protected static final String PAR_LR = "ann.learningRate";
  
  private Matrix[] thetas = null;
  private double[] gammas = null;
  private TreeMap<Integer,Integer> sparseDimMap = null;  // inputDim -> matrixDim
  private int numberOfClasses = 2;
  private double lambda = 0.001;
  private double age = 0;
  private double learningRate = 0.001;
  
  public ANN() {
    sparseDimMap = new TreeMap<Integer,Integer>();
  }
    
  // copy constructor (e.g. it is used in clone)
  public ANN(ANN a) {
    // copy thetas
    thetas = new Matrix[a.thetas.length];
    for (int i = 0; i < thetas.length; i ++) {
      thetas[i] = new Matrix(a.thetas[i]);
    }
    
    // copy gammas
    gammas = new double[a.gammas.length];
    for (int i = 0; i < gammas.length; i ++) {
      gammas[i] = a.gammas[i];
    }
    
    // copy sparse dim mapping
    sparseDimMap = new TreeMap<Integer, Integer>();
    for (int inputDim : a.sparseDimMap.keySet()) {
      int matrixDim = a.sparseDimMap.get(inputDim);
      sparseDimMap.put(inputDim, matrixDim);
    }
    
    // copy the number of classes
    numberOfClasses = a.numberOfClasses;
    
    // copy lambda
    lambda = a.lambda;
    
    // copy age
    age = a.age;
    
    // copy learning rate
    learningRate = a.learningRate;
  }

  @Override
  public Object clone() {
    return new ANN(this);
  }
  
  /**
   * This method initializes the neural network with several hidden layers.
   * Each hidden layers contains a predefined number of hidden neurons with randomly initialized weights (between 0.0 and 1.0).
   * The number of hidden layers and the number neurons in each layer is defined in property prefix.ann.hiddenLayers. This
   * property is a comma separated string containing the number of hidden neurons from left to right. I.e. "1,2,3" will generates
   * a neural network with 3 hidden layer (plus the input and output layer). The first layer is the input layer which
   * contains input dimensional number of neurons, the second layer (the first hidden layer) consists of only 1 neuron, the following 
   * hidden layer stores 2 neurons, similarly the third hidden layer consists of 3 neurons and finally the output layer contains
   * number of classes neurons.<br/>
   * Here we can specify the weight of sigmoid function of each layer. This determines the slope of each sigmoid.
   */
  @Override
  public void init(String prefix) {
    // read number of hidden neurons
    String hiddenLayers = Configuration.getString(prefix + "." + PAR_HIDDEN, "");
    String[] numOfHiddenNeuronsStr = (hiddenLayers != null && !hiddenLayers.equals("")) ? hiddenLayers.split("\\s*,\\s*") : new String[0] ;
    thetas = new Matrix[numOfHiddenNeuronsStr.length + 1];
    // we assume that the input space at least one dimensional later it can be increased
    int prevDim = 0;
    int hidden = -1;
    for (int i = 0; i < numOfHiddenNeuronsStr.length; i ++) {
      hidden = Integer.parseInt(numOfHiddenNeuronsStr[i]);
      thetas[i] = new Matrix(hidden, prevDim + 1);
      prevDim = hidden;
    }
    thetas[numOfHiddenNeuronsStr.length] = new Matrix(getNumberOfClasses(), prevDim + 1);
    randomInitThetas();
    
    // read gammas
    String gammasStr = Configuration.getString(prefix + "." + PAR_GAMMA, null);
    gammas = new double[thetas.length];
    if (gammasStr == null) {
      // use default gamma values
      Arrays.fill(gammas, 1.0);
    } else {
      String[] gammasStrArray = gammasStr.split("\\s*,\\s*");
      if (gammasStrArray.length != gammas.length) {
        // number of gamma values is different from number of applied sigmoids => error
        throw new RuntimeException("Number of gamma values is different from number of applied sigmoids ("+thetas.length+")!");
      } else {
        // parse gamma values
        for (int i = 0; i < gammasStrArray.length; i ++) {
          gammas[i] = Double.parseDouble(gammasStrArray[i]);
        }
      }
    }
    
    // clear previous mapping
    sparseDimMap.clear();
    // initialize bias dimension
    sparseDimMap.put(-1, 0);
    
    // parse lambda
    lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 0.001);
    
    // parse learning rate
    learningRate = Configuration.getDouble(prefix + "." + PAR_LR, 0.001); 
  }
  
  /**
   * Produces a random theta value. This is created to handle uniformly the initialization.
   * 
   * @return random theta value
   */
  private double randomTheta() {
    return (CommonState.r.nextDouble() - 0.5) / 10.0;
  }
  
  /**
   * Initializes the theta matrices.
   */
  private void randomInitThetas() {
    for (int l = 0; l < thetas.length; l ++) {
      // initialize each matrix
      for (int i = 0; i < thetas[l].getNumberOfRows(); i ++) {
        // each value
        for (int j = 0; j < thetas[l].getNumberOfColumns(); j++) {
          thetas[l].setValue(i, j, randomTheta());
        }
      }
    }
  }
  
  /**
   * Due to sparse instance representation initially we do not now the dimension of the input space. Since 
   * we have to handle the new upcoming dimension during the learning. This method is a helper performing
   * the above mentioned process. It simply manages a mapping between the already saw dimensions (input dimensions)
   * and the corresponding matrix columns (matrix dimensions). If a new input dimension arrives with the current instance
   * a new column is added to the necessary matrix and filled with random values. After this step the new dimension also
   * learned.<br/>  
   * Notice that the dimension of the input space must not be infinity!
   * 
   * @param x instance
   */
  private void adjustInputLayer(Map<Integer,Double> x) {
    // insert a new column into the first theta matrix for each previously unseen dimension
    
    // FIXME: After debug replace it with sparse version
    for (int inputDim : x.keySet()) {
    //for (int inputDim = 0; inputDim < 400; inputDim ++) {
      if (!sparseDimMap.containsKey(inputDim)) {
        int matrixDim = thetas[0].getNumberOfColumns();
        sparseDimMap.put(inputDim, matrixDim);
        Matrix newTheta = new Matrix(thetas[0].getNumberOfRows(), thetas[0].getNumberOfColumns() + 1);
        
        // copy existing data
        for (int i = 0; i < thetas[0].getNumberOfRows(); i ++) {
          for (int j = 0; j < thetas[0].getNumberOfColumns(); j ++) {
            newTheta.setValue(i, j, thetas[0].getValue(i, j));
          }
        }
        
        // fill new line
        for (int i = 0; i < newTheta.getNumberOfRows(); i ++) {
          newTheta.setValue(i, thetas[0].getNumberOfColumns(), randomTheta());
        }
        
        // store new theta
        thetas[0] = newTheta;
      }
    }
  }
  
  /**
   * Sigmoid function for matrix input which is applied element wise. 
   * 
   * @param m input matrix
   * @param gamma slope of sigmoid
   * @return reference to input m for supporting cascade call of the method
   */
  private Matrix sigmoid(Matrix m, double gamma) {
    for (int i = 0; i < m.getNumberOfRows(); i ++) {
      for (int j = 0; j < m.getNumberOfColumns(); j ++) {
        m.setValue(i, j, 1.0 / (1.0 + Math.exp(-1.0 * gamma * m.getValue(i, j))));
      }
    }
    return m;
  }
  
  /**
   * This method performs the propagation phase of the learning. Basically it computes the activation of each layer
   * and returns it as an array of matrices which are column vectors.
   * 
   * @param x training sample on which the propagation is computed
   * @return activations of each layer
   */
  private Matrix[] computeActivations(Map<Integer,Double> x) {
    // create a[0] i.e. the instance as an output vector
    Matrix[] a = new Matrix[thetas.length + 1];
    a[0] = new Matrix(thetas[0].getNumberOfColumns(), 1);
    for (int inputDim : x.keySet()) {
      Integer matrixDimInt = sparseDimMap.get(inputDim);
      if (matrixDimInt != null) {
        int matrixDim = matrixDimInt.intValue();
        a[0].setValue(matrixDim, 0, x.get(inputDim));
      }
    }
    
    // perform propagation
    for (int l = 1; l < a.length; l ++) {
      // compute a[l] activations using a[l-1] activation vector and thetas[l-1]
      Matrix al = sigmoid(thetas[l-1].mul(a[l-1]), gammas[l-1]);
      
      // adding bias term
      a[l] = new Matrix(al.getNumberOfRows() + 1, 1, 1.0);
      for (int i = 1; i < a[l].getNumberOfRows(); i++) {
        a[l].setValue(i, 0, al.getValue(i-1, 0));
      }
    }
    // remove the bias term from the output layer
    Matrix aOut = new Matrix(a[a.length - 1].getNumberOfRows() - 1, 1);
    for (int i = 1; i < a[a.length - 1].getNumberOfRows(); i ++) {
      aOut.setValue(i - 1, 0, a[a.length - 1].getValue(i, 0));
    }
    a[a.length - 1] = aOut;
    return a;
  }
  
  /**
   * 
   * @param a
   * @param y
   * @param nu
   */
  private void updateThetas(Matrix[] a, Matrix y, double nu) {
    // compute delta[l+1]
    Matrix delta = new Matrix(getNumberOfClasses()+1, 1, 0.0);
    for (int i = 1; i <= getNumberOfClasses(); i ++) {
      delta.setValue(i, 0, a[thetas.length].getValue(i-1, 0) - y.getValue(i-1, 0));
    }
    
    // perform backpropagation of error
    for (int l = thetas.length - 1; l >= 0; l --) {
      // create a temporary variable
      Matrix gammaActivationOneMinusActivation = new Matrix(a[l].getNumberOfRows(), 1, 1.0);
      gammaActivationOneMinusActivation = gammaActivationOneMinusActivation.subtract(a[l]).mul(gammas[l]).pointMul(a[l]);
      
      // remove bias term
      Matrix tmpDelta = new Matrix(delta.getNumberOfRows() - 1, 1, 0.0);
      for (int i = 1; i < delta.getNumberOfRows(); i ++) {
        tmpDelta.setValue(i-1, 0, delta.getValue(i, 0));
      }
      delta = tmpDelta;
      
      // compute delta[l] from delta[l+1], a[l] and thetas[l]
      Matrix prevDelta = thetas[l].transpose().mul(delta);
      thetas[l].transpose();
      prevDelta = prevDelta.pointMul(gammaActivationOneMinusActivation);
      
      // compute gradient_l
      Matrix tmpTheta = new Matrix(thetas[l]);
      for (int i = 0; i < tmpTheta.getNumberOfRows(); i ++) {
        tmpTheta.setValue(i, 0, 0.0);
      }
      Matrix lambdaTheta = tmpTheta.mul(lambda);
      Matrix grad_l = delta.mul(a[l].transpose()).add(lambdaTheta);
      a[l].transpose();
      
      //System.out.println("grad_" + l + "=\n" + grad_l);
      
      // update theta_l
      thetas[l] = thetas[l].subtract(grad_l.mul(nu));
      
      // update delta
      delta = prevDelta;
    }
    
  }
  
  @Override
  public void update(Map<Integer, Double> instance, double label) {
    // increase age
    age ++;
    
    // create y
    Matrix y = new Matrix(getNumberOfClasses(), 1);
    y.setValue((int) label, 0, 1.0);
    
    // add bias part
    instance.put(-1, 1.0);
    // adjust the column dimension of theta[0]
    adjustInputLayer(instance);
    
    // perform propagation
    Matrix[] a = computeActivations(instance);
    
    // perform update
    double nu = learningRate / (double)age;
    updateThetas(a, y, nu);
  }
  
  @Override
  public double[] distributionForInstance(Map<Integer, Double> instance) {
    // add bias part
    instance.put(-1, 1.0);
    // perform propagation
    Matrix[] a = computeActivations(instance);
    // returns the output of last layer
    return a[a.length-1].getColumn(0);  // TODO: normalization
  }
  
  public double computeCostFunction(Map<Integer, Double> x, double label) {
    return computeCostFunction(x, label, lambda);
  }
  
  public double computeCostFunction(Map<Integer, Double> x, double label, double lv) {
    // create y
    Matrix y = new Matrix(getNumberOfClasses(), 1);
    y.setValue((int) label, 0, 1.0);
    
    // predict y
    Matrix h = new Matrix(distributionForInstance(x), false);
    
    // compute cost
    double cost = 0.0;
    for (int i = 0; i < getNumberOfClasses(); i ++) {
      cost += - y.getValue(i, 0) * ((h.getValue(i, 0) > 1.0E-6) ? Math.log(h.getValue(i, 0)) : 0.0) - (1.0 - y.getValue(i, 0)) * ((1.0 - h.getValue(i, 0) > 1.0E-6) ?  Math.log(1.0 - h.getValue(i, 0)) : 0.0);
    }
    
    // adding regularization term
    double reg = 0.0;
    for (int l = 0; l < thetas.length; l ++) {
      for (int i = 0; i < thetas[l].getNumberOfRows(); i ++) {
        for (int j = 1; j < thetas[l].getNumberOfColumns(); j ++) {
          reg += thetas[l].getValue(i, j) * thetas[l].getValue(i, j);
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
  
  @Override
  public String toString() {
    StringBuffer out = new StringBuffer();
    for (int l = 0; l < thetas.length; l ++) {
      out.append("Theta[" + l + "]:\n").append(thetas[l]);
    }
    return out.toString();
  }
}
