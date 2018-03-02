package gossipLearning.models;

import gossipLearning.interfaces.models.MatrixBasedModel;
import gossipLearning.interfaces.models.Partializable;
import gossipLearning.utils.Matrix;
import gossipLearning.utils.SparseVector;

import java.util.Random;
import java.util.Vector;

import peersim.config.Configuration;

public class PowerMethod implements MatrixBasedModel, Partializable {
  private static final long serialVersionUID = 3933436730124024955L;
  private static final double EPS = 1E-200;
  
  private static final String PAR_NUMV = "PowerMethod.maxIndex";
  
  protected double age;
  protected Vector<Double> eigenValues;
  protected Vector<SparseVector> eigenVectors;
  protected Vector<Integer> isConverged;
  protected int maxSize;
  protected double wSize;
  
  protected int maxIndex;
  
  private Random r;
  
  public PowerMethod() {
    age = 1.0;
    eigenValues = new Vector<Double>();
    eigenVectors = new Vector<SparseVector>();
    isConverged = new Vector<Integer>();
    maxSize = 0;
    wSize = 10.0;
    r = new Random(System.nanoTime());
    maxIndex = 0;
  }
  
  public PowerMethod(String prefix) {
    this();
    maxIndex = Configuration.getInt(prefix + "." + PAR_NUMV);
  }
  
  public PowerMethod(int maxIndex) {
    this();
    this.maxIndex = maxIndex;
  }
  
  public PowerMethod(PowerMethod a) {
    age = a.age;
    eigenValues = new Vector<Double>();
    eigenVectors = new Vector<SparseVector>();
    isConverged = new Vector<Integer>();
    for (int i = 0; i <= a.maxIndex && i < a.maxSize; i++) {
      eigenValues.add(a.eigenValues.get(i));
      eigenVectors.add((SparseVector)a.eigenVectors.get(i).clone());
      isConverged.add(a.isConverged.get(i));
    }
    maxSize = a.maxSize;
    wSize = a.wSize;
    r = new Random(System.nanoTime());
    maxIndex = a.maxIndex;
  }
  
  @Override
  public Object clone() {
    return new PowerMethod(this);
  }

  @Override
  public double getAge() {
    return age;
  }
  
  private void update(int index, SparseVector instance, double label) {
    if (index > 0 && isConverged.get(index-1) > 0) {
      return;
    }
    
    SparseVector inst = new SparseVector(instance);
    double prevEV = eigenValues.get(index);
    
    // deflation step
    for (int i = 0; i < index; i++) {
      SparseVector vector = new SparseVector(eigenVectors.get(i));
      vector.normalize();
      inst.add(vector.mul(vector.get((int)label)), -eigenValues.get(i));
    }
    
    // update step
    double dotProd = eigenVectors.get(index).mul(inst);
    double vectorValue = eigenVectors.get(index).get((int)label);
    double value = dotProd / vectorValue;
    if (Math.abs(dotProd) < EPS || Math.abs(vectorValue) < EPS) {
      value = 0.0;
    }
    eigenValues.set(index, (1.0 - 1.0 / wSize) * eigenValues.get(index) + (1.0 / wSize) * value);
    eigenVectors.get(index).put((int)label, (value == 0.0) ? value : dotProd / eigenValues.get(index));
    
    // if the vector goes to 0, the corresponding eigenvalue is 0
    if (eigenVectors.get(index).size() == 0) {
      eigenValues.set(index, 0.0);
    }
    
    // check convergence
    if (Math.abs(eigenValues.get(index) - prevEV) < EPS) {
      isConverged.set(index, isConverged.get(index) -1);
    } else {
      isConverged.set(index, 3);
    }
  }
  
  private void initialize(int prevIdx, int maxIdx) {
    for (int i = 0; i <= maxIdx; i++) {
      if (i < prevIdx) {
        // ith is already initialized
        for (int j = prevIdx; j <= maxIdx; j++) {
          eigenVectors.get(i).put(j, r.nextDouble() - 0.5);
        }
        eigenVectors.get(i).normalize();
      } else {
        // ith is not initialized yet
        isConverged.add(3);
        eigenValues.add(r.nextDouble() - 0.5);
        eigenVectors.add(new SparseVector());
        for (int j = 0; j <= maxIdx; j++) {
          eigenVectors.get(i).put(j, r.nextDouble() - 0.5);
        }
        eigenVectors.get(i).normalize();
      }
      if (i >= maxIndex) {
        return;
      }
    }
  }

  @Override
  public SparseVector update(int rowIndex, SparseVector rowModel, SparseVector instance) {
    age ++;
    // initialize variables if necessary
    int maxIdx = Math.max(instance.maxIndex(), rowIndex);
    if (maxSize <= maxIdx) {
      initialize(maxSize, maxIdx);
      maxSize = maxIdx + 1;
      wSize = maxSize;
    }
    
    // update vectors
    for (int i = 0; i < maxSize && i <= maxIndex; i++) {
      update(i, instance, rowIndex);
    }
    return null;
  }

  @Override
  public double predict(int rowIndex, SparseVector rowModel, int columnIndex) {
    return eigenValues.size() > columnIndex ? eigenValues.get(columnIndex) : 0.0;
  }
  
  @Override
  public PowerMethod getModelPart() {
    return this;
  }

  public Vector<Double> getValues() {
    Vector<Double> ret = new Vector<Double>();
    for (int i = 0; i < eigenValues.size(); i++) {
      ret.add(eigenValues.get(i));
    }
    return ret;
  }
  
  public Vector<SparseVector> getVectors() {
    Vector<SparseVector> ret = new Vector<SparseVector>();
    for (int i = 0; i < eigenVectors.size(); i++) {
      ret.add((new SparseVector(eigenVectors.get(i))).normalize());
    }
    return ret;
  }
  
  public boolean isConverged(int index) {
    return isConverged.get(index) <= 0;
  }

  @Override
  public Matrix getV() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Matrix getUSi(SparseVector ui) {
    // TODO Auto-generated method stub
    return null;
  }

}
