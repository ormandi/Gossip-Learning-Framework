package gossipLearning.utils;

import java.util.Iterator;
import java.util.Random;

import gossipLearning.interfaces.Vector;

public class DenseVector extends Vector {
  private static final long serialVersionUID = 4846797768834974849L;
  private double[] values;
  
  public DenseVector() {
    this(defaultCapacity);
  }
  public DenseVector(int size) {
    values = new double[size];
  }
  public DenseVector(Vector vector) {
    values = new double[vector.length()];
    for (VectorEntry entry : vector) {
      values[entry.index] = entry.value;
    }
  }
  public DenseVector(DenseVector vector) {
    this(vector.values);
  }
  public DenseVector(double[] values) {
    this.values = new double[values.length];
    System.arraycopy(values, 0, this.values, 0, values.length);
  }

  @Override
  public DenseVector clone() {
    return new DenseVector(this);
  }
  
  @Override
  public boolean equals(Object vector) {
    if (!(vector instanceof DenseVector)) {
      return false;
    }
    DenseVector v = (DenseVector) vector;
    for (int i = 0; i < Math.max(length(), v.length()); i++) {
      if (get(i) != v.get(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int compareTo(Vector vector) {
    int prevIdx = 0;
    for (VectorEntry entry : vector) {
      for (int i = prevIdx; i < entry.index; i++) {
        if (0.0 < get(i)) {
          return 1;
        } else if (get(i) < 0.0) {
          return -1;
        }
      }
      if (get(entry.index) < entry.value) {
        return -1;
      } else if (entry.value < get(entry.index)) {
        return 1;
      }
      prevIdx = entry.index;
    }
    for (int i = prevIdx; i < values.length; i++) {
      if (0.0 < values[i]) {
        return 1;
      } else if (values[i] < 0) {
        return -1;
      }
    }
    return 0;
  }

  @Override
  public Iterator<VectorEntry> iterator() {
    return new DenseVectorIterator(this);
  }

  @Override
  public Vector clear() {
    for (int i = 0; i < values.length; i++) {
      values[i] = 0.0;
    }
    return this;
  }

  @Override
  public Vector put(int index, double value) {
    if (values.length <= index) {
      stretch(index + 1);
    }
    values[index] = value;
    return this;
  }

  @Override
  public double get(int index) {
    if (index < values.length) {
      return values[index];
    }
    return 0.0;
  }

  @Override
  public double remove(int index) {
    if (index < values.length) {
      double value = values[index];
      values[index] = 0.0;
      return value;
    }
    return 0.0;
  }

  @Override
  public int size() {
    return values.length;
  }
  
  @Override
  public int length() {
    return values.length;
  }

  @Override
  public Vector set(Vector vector) {
    if (vector instanceof DenseVector) {
      return set((DenseVector)vector);
    } else if (vector instanceof SparseVector) {
      return set((SparseVector)vector);
    } else {
      return super.set(vector);
    }
  }
  
  protected Vector set(DenseVector vector) {
    if (values.length < vector.values.length) {
      values = new double[vector.values.length];
    }
    for (int i = 0; i < vector.values.length; i++) {
      values[i] = vector.values[i];
    }
    for (int i = vector.values.length; i < values.length; i++) {
      values[i] = 0.0;
    }
    return this;
  }
  
  protected Vector set(SparseVector vector) {
    if (values.length < vector.length()) {
      values = new double[vector.length()];
    }
    clear();
    for (int i = 0; i < vector.size(); i++) {
      values[vector.indexAt(i)] = vector.valueAt(i);
    }
    return this;
  }

  @Override
  public Vector add(int index, double value) {
    if (values.length <= index) {
      stretch(index + 1);
    }
    values[index] += value;
    return this;
  }

  @Override
  public Vector add(double[] vector) {
    return add(vector, 1.0);
  }

  @Override
  public Vector add(double[] vector, double times) {
    if (values.length < vector.length) {
      stretch(vector.length);
    }
    for (int i = 0; i < vector.length; i++) {
      values[i] += times * vector[i];
    }
    return this;
  }

  @Override
  public Vector add(Vector vector, double times) {
    if (vector instanceof DenseVector) {
      return add((DenseVector)vector, times);
    } else if (vector instanceof SparseVector) {
      return add((SparseVector)vector, times);
    } else {
      return super.add(vector, times);
    }
  }
  
  protected Vector add(DenseVector vector, double times) {
    if (values.length < vector.values.length) {
      stretch(vector.values.length);
    }
    for (int i = 0; i < vector.values.length; i++) {
      values[i] += times * vector.values[i];
    }
    return this;
  }
  
  protected Vector add(SparseVector vector, double times) {
    if (values.length < vector.length()) {
      stretch(vector.length());
    }
    for (int i = 0; i < vector.size(); i++) {
      values[vector.indexAt(i)] += times * vector.valueAt(i);
    }
    return this;
  }

  @Override
  public Vector mul(double value) {
    for (int i = 0; i < values.length; i++) {
      values[i] *= value;
    }
    return this;
  }
  
  @Override
  public double mul(Vector vector) {
    if (vector instanceof DenseVector) {
      return mul((DenseVector)vector);
    } else if (vector instanceof SparseVector) {
      return mul((SparseVector)vector);
    } else {
      return super.mul(vector);
    }
  }

  protected double mul(DenseVector vector) {
    double result = 0.0;
    for (int i = 0; i < Math.min(values.length, vector.values.length); i++) {
      result += values[i] * vector.values[i];
    }
    return result;
  }
  
  protected double mul(SparseVector vector) {
    double result = 0.0;
    for (int i = 0; i < vector.size(); i++) {
      result += get(vector.indexAt(i)) * vector.valueAt(i);
    }
    return result;
  }
  
  @Override
  public Vector pointMul(Vector vector) {
    if (vector instanceof DenseVector) {
      return pointMul((DenseVector)vector);
    } else {
      return super.pointMul(vector);
    }
  }
  
  protected Vector pointMul(DenseVector vector) {
    for (int i = 0; i < values.length; i++) {
      values[i] *= vector.get(i);
    }
    return this;
  }
  
  @Override
  public Vector div(Vector vector) {
    if (vector instanceof DenseVector) {
      return div((DenseVector)vector);
    } else {
      return super.div(vector);
    }
  }
  
  protected Vector div(DenseVector vector) {
    for (int i = 0; i < values.length; i++) {
      double v = vector.get(i);
      values[i] = v == 0.0 ? 0.0 : values[i] / v;
    }
    return this;
  }

  @Override
  public Vector invert() {
    for (int i = 0; i < values.length; i++) {
      if (values[i] != 0.0) {
        values[i] = 1.0 / values[i];
      }
    }
    return this;
  }

  @Override
  public Vector powerTo(double power) {
    for (int i = 0; i < values.length; i++) {
      values[i] = Math.pow(values[i], power);
    }
    return this;
  }

  @Override
  public Vector scale(int bits, Random random) {
    for (int i = 0; i < values.length; i++) {
      assert values[i] <= 1 && values[i] >= -1;
      values[i] = Utils.scaleValueRange(values[i], bits, random);
    }
    return this;
  }

  @Override
  public double euclideanDistance(Vector vector) {
    double result = 0.0;
    for (int i = 0; i < Math.max(length(), vector.length()); i++) {
      result = Utils.hypot(result, get(i) - vector.get(i));
    }
    return result;
  }

  @Override
  public double norm1() {
    double result = 0.0;
    for (int i = 0; i < values.length; i++) {
      result += Math.abs(values[i]);
    }
    return result;
  }

  @Override
  public double norm2() {
    double result = 0.0;
    for (int i = 0; i < values.length; i++) {
      result = Utils.hypot(result, values[i]);
    }
    return result;
  }

  @Override
  public double normInf() {
    double result = 0.0;
    for (int i = 0; i < values.length; i++) {
      if (result < Math.abs(values[i])) {
        result = Math.abs(values[i]);
      }
    }
    return result;
  }

  @Override
  public double sum() {
    double result = 0.0;
    for (int i = 0; i < values.length; i++) {
      result += values[i];
    }
    return result;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("{");
    for (int i = 0; i < values.length; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(i);
      sb.append(":");
      sb.append(values[i]);
    }
    sb.append("}");
    return sb.toString();
  }
  
  private void stretch(int size) {
    double[] temp = values;
    values = new double[size];
    System.arraycopy(temp, 0, values, 0, temp.length);
  }
  
  private class DenseVectorIterator implements Iterator<VectorEntry> {
    
    private final DenseVector vector;
    private int index;
    
    public DenseVectorIterator(DenseVector vector) {
      this.vector = vector;
      index = -1;
    }
    
    @Override
    public boolean hasNext() {
      return index < vector.values.length - 1;
    }

    @Override
    public VectorEntry next() {
      index ++;
      return new VectorEntry(index, vector.values[index]);
    }

    @Override
    public void remove() {
      if (index == -1) {
        throw new IllegalStateException();
      }
      vector.remove(index);
    }
    
  }

}
