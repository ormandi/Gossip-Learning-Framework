package gossipLearning.algorithms.pegasos.model;

import gossipLearning.interfaces.SimilarityComputableModel;
import gossipLearning.utils.Utils;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class PegasosModel extends gossipLearning.utils.Cloneable<PegasosModel> implements SimilarityComputableModel<Map<Integer, Double>>, Serializable, Comparable<PegasosModel> {
  private static final long serialVersionUID = -1243440216048664692L;
  private Map<Integer, Double> w;
  private double b; // currently not used
  private long age;
  
  protected final static PegasosModelComparator mapComp = new PegasosModelComparator();
    
  public PegasosModel(Map<Integer,Double> w, double bias, long age) {
    this.w = new TreeMap<Integer,Double>();
    this.w.putAll(w);
    
    this.b = bias;
    this.age = age;
  }
  
  protected PegasosModel genericClone() {
    return new PegasosModel(w, b, age);
  }
  
  public int compareTo(PegasosModel a) {
    if (age < a.age) {
      return 1;
    } else if (age > a.age) {
      return -1;
    }
    return mapComp.compare(this, a);
  }

  public Map<Integer, Double> getW() {
    return w;
  }

  public void setW(Map<Integer, Double> w) {
    this.w = w;
  }

  public double getBias() {
    return b;
  }

  public void setBias(double b) {
    this.b = b;
  }

  public long getAge() {
    return age;
  }

  public void setAge(long age) {
    this.age = age;
  }
  
  public double predict(Map<Integer, Double> instance) {
    return (Utils.innerProduct(getW(), instance) + getBias() > 0.0) ? 1.0 : -1.0; // currently the bias not used
  }
  
  public double similarity(SimilarityComputableModel<Map<Integer, Double>> a) {
    if (a instanceof PegasosModel) {
      return Utils.computeSimilarity(w, ((PegasosModel)a).getW());
    } else {
      throw new RuntimeException("Similarity can not be computated since type mismatch: (" + this.getClass().getCanonicalName() + "," + a.getClass().getCanonicalName() + ")!");
    }
  }
  
}
